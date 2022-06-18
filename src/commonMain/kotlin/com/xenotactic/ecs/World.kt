package com.xenotactic.ecs;

import kotlin.time.Duration



class World {
    val injections = Injections()

    private val entityIdService = EntityIdService()
    val componentService = ComponentService(this)
    private val familyService = FamilyService(this)

    internal val entities = arrayListOf<Entity>()
    private val systems = arrayListOf<System>()

    var isUpdateInProgress = false
    val pendingModifications = mutableListOf<Pair<Entity, ModifyEntityApi.() -> Unit>>()

    inner class ModifyEntityApi(val entity: Entity) {
        fun <T : Any> addOrReplaceComponent(component: T) {
            componentService.addOrReplaceComponentForEntity(entity, component)
        }

        inline fun <reified T> removeComponent() : T {
            return componentService.removeComponentForEntity<T>(entity)
        }

        inline fun <reified T : Any> getComponentOrAdd(default: () -> T): T {
            val component = componentService.getComponentForEntityOrNull<T>(entity)
            if (component == null) {
                val newComponent = default()
                addOrReplaceComponent(newComponent)
                return newComponent
            }
            return component
        }
    }

    fun addEntity(builder: ModifyEntityApi.() -> Unit = {}): Entity {
        val id = entityIdService.getNewEntityId()
        val newEntity = Entity(id, componentService)
        entities.add(newEntity)
        modifyEntity(newEntity, builder)
        return newEntity
    }

    fun modifyEntity(entity: Entity, builder: ModifyEntityApi.() -> Unit) {
        if (isUpdateInProgress) {
            pendingModifications.add(entity to builder)
            return
        }
        builder(ModifyEntityApi(entity))
        familyService.updateFamiliesForEntity(entity)
    }

    fun createFamily(familyConfiguration: FamilyConfiguration): Family {
        return familyService.createFamily(familyConfiguration)
    }

    fun addSystem(familyConfiguration: FamilyConfiguration, system: System) {
        val family = familyService.createFamily(familyConfiguration)
        system.setFamily(family)
        systems.add(system)
    }

    fun update(deltaTime: Duration) {
        isUpdateInProgress = true
        systems.forEach { if (it.isEnabled) { it.update(deltaTime) } }
        isUpdateInProgress = false
        pendingModifications.forEach { modifyEntity(it.first, it.second) }
    }

    inline fun <reified T : Any> addComponentListener(listener: ComponentListener<T>) {
        componentService.addComponentListener(listener)
    }

    inline fun <reified T : Any> getComponentContainer(): ComponentEntityContainer<T> {
        return componentService.getOrPutContainer(T::class) as ComponentEntityContainer<T>
    }
}
