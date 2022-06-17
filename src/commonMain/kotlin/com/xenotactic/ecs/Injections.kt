package com.xenotactic.ecs

import kotlin.reflect.KClass

class Injections {
    val singletonComponents = mutableMapOf<KClass<*>, Any>()

    fun <T: Any> setSingleton(obj: T) {
        if (singletonComponents.containsKey(obj::class)) {
            throw SingletonInjectionAlreadyExistsException {
                "Singleton injection already exists: ${obj::class}"
            }
        }
        singletonComponents[obj::class] = obj
    }

    inline fun <reified T: Any> getSingleton(): T {
        return getSingletonOrNull<T>()!!
    }

    inline fun <reified T: Any> getSingletonOrNull(): T? {
        return singletonComponents[T::class] as T?
    }
}