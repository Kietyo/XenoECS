package com.xenotactic.ecs

import com.kietyo.ktruth.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

interface State

sealed interface SealedClassState: State {
    data class StringState(val test: String): SealedClassState
    data class IntState(val test: Int): SealedClassState
}

internal class SealedClassInjectionTest {
    @Test
    fun setSealedClassChildren_canGetSealedClassChildren() {
        val injections = TypedInjections<State>()

        val stringState = SealedClassState.StringState("test")
        injections.setSingletonOrThrow(stringState)
        val intState = SealedClassState.IntState(123)
        injections.setSingletonOrThrow(intState)

        assertEquals(stringState, injections.getSingleton<SealedClassState.StringState>())
        assertEquals(intState, injections.getSingleton<SealedClassState.IntState>())

        val thrown = assertFails {
            injections.getSingleton<SealedClassState>()
        }
        assertThat(thrown.message!!).contains("Singleton injection does not exist: ")
    }

    @Test
    fun sealedChild_castedAsParent_stillCannotGetUsingParentType() {
        val injections = TypedInjections<State>()

        val intState = SealedClassState.IntState(123) as SealedClassState
        injections.setSingletonOrThrow(intState)

        val thrown = assertFails {
            injections.getSingleton<SealedClassState>()
        }
        assertThat(thrown.message!!).contains("Singleton injection does not exist: ")
    }
}