package com.xenotactic.ecs

import kotlin.reflect.KClass

class Entity(
    val id: Int,
    val componentService: ComponentService
) {
//    inline fun <reified T> getComponent(): T {
//        return componentService.getComponentForEntity<T>(this)
//    }
//
//    inline fun <reified T> getComponentOrNull(): T? {
//        return componentService.getComponentForEntityOrNull<T>(this)
//    }

//    inline fun <reified T> getComponentOrAdd(default: () -> T): T {
//        val component = componentService.getComponentForEntityOrNull<T>(this)
//        if (component == null) {
//            val newComponent = default()
//            world.modifyEntity(this@Entity) {
//                addOrReplaceComponent(newComponent)
//            }
//            return newComponent
//        }
//        return component
//    }

    private fun containsComponent(klass: KClass<*>): Boolean {
        return componentService.containsComponentForEntity(klass, this)
    }

    inline fun <reified T> containsComponent(): Boolean {
        return componentService.containsComponentForEntity(T::class, this)
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