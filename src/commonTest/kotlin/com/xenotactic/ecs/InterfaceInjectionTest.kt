package com.xenotactic.ecs

import kotlin.test.Test
import kotlin.test.assertEquals

interface TestStateI

data class TestState1(val id: Int): TestStateI
data class TestState2(val id: String): TestStateI

internal class InterfaceInjectionTest {

    @Test
    fun test() {
        val injection = TypedInjections<TestStateI>()

        val state1 = TestState1(1)
        val state2 = TestState2("blah")

        injection.setSingletonOrThrow(state1)
        injection.setSingletonOrThrow(state2)

        assertEquals(state1, injection.getSingleton<TestState1>())
        assertEquals(state2, injection.getSingleton<TestState2>())
    }

    @Test
    fun testAny() {
        val injection = TypedInjections<Any>()

        val state1 = TestState1(1)
        val state2 = TestState2("blah")

        injection.setSingletonOrThrow(state1)
        injection.setSingletonOrThrow(state2)

        assertEquals(state1, injection.getSingleton<TestState1>())
        assertEquals(state2, injection.getSingleton<TestState2>())
    }
}