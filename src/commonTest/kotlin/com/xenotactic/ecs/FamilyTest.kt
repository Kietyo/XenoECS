package com.xenotactic.ecs

import kotlin.test.Test
import kotlin.test.assertEquals

internal class FamilyTest {


    @Test
    fun familyTest1() {
        val world = World()

        val family = world.createFamily(
            FamilyConfiguration(
                allOfComponents = setOf(TestComponent::class)
            )
        )

        assertEquals(family.getList().size, 0)

        val entity = world.addEntity {
            addOrReplaceComponent(TestComponent("test"))
        }

        assertEquals(family.getList().size, 1)
    }
}