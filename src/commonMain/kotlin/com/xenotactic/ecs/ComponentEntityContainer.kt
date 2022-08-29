package com.xenotactic.ecs

import kotlin.reflect.KClass

class ComponentEntityContainer<T : Any>(
    val klass: KClass<*>,
    private val world: World
) {
    private val entityIdToComponentMap: MutableMap<EntityId, T> = mutableMapOf()
    private val listeners = mutableListOf<ComponentListener<T>>()

    internal fun addOrReplaceComponentInternal(entityId: EntityId, component: Any): T? {
        val res = entityIdToComponentMap.put(entityId, component as T)
        world.familyService.updateFamiliesForEntity(entityId)
        listeners.forEach {
            it.onAdd(entityId, component)
        }
        return res
    }

    fun getComponentOrAdd(entityId: EntityId, default: () -> T): T {
        val comp = getComponentOrNull(entityId)
        if (comp != null) {
            return comp
        }
        val newComp = default()
        world.modifyEntity(entityId) {
            this.addOrReplaceComponent(newComp)
        }
        return newComp
    }

    fun getComponent(entityId: EntityId): T {
        return entityIdToComponentMap[entityId]
            ?: throw ECSComponentNotFoundException {
                "Component for class ($klass), not found for entity: ${entityId.id}"
            }
    }

    fun getComponentOrNull(entityId: EntityId): T? {
        return entityIdToComponentMap[entityId]
    }

    fun removeComponent(entityId: EntityId): T? {
        val removedComponent = entityIdToComponentMap.remove(entityId)
            ?: return null
        world.familyService.updateFamiliesForEntity(entityId)
        listeners.forEach { it.onRemove(entityId, removedComponent) }
        return removedComponent
    }

    fun containsComponent(entityId: EntityId): Boolean {
        return entityIdToComponentMap.containsKey(entityId)
    }

    fun addListener(listener: ComponentListener<T>) {
        listeners.add(listener)
        for ((entity, component) in entityIdToComponentMap) {
            listener.onExisting(entity, component)
        }
    }
}