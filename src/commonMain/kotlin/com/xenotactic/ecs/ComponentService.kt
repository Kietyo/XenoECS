package com.xenotactic.ecs

import kotlin.reflect.KClass

class ComponentService(
    val world: World
) {
    val componentTypeToArray = mutableMapOf<KClass<out Any>, ComponentEntityContainer<*>>()

    inline fun <reified T> getComponentForEntity(entity: Entity): T {
        return getComponentForEntityOrNull(entity) ?: throw ECSComponentNotFoundException {
            "No component type ${T::class} found for entity: ${entity.id}"
        }
    }

    inline fun <reified T> getComponentForEntityOrNull(entity: Entity): T? {
        val arr = componentTypeToArray[T::class] ?: return null
        return arr.getComponentOrNull(entity) as T?
    }

    fun containsComponentForEntity(kClass: KClass<*>, entity: Entity): Boolean {
        val arr = componentTypeToArray[kClass] ?: return false
        return arr.containsComponent(entity)
    }

    fun <T : Any> addOrReplaceComponentForEntity(entity: Entity, component: T) {
//        val container = componentTypeToArray.getOrPut(component!!::class) {
//            ComponentEntityContainer<T>(component!!::class, world)
//        }
        val container = getOrPutContainer(component::class)
        container.addOrReplaceComponentInternal(entity, component)
    }

    fun <T : Any> addIfNotExistsForEntity(entity: Entity, component: T) {
        val container = getOrPutContainer(component::class)
        if (!container.containsComponent(entity)) {
            container.addOrReplaceComponentInternal(entity, component)
        }
    }

    fun getOrPutContainer(klass: KClass<*>): ComponentEntityContainer<*> {
        return componentTypeToArray.getOrPut(klass) {
            ComponentEntityContainer<Any>(klass, world)
        }
    }

    inline fun <reified T> removeComponentForEntity(entity: Entity): T? {
        val container = componentTypeToArray[T::class] ?: return null
        return container.removeComponent(entity) as T?
    }

    inline fun <reified T : Any> addComponentListener(listener: ComponentListener<T>) {
        val container = getOrPutContainer(T::class) as ComponentEntityContainer<T>
        container.addListener(listener)
    }

    fun <T: Any> addComponentOrThrow(entity: Entity, component: T) {
        val container = getOrPutContainer(component::class)
        if (container.containsComponent(entity)) {
            throw ECSComponentAlreadyExistsException {
                "Component $component already exists for entity: $entity"
            }
        } else {
            container.addOrReplaceComponentInternal(entity, component)
        }
    }

}