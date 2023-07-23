package com.xenotactic.ecs

interface EntityComponentListener<T> {
    fun onAddOrReplace(oldComponent: T?, newComponent: T) = Unit
}