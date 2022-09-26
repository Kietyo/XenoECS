package com.xenotactic.ecs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

internal class InjectionsTest {
    @Test
    fun getSingletonOrNull() {
        val injections = Injections()
        assertNull(injections.getSingletonOrNull<ObjectComponent>())

    }

    @Test
    fun getSingleton() {
        val injections = Injections()
        val comp = TestComponent("blah")
        injections.setSingletonOrThrow(comp)

        assertEquals(injections.getSingleton<TestComponent>(), comp)
    }

    @Test
    fun setSingletonTwiceCausesError() {
        val injections = Injections()
        val comp = TestComponent("blah")
        injections.setSingletonOrThrow(comp)
        assertFails {
            injections.setSingletonOrThrow(comp)
        }
    }
}