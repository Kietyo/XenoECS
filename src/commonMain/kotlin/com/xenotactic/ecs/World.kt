package com.xenotactic.ecs;

import kotlin.reflect.KClass
import kotlin.time.Duration

class World {

    var injections = Injections()

    private val entityIdService = EntityIdService()
    val componentService = ComponentService(this)
    internal val familyService = FamilyService(this, componentService)

    internal val entities = arrayListOf<EntityId>()
    private val systems = arrayListOf<System>()

    private var isUpdateInProgress = false
    private val pendingModifications = mutableListOf<Pair<EntityId, ModifyEntityApi.() -> Unit>>()

    val numEntities get() = entities.size

    inner class ModifyEntityApi(val entityId: EntityId) {
        fun <T : Any> addOrReplaceComponent(component: T) {
            componentService.addOrReplaceComponentForEntity(entityId, component)
        }

        // Only adds the component to the entity if it doesn't exist.
        // Notes:
        // - If the component already exists, does nothing.
        //      Will not activate any component listeners.
        fun <T : Any> addIfNotExists(component: T) {
            componentService.addIfNotExistsForEntity(entityId, component)
        }

        // Attempts to add the component to the entity.
        // Throws an error if the entity already has the component.
        fun <T : Any> addComponentOrThrow(component: T) {
            componentService.addComponentOrThrow(entityId, component)
        }

        inline fun <reified T> removeComponent(): T? {
            return componentService.removeComponentForEntity<T>(entityId)
        }

        inline fun <reified T : Any> getComponentOrAdd(default: () -> T): T {
            val component = componentService.getComponentForEntityOrNull<T>(entityId)
            if (component == null) {
                val newComponent = default()
                addOrReplaceComponent(newComponent)
                return newComponent
            }
            return component
        }
    }

    fun containsEntity(entityId: EntityId): Boolean {
        return entities.contains(entityId)
    }

    fun addEntity(builder: ModifyEntityApi.() -> Unit = {}): EntityId {
        val id = entityIdService.getNewEntityId()
        val newEntityId = EntityId(id)
        entities.add(newEntityId)
        modifyEntity(newEntityId, builder)
        return newEntityId
    }

    fun modifyEntity(entityId: EntityId, builder: ModifyEntityApi.() -> Unit) {
        if (isUpdateInProgress) {
            pendingModifications.add(entityId to builder)
            return
        }
        builder(ModifyEntityApi(entityId))
        familyService.updateFamiliesForEntity(entityId)
    }

    fun removeEntity(entityId: EntityId) {
        componentService.removeEntity(entityId)
        entities.remove(entityId)
    }

    /**
     * Gets the family corresponding to the configuration if exists.
     * If not, then creates and returns a new family.
     */
    fun getOrCreateFamily(familyConfiguration: FamilyConfiguration): Family {
        return familyService.getOrCreateFamily(familyConfiguration).family
    }

    fun addFamilyListener(listener: FamilyListener) {
        familyService.createFamilyIfNotExistsAndAddListener(listener)
    }

    fun addSystem(familyConfiguration: FamilyConfiguration, system: System) {
        val familyNode = familyService.getOrCreateFamily(familyConfiguration)
        system.setFamily(familyNode.family)
        systems.add(system)
    }

    fun addSystem(system: System) {
        val familyNode = familyService.getOrCreateFamily(system.familyConfiguration)
        system.setFamily(familyNode.family)
        systems.add(system)
    }

    fun update(deltaTime: Duration) {
        isUpdateInProgress = true
        systems.forEach {
            if (it.isEnabled) {
                it.update(deltaTime)
            }
        }
        isUpdateInProgress = false
        pendingModifications.forEach { modifyEntity(it.first, it.second) }
    }

    inline fun <reified T : Any> addComponentListener(listener: ComponentListener<T>) {
        componentService.addComponentListener(listener)
    }

    inline fun <reified T : Any> getComponentContainer(): ComponentEntityContainer<T> {
        return getComponentContainer(T::class)
    }

    fun <T : Any> getComponentContainer(kClass: KClass<T>): ComponentEntityContainer<T> {
        return componentService.getOrPutContainer(kClass) as ComponentEntityContainer<T>
    }

    operator fun <T : Any> get(entityId: EntityId, kClass: KClass<T>): T {
        return getComponentContainer(kClass).getComponent(entityId)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.appendLine("Entities:")
        entities.forEach { entityId ->
            sb.appendLine("\t$entityId")
            componentService.getAllComponentsForEntity(entityId).sortedBy {
                it::class.toString()
            }.forEach {
                sb.appendLine("\t\t$it")
            }
        }

        return sb.toString()
    }

}
