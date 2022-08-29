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
    private var entities: ArrayList<EntityId>
) {
    fun getSequence(): Sequence<EntityId> = entities.asSequence()
    fun getList(): List<EntityId> = entities

    // Useful to avoid concurrent modifications
    fun getNewList(): List<EntityId> = entities.toList()

    // Only adds the entity to the family if it doesn't yet exist.
    // If the entity already exists, then we won't add it to this family again.
    internal fun addEntityIfNotExists(entityId: EntityId) {
        entities.add(entityId)
    }

    internal fun removeEntity(entityId: EntityId) {
        entities.remove(entityId)
    }

    fun containsEntity(entityId: EntityId): Boolean {
        return entities.contains(entityId)
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
    val world: World,
    val componentService: ComponentService
) {
    private val families = mutableMapOf<FamilyConfiguration, FamilyNode>()

    fun updateFamiliesForEntity(entityId: EntityId) {
        for ((config, node) in families) {
            if (matchesFamilyConfiguration(entityId, config)) {
                if (!node.family.containsEntity(entityId)) {
                    // Only add and call listeners if it didn't already exists in the family.
                    node.family.addEntityIfNotExists(entityId)
                    for (listener in node.listeners) {
                        listener.onAdd(entityId)
                    }
                }
            } else {
                if (node.family.containsEntity(entityId)) {
                    // Only remove entity and call listeners if the entity was already a part
                    // of the family.
                    node.family.removeEntity(entityId)
                    for (listener in node.listeners) {
                        listener.onRemove(entityId)
                    }
                }
            }
        }
    }

    fun matchesFamilyConfiguration(entityId: EntityId, familyConfiguration: FamilyConfiguration): Boolean {
        return familyConfiguration.allOfComponents.all {
            componentService.containsComponentForEntity(it, entityId)
        } && (familyConfiguration.anyOfComponents.isEmpty() || familyConfiguration.anyOfComponents.any {
            componentService.containsComponentForEntity(it, entityId)
        }) && familyConfiguration.noneOfComponents.none {
            componentService.containsComponentForEntity(it, entityId)
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
            matchesFamilyConfiguration(it, familyConfiguration)
        }.forEach {
            node.family.addEntityIfNotExists(it)
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