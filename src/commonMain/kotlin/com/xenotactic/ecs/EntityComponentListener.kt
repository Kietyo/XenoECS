package com.xenotactic.ecs

interface EntityComponentListener<T> {
    fun onAdd(newComponent: T)
    fun onReplace(oldComponent: T, newComponent: T)
    fun onAddOrReplace(oldComponent: T?, newComponent: T)
}