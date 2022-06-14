package com.xenotactic.ecs;

import kotlin.time.Duration

class World {
    private val entityIdService = EntityIdService()
    val componentService = ComponentService(this)
    private val familyService = FamilyService(this)

    internal val entities = arrayListOf<Entity>()
    private val systems = arrayListOf<System>()

    fun addEntity(builder: EntityBuilder.() -> Unit = {}): Entity {
        val id = entityIdService.getNewEntityId()
        val newEntity = Entity(id, componentService)
        builder(EntityBuilder(newEntity, componentService))
        entities.add(newEntity)

        familyService.updateFamiliesWithNewEntity(newEntity)
        return newEntity
    }

    fun addFamily(familyConfiguration: FamilyConfiguration): Family {
        return familyService.createFamily(familyConfiguration)
    }

    fun addSystem(familyConfiguration: FamilyConfiguration, system: System) {
        val family = familyService.createFamily(familyConfiguration)
        system.setFamily(family)
        systems.add(system)
    }

    fun update(deltaTime: Duration) {
        systems.forEach { it.update(deltaTime) }
    }

    inline fun <reified T> addComponentListener(listener: ComponentListener<T>) {
        componentService.addComponentListener(listener)
    }
}
