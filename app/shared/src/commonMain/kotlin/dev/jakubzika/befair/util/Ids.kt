package dev.jakubzika.befair.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The item API validates ids against a UUID pattern and treats a replayed id as an idempotent
 * retry, so client-generated ids must be real UUIDs.
 */
@OptIn(ExperimentalUuidApi::class)
fun newItemId(): String = Uuid.random().toString()
