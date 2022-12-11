package com.xenotactic.ecs

import kotlin.reflect.KClass

abstract class IEntity(

) {
    protected abstract val componentMap: Map<KClass<out Any>, Any>
    val numComponents get() = componentMap.size
    val allComponents get() = componentMap.values
    fun containsComponentType(klass: KClass<out Any>): Boolean {
        return componentMap.containsKey(klass)
    }
    fun containsComponentTypes(vararg klass: KClass<out Any>): Boolean {
        return klass.all {
            componentMap.containsKey(it)
        }
    }
    operator fun <T : Any> get(klass: KClass<T>): T {
        return componentMap[klass]!! as T
    }
}

