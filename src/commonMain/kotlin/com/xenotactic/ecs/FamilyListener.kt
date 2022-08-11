package com.xenotactic.ecs

interface FamilyListener {
    val familyConfiguration: FamilyConfiguration

    // Listener for when an entity gets added to a family.
    fun onAdd(entity: Entity)

    // Listener for when an entity gets removed from a family.
    fun onRemove(entity: Entity)

    // For entities that already exists in the family at the time of adding the listener
    // to the world.
    fun onExisting(entity: Entity)
}