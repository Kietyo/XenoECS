package com.xenotactic.ecs

import kotlin.reflect.KClass

class ComponentEntityContainer<T : Any>(
    val klass: KClass<*>,
    private val world: World
) {
    private val entityIdToComponentMap: MutableMap<EntityId, T> = mutableMapOf()
    private val entityIdToListenersMap: MutableMap<EntityId, MutableList<EntityComponentListener<T>>> = mutableMapOf()
    private val listeners = mutableListOf<ComponentListener<T>>()

    internal fun addOrReplaceComponentInternal(entityId: EntityId, component: Any): T? {
        val previousComponent = entityIdToComponentMap.put(entityId, component as T)
        world.familyService.updateFamiliesForEntity(entityId)
        listeners.forEach {
            it.onAdd(entityId, component)
        }
        if (previousComponent == null) {
            entityIdToListenersMap.getOrElse(entityId) {
                emptyList()
            }.forEach {
                it.onAdd(component)
            }
        } else {
            entityIdToListenersMap.getOrElse(entityId) {
                emptyList()
            }.forEach {
                it.onReplace(previousComponent, component)
            }
        }

        return previousComponent
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
        return getComponentOrNull(entityId)
            ?: throw ECSComponentNotFoundException {
                val statefulEntity = world.getStatefulEntitySnapshot(entityId)
                "Component of class ($klass), not found for entity: ${entityId.id}.\nCurrent data: $statefulEntity"
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

    fun addComponentListener(listener: ComponentListener<T>) {
        listeners.add(listener)
        for ((entity, component) in entityIdToComponentMap) {
            listener.onExisting(entity, component)
        }
    }

    fun addEntityComponentListener(entityId: EntityId, listener: EntityComponentListener<T>): Closeable {
        val listeners = entityIdToListenersMap.getOrPut(entityId) {
            mutableListOf()
        }
        listeners.add(listener)
        return object : Closeable {
            override fun close() {
                listeners.remove(listener)
            }
        }
    }
}