package com.xenotactic.ecs

import kotlin.reflect.KClass

/**
 * A stateful entity is a snapshot of the entity at the time of creation,
 * with all of its various components.
 *
 * Note though that the components are references to the actual entity component
 * references. So any modifications to them will be reflected to the actual entity.
 */
data class StatefulEntity private constructor(
    val entityId: EntityId,
    private val componentMap: Map<KClass<out Any>, *>
) {
    val numComponents get() = componentMap.size
    fun containsComponentType(klass: KClass<out Any>): Boolean {
        return componentMap.containsKey(klass)
    }

    fun containsComponentTypes(vararg klass: KClass<out Any>): Boolean {
        return klass.all {
            componentMap.containsKey(it)
        }
    }

    operator fun get(klass: KClass<out Any>): Any? {
        return componentMap[klass]
    }

    companion object {
        fun create(entityId: EntityId, componentMap: Map<KClass<out Any>, *>): StatefulEntity {
            return StatefulEntity(entityId, componentMap)
        }
    }
}
