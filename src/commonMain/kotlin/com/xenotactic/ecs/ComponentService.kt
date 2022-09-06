package com.xenotactic.ecs

import kotlin.reflect.KClass

class ComponentService(
    val world: World
) {
    val componentTypeToArray = mutableMapOf<KClass<out Any>, ComponentEntityContainer<*>>()
    val entityIdToActiveComponentKlassSet = mutableMapOf<EntityId, MutableSet<KClass<out Any>>>()

    /**
     * Removes the entity by removing all components associated with the entity.
     */
    fun removeEntity(entityId: EntityId) {
        val activeComponentKlasses = entityIdToActiveComponentKlassSet.getOrElse(entityId) {
            emptySet()
        }
        for (activeComponentKlass in activeComponentKlasses) {
            removeComponentForEntity<Any>(entityId, activeComponentKlass)
        }
    }

    inline fun <reified T> getComponentForEntity(entityId: EntityId): T {
        return getComponentForEntityOrNull(entityId)
            ?: throw ECSComponentNotFoundException {
                "No component type ${T::class} found for entity: ${entityId.id}"
            }
    }

    inline fun <reified T> getComponentForEntityOrNull(entityId: EntityId): T? {
        val arr = componentTypeToArray[T::class]
            ?: return null
        return arr.getComponentOrNull(entityId) as T?
    }

    fun containsComponentForEntity(kClass: KClass<*>, entityId: EntityId): Boolean {
        val arr = componentTypeToArray[kClass]
            ?: return false
        return arr.containsComponent(entityId)
    }

    fun <T : Any> addOrReplaceComponentForEntity(entityId: EntityId, component: T) {
        val container = getOrPutContainer(component::class)
        addOrReplaceComponentInternal(container, entityId, component)
    }

    fun <T : Any> addIfNotExistsForEntity(entityId: EntityId, component: T) {
        val container = getOrPutContainer(component::class)
        if (!container.containsComponent(entityId)) {
            addOrReplaceComponentInternal(container, entityId, component)
        }
    }

    fun getOrPutContainer(klass: KClass<*>): ComponentEntityContainer<*> {
        return componentTypeToArray.getOrPut(klass) {
            ComponentEntityContainer<Any>(klass, world)
        }
    }

    inline fun <reified T> removeComponentForEntity(entityId: EntityId): T? {
        return removeComponentForEntity(entityId, T::class)
    }

    fun <T : Any> removeComponentForEntity(entityId: EntityId, componentKlass: KClass<*>): T? {
        val container = componentTypeToArray[componentKlass]
            ?: return null
        entityIdToActiveComponentKlassSet.getOrPut(entityId) {
            mutableSetOf()
        }.remove(componentKlass)
        return container.removeComponent(entityId) as T?
    }

    inline fun <reified T : Any> addComponentListener(listener: ComponentListener<T>) {
        val container = getOrPutContainer(T::class) as ComponentEntityContainer<T>
        container.addListener(listener)
    }

    /**
     * Attempts to add the component to the entity.
     * If the component already exists, then throws an ECSComponentAlreadyExistsException.
     */
    fun <T : Any> addComponentOrThrow(entityId: EntityId, component: T) {
        val container = getOrPutContainer(component::class)
        if (container.containsComponent(entityId)) {
            throw ECSComponentAlreadyExistsException {
                "Class `${component::class}` of component `$component` already exists for entity: $entityId"
            }
        } else {
            addOrReplaceComponentInternal(container, entityId, component)
        }
    }

    private fun <T : Any> addOrReplaceComponentInternal(
        container: ComponentEntityContainer<*>,
        entityId: EntityId,
        component: T
    ) {
        container.addOrReplaceComponentInternal(entityId, component)
        entityIdToActiveComponentKlassSet.getOrPut(entityId) {
            mutableSetOf()
        }.add(component::class)
    }

}