package com.xenotactic.ecs;

import kotlin.time.Duration



class World {
    private val entityIdService = EntityIdService()
    val componentService = ComponentService(this)
    private val familyService = FamilyService(this)

    internal val entities = arrayListOf<Entity>()
    private val systems = arrayListOf<System>()

    inner class ModifyEntityApi(val entity: Entity) {
        fun <T> addOrReplaceComponent(component: T) {
            componentService.addOrReplaceComponentForEntity(entity, component)
        }
    }

    fun addEntity(builder: ModifyEntityApi.() -> Unit = {}): Entity {
        val id = entityIdService.getNewEntityId()
        val newEntity = Entity(id, componentService)
        entities.add(newEntity)
        modifyEntity(newEntity, builder)
        return newEntity
    }

    fun modifyEntity(entity: Entity, builder: ModifyEntityApi.() -> Unit): Entity {
        builder(ModifyEntityApi(entity))
        familyService.updateFamiliesWithNewEntity(entity)
        return entity
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
        systems.forEach { if (it.isEnabled) { it.update(deltaTime) } }
    }

    inline fun <reified T> addComponentListener(listener: ComponentListener<T>) {
        componentService.addComponentListener(listener)
    }
}
