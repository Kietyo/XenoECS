package com.xenotactic.ecs

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ECSTest {

    @Test
    fun entityWithComponentGetsReturned() {
        val world = World()
        val component = TestComponent("test")

        val testComponentContainer = world.getComponentContainer<TestComponent>()

        val entity = world.addEntity {
            addOrReplaceComponent(component)
        }

        assertEquals(testComponentContainer.getComponent(entity), component)
    }
}