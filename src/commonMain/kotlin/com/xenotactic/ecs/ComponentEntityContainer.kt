package com.xenotactic.ecs

import kotlin.reflect.KClass

class ComponentEntityContainer<T>(
    private val world: World
) {
    private val entityIdToComponentMap: MutableMap<Int, T> = mutableMapOf()
    private val listeners = mutableListOf<ComponentListener<T>>()

    internal fun addOrReplaceComponentInternal(entity: Entity, component: T): T? {
        val res = entityIdToComponentMap.put(entity.id, component)
        listeners.forEach {
            it.onAdd(entity, component)
        }
        return res
    }

    fun getComponentOrAdd(entity: Entity, default: () -> T): T {
        val comp = getComponentOrNull(entity)
        if (comp != null) {
            return comp
        }
        val newComp = default()
        world.modifyEntity(entity) {
            this.addOrReplaceComponent(newComp)
        }
        return newComp
    }

    fun getComponent(entity: Entity): T {
        return entityIdToComponentMap[entity.id]
            ?: throw ECSComponentNotFoundException {
                "No component type found for entity: ${entity.id}"
            }
    }

    fun getComponentOrNull(entity: Entity): T? {
        return entityIdToComponentMap[entity.id]
    }

    fun removeComponent(entity: Entity): T? {
        val removedComponent = entityIdToComponentMap.remove(entity.id)
            ?: return null
        listeners.forEach { it.onRemove(entity, removedComponent) }
        return removedComponent
    }

    fun containsComponent(entity: Entity): Boolean {
        return entityIdToComponentMap.containsKey(entity.id)
    }

    fun addListener(listener: ComponentListener<T>) {
        listeners.add(listener)
    }
}