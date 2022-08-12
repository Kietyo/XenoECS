package com.xenotactic.ecs

object ObjectComponent

data class TestComponent(val value: String)

sealed class TestSealedClass {
    class RegularClassChild(
        val value: String
    ) : TestSealedClass()

    data class DataClassChild(
        val value: String
    ) : TestSealedClass()

    object ObjectClassChild: TestSealedClass()
}