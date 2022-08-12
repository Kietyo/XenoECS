package com.xenotactic.ecs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun sealedClass_regularClassChild() {
        val world = World()

        val sealedClassContainer = world.getComponentContainer<TestSealedClass>()
        val regularClassContainer = world.getComponentContainer<TestSealedClass.RegularClassChild>()

        val component = TestSealedClass.RegularClassChild("blah")

        val entity = world.addEntity {
            addComponentOrThrow(component)
        }

        assertEquals(regularClassContainer.getComponent(entity), component)
        assertEquals(regularClassContainer.getComponent(entity).value, "blah")
        assertFailsWith(ECSComponentNotFoundException::class) {
            sealedClassContainer.getComponent(entity)
        }
    }

    @Test
    fun sealedClass_dataClassChild() {
        val world = World()

        val sealedClassContainer = world.getComponentContainer<TestSealedClass>()
        val dataClassContainer = world.getComponentContainer<TestSealedClass.DataClassChild>()

        val component = TestSealedClass.DataClassChild("blah")

        val entity = world.addEntity {
            addComponentOrThrow(component)
        }

        assertEquals(dataClassContainer.getComponent(entity), component)
        assertEquals(dataClassContainer.getComponent(entity).value, "blah")
        assertFailsWith(ECSComponentNotFoundException::class) {
            sealedClassContainer.getComponent(entity)
        }
    }

    @Test
    fun sealedClass_objectChild() {
        val world = World()

        val sealedClassContainer = world.getComponentContainer<TestSealedClass>()
        val objectClassContainer = world.getComponentContainer<TestSealedClass.ObjectClassChild>()

        val component = TestSealedClass.ObjectClassChild

        val entity = world.addEntity {
            addComponentOrThrow(component)
        }

        assertEquals(objectClassContainer.getComponent(entity), component)
        assertFailsWith(ECSComponentNotFoundException::class) {
            sealedClassContainer.getComponent(entity)
        }
    }
}