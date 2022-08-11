package com.xenotactic.ecs

import kotlin.reflect.KClass

class Entity(
    val id: Int,
    val componentService: ComponentService
) {
    private fun containsComponent(klass: KClass<*>): Boolean {
        return componentService.containsComponentForEntity(klass, this)
    }

    fun matchesFamilyConfiguration(familyConfiguration: FamilyConfiguration): Boolean {
        return familyConfiguration.allOfComponents.all {
            containsComponent(it)
        } && (familyConfiguration.anyOfComponents.isEmpty() || familyConfiguration.anyOfComponents.any {
            containsComponent(it)
        }) && familyConfiguration.noneOfComponents.none {
            containsComponent(it)
        }
    }
}