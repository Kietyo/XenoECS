package com.xenotactic.ecs

interface EntityComponentListener<T> {
    fun onAdd(newComponent: T)
    fun onReplace(oldComponent: T, newComponent: T)
}