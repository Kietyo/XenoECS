package com.xenotactic.ecs

import kotlin.reflect.KClass

class TypedInjections<T: Any> {
    val singletonComponents = mutableMapOf<KClass<out T>, T>()

    /**
     * Sets singleton or throws a SingletonInjectionAlreadyExistsException if it already exists.
     */
    fun setSingletonOrThrow(obj: T) {
        val klass = obj::class
        if (singletonComponents.containsKey(klass)) {
            throw SingletonInjectionAlreadyExistsException {
                "Singleton injection already exists: ${klass}"
            }
        }
        singletonComponents[klass] = obj
    }

    inline fun <reified V: T> getSingleton(): V {
        println("V::class: ${V::class}")
        println("this::class: ${this::class}")
        println(V::class == this::class)
        return getSingletonOrNull<V>() ?:
        throw SingletonInjectionDoesNotExistException {
            "Singleton injection does not exist: ${V::class}"
        }
    }

    inline fun <reified V: T> getSingletonOrNull(): V? {
        return singletonComponents[V::class] as V?
    }
}