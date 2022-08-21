package com.xenotactic.ecs

import kotlin.reflect.KClass

class ComponentService(
    val world: World
) {
    val componentTypeToArray = mutableMapOf<KClass<out Any>, ComponentEntityContainer<*>>()

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
        //        val container = componentTypeToArray.getOrPut(component!!::class) {
        //            ComponentEntityContainer<T>(component!!::class, world)
        //        }
        val container = getOrPutContainer(component::class)
        container.addOrReplaceComponentInternal(entityId, component)
    }

    fun <T : Any> addIfNotExistsForEntity(entityId: EntityId, component: T) {
        val container = getOrPutContainer(component::class)
        if (!container.containsComponent(entityId)) {
            container.addOrReplaceComponentInternal(entityId, component)
        }
    }

    fun getOrPutContainer(klass: KClass<*>): ComponentEntityContainer<*> {
        return componentTypeToArray.getOrPut(klass) {
            ComponentEntityContainer<Any>(klass, world)
        }
    }

    inline fun <reified T> removeComponentForEntity(entityId: EntityId): T? {
        val container = componentTypeToArray[T::class]
            ?: return null
        return container.removeComponent(entityId) as T?
    }

    inline fun <reified T : Any> addComponentListener(listener: ComponentListener<T>) {
        val container = getOrPutContainer(T::class) as ComponentEntityContainer<T>
        container.addListener(listener)
    }

    fun <T : Any> addComponentOrThrow(entityId: EntityId, component: T) {
        val container = getOrPutContainer(component::class)
        if (container.containsComponent(entityId)) {
            throw ECSComponentAlreadyExistsException {
                "Class `${component::class}` of component `$component` already exists for entity: $entityId"
            }
        } else {
            container.addOrReplaceComponentInternal(entityId, component)
        }
    }

}