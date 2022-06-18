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
        val container = componentTypeToArray.getOrPut(component!!::class) {
            ComponentEntityContainer<T>(component!!::class, world)
        }
        container.addOrReplaceComponentInternal(entity, component)
    }

    fun getOrPutContainer(klass: KClass<*>): ComponentEntityContainer<*> {
        return componentTypeToArray.getOrPut(klass) {
            ComponentEntityContainer<Any>(klass, world)
        }
    }

    inline fun <reified T> removeComponentForEntity(entity: Entity): T {
        val container = componentTypeToArray[T::class] ?:
            throw ECSComponentNotFoundException {
                "Component ${T::class} not found for entity: $entity"
            }
        return container.removeComponent(entity) as T
    }

    inline fun <reified T : Any> addComponentListener(listener: ComponentListener<T>) {
        val container = getOrPutContainer(T::class) as ComponentEntityContainer<T>
        container.addListener(listener)
    }
}