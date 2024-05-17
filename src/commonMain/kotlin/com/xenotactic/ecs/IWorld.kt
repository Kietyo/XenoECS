package com.xenotactic.ecs

import kotlin.reflect.KClass

interface IWorld {
    fun containsEntity(entityId: EntityId): Boolean
    operator fun <T : Any> get(entityId: EntityId, kClass: KClass<T>): T
    fun <T : Any> getOrNull(entityId: EntityId, kClass: KClass<T>): T?
    fun getEntities(
        familyConfiguration: FamilyConfiguration
    ): Set<EntityId>
    fun getStatefulEntities(): List<StatefulEntity>
    fun getStatefulEntitySnapshots(
        familyConfiguration: FamilyConfiguration
    ): List<StatefulEntity>
    fun getStatefulEntitySnapshot(
        entityId: EntityId
    ): StatefulEntity
    fun getFirstStatefulEntityMatchingOrNull(familyConfiguration: FamilyConfiguration): StatefulEntity?
    fun getFirstStatefulEntityMatching(familyConfiguration: FamilyConfiguration): StatefulEntity
    fun getStagingEntities(): List<StagingEntity>
    fun getStagingEntity(
        entityId: EntityId
    ): StagingEntity

}