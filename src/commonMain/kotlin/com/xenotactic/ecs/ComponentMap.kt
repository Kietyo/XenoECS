package com.xenotactic.ecs

import kotlin.reflect.KClass

class ComponentMap(
    private val map: MutableMap<KClass<out Any>, Any> = mutableMapOf()
) {

    fun <T : Any> addOrReplaceComponentForEntity(component: T) {
        map[component::class] = component
    }

    fun <T : Any> addIfNotExistsForEntity(component: T) {
        if (!map.containsKey(component::class)) {
            addOrReplaceComponentForEntity(component)
        }
    }

    /**
     * Attempts to add the component to the entity.
     * If the component already exists, then throws an ECSComponentAlreadyExistsException.
     */
    fun <T : Any> addComponentOrThrow(component: T) {
        if (map.containsKey(component::class)) {
            throw ECSComponentAlreadyExistsException {
                "Class `${component::class}` of component `$component` already exists for this staging entity."
            }
        } else {
            addOrReplaceComponentForEntity(component)
        }
    }

    inline fun <reified T: Any> containsComponentType(): Boolean {
        return containsComponentType(T::class)
    }
    fun containsComponentType(klass: KClass<out Any>): Boolean {
        return map.containsKey(klass)
    }
    fun containsComponentTypes(vararg klass: KClass<out Any>): Boolean {
        return klass.all {
            map.containsKey(it)
        }
    }
    operator fun <T : Any> get(klass: KClass<T>): T {
        return map[klass]!! as T
    }
}