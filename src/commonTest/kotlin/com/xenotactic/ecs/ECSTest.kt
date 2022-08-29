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

    @Test
    fun enumClass() {
        val world = World()

        val enumClassContainer = world.getComponentContainer<TestEnumClass>()

        val entity = world.addEntity {
            addComponentOrThrow(TestEnumClass.ENUM_1)
        }

        assertEquals(enumClassContainer.getComponent(entity), TestEnumClass.ENUM_1)

        assertFailsWith<ECSComponentAlreadyExistsException> {
            world.modifyEntity(entity) {
                addComponentOrThrow(TestEnumClass.ENUM_2)
            }
        }

        assertEquals(enumClassContainer.getComponent(entity), TestEnumClass.ENUM_1)
    }

    @Test
    fun addIfNotExistsAddsToFamily() {
        val world = World()
        val objectComponentFamily = world.createFamily(
            FamilyConfiguration(
                setOf(ObjectComponent::class)
            )
        )

        val newEntity = world.addEntity()

        world.modifyEntity(newEntity) {
            addIfNotExists(ObjectComponent)
        }

        assertEquals(objectComponentFamily.getList().size, 1)
    }

    @Test
    fun addIfNotExistsAddsToFamilyWithListener() {
        val world = World()
        val objectComponentFamily = world.createFamily(
            FamilyConfiguration(
                setOf(ObjectComponent::class)
            )
        )
        world.addComponentListener(object : ComponentListener<ObjectComponent> {
            override fun onAdd(entityId: EntityId, component: ObjectComponent) {
                assertEquals(objectComponentFamily.getList().size, 1)
            }

            override fun onRemove(entityId: EntityId, component: ObjectComponent) {
                TODO("Not yet implemented")
            }

            override fun onExisting(entityId: EntityId, component: ObjectComponent) {
                TODO("Not yet implemented")
            }

        })

        val newEntity = world.addEntity()

        world.modifyEntity(newEntity) {
            addIfNotExists(ObjectComponent)
        }

        assertEquals(objectComponentFamily.getList().size, 1)
    }

    @Test
    fun removeEntity_removesFromFamilyBeforeReachingListener() {
        val world = World()
        val objectComponentFamily = world.createFamily(
            FamilyConfiguration(
                setOf(ObjectComponent::class)
            )
        )
        val newEntity = world.addEntity()
        world.modifyEntity(newEntity) {
            addIfNotExists(ObjectComponent)
        }

        world.addComponentListener(object : ComponentListener<ObjectComponent> {
            override fun onAdd(entityId: EntityId, component: ObjectComponent) {
                TODO()
            }

            override fun onRemove(entityId: EntityId, component: ObjectComponent) {
                assertEquals(objectComponentFamily.getList().size, 0)
            }

            override fun onExisting(entityId: EntityId, component: ObjectComponent) {
            }

        })

        world.modifyEntity(newEntity) {
            removeComponent<ObjectComponent>()
        }

        assertEquals(objectComponentFamily.getList().size, 0)
    }
}