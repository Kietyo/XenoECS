package com.xenotactic.ecs

interface ComponentListener<T> {
    // Listener for when a component gets added to the entity.
    fun onAdd(entityId: EntityId, component: T)

    // Listener for when a component gets removed from an entity.
    fun onRemove(entityId: EntityId, component: T)

    // Listener for entities that already contain the component.
    fun onExisting(entityId: EntityId, component: T)
}