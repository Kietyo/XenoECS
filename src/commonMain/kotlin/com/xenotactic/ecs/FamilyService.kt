package com.xenotactic.ecs

import kotlin.reflect.KClass

data class FamilyConfiguration(
    val allOfComponents: Set<KClass<*>> = emptySet(),
    val anyOfComponents: Set<KClass<*>> = emptySet(),
    val noneOfComponents: Set<KClass<*>> = emptySet()
) {
    companion object {
        val EMPTY = FamilyConfiguration()
    }
}

data class Family(
    private val familyConfiguration: FamilyConfiguration,
    private var entities: ArrayList<Entity>
) {
    fun getSequence(): Sequence<Entity> = entities.asSequence()
    fun getList(): List<Entity> = entities

    internal fun addEntity(entity: Entity) {
        entities.add(entity)
    }

    internal fun removeEntity(entity: Entity) {
        entities.remove(entity)
    }

    fun containsEntity(entity: Entity): Boolean {
        return entities.contains(entity)
    }

    companion object {
        val EMPTY = Family(FamilyConfiguration.EMPTY, ArrayList())
    }
}

data class FamilyNode(
    val family: Family,
    // Listeners for this family
    val listeners: MutableList<FamilyListener> = mutableListOf()
)

class FamilyService(
    val world: World
) {
    private val families = mutableMapOf<FamilyConfiguration, FamilyNode>()

    fun updateFamiliesForEntity(entity: Entity) {
        for ((config, node) in families) {
            if (entity.matchesFamilyConfiguration(config)) {
                if (!node.family.containsEntity(entity)) {
                    node.family.addEntity(entity)
                    for (listener in node.listeners) {
                        listener.onAdd(entity)
                    }
                }
            } else {
                node.family.removeEntity(entity)
                for (listener in node.listeners) {
                    listener.onRemove(entity)
                }
            }
        }
    }

    fun createFamilyIfNotExistsAndAddListener(
        listener: FamilyListener
    ) {
        val node = createFamily(listener.familyConfiguration)
        node.listeners.add(listener)
        for (entity in node.family.getSequence()) {
            listener.onExisting(entity)
        }
    }

    fun createFamily(familyConfiguration: FamilyConfiguration): FamilyNode {
        val node = getOrCreateFamilyNode(familyConfiguration)
        world.entities.asSequence().filter {
            it.matchesFamilyConfiguration(familyConfiguration)
        }.forEach {
            node.family.addEntity(it)
        }
        return node
    }

    private fun getOrCreateFamilyNode(familyConfiguration: FamilyConfiguration): FamilyNode {
        return families.getOrPut(familyConfiguration) {
            FamilyNode(
                Family(
                    familyConfiguration,
                    ArrayList()
                )
            )
        }
    }
}