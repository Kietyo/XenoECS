package com.xenotactic.ecs

import kotlin.reflect.KClass

data class FamilyConfiguration(
    val allOfComponents: Set<KClass<*>> = emptySet(),
    val anyOfComponents: Set<KClass<Any>> = emptySet(),
    val noneOfComponents: Set<KClass<Any>> = emptySet()
)

data class Family(
    private var entities: ArrayList<Entity>
) {
    fun getSequence() : Sequence<Entity> = entities.asSequence()
    fun getList() : List<Entity> = entities

    internal fun addEntity(entity: Entity) {
        entities.add(entity)
    }

    internal fun removeEntity(entity: Entity) {
        entities.remove(entity)
    }

    companion object {
        val EMPTY = Family(ArrayList())
    }
}

data class FamilyNode(
    var numInstances: Int,
    val family: Family
)

class FamilyService(
    val world: World
) {

    val families = mutableMapOf<FamilyConfiguration, FamilyNode>()

    fun updateFamiliesForEntity(entity: Entity) {
        for ((config, node) in families) {
            if (entity.matchesFamilyConfiguration(config)) {
                node.family.addEntity(entity)
            } else {
                node.family.removeEntity(entity)
            }
        }
    }

    fun createFamily(familyConfiguration: FamilyConfiguration): Family {
        val node = families.getOrPut(familyConfiguration) {
            FamilyNode(0,
                Family(
                    kotlin.run {
                        val entities = world.entities.filter {
                            it.matchesFamilyConfiguration(familyConfiguration)
                        }
                        val arr = ArrayList<Entity>(entities.size)
                        entities.forEach { arr.add(it) }
                        arr
                    }
                )
            )
        }
        node.numInstances++
        return node.family
    }
}