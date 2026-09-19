package dev.havlicektomas.studycoach.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ResultTest {
    @Test
    fun mapsSuccessAndRunsOnlySuccessCallback() {
        val calls = mutableListOf<String>()
        val result: Result<Int, DataError.Network> = Result.Success(21)
        val mapped = result.map { it * 2 }
            .onSuccess { calls += "success:$it" }
            .onFailure { error("Failure callback must not run") }
        assertEquals(Result.Success(42), mapped)
        assertEquals(listOf("success:42"), calls)
    }

    @Test
    fun preservesErrorAndSkipsSuccessWork() {
        val result: Result<Int, DataError.Network> = Result.Error(DataError.Network.REQUEST_TIMEOUT)
        var received: DataError.Network? = null
        val mapped = result.map { error("Transform must not run") }
            .onSuccess { error("Success callback must not run") }
            .onFailure { received = it }
        assertSame(result, mapped)
        assertEquals(DataError.Network.REQUEST_TIMEOUT, received)
        assertSame<Any>(result, result.asEmptyResult())
    }

    @Test
    fun emptySuccessDiscardsPayload() {
        assertEquals(Result.Success(Unit), Result.Success("payload").asEmptyResult())
    }

    @Test
    fun callbacksPreserveOriginalSuccess() {
        val result = Result.Success("payload")
        assertSame(result, result.onSuccess { }.onFailure { })
    }

    @Test
    fun doesNotSwallowExceptionsFromCallers() {
        val failure = IllegalStateException("caller failure")
        assertSame(failure, assertFailsWith<IllegalStateException> {
            Result.Success(Unit).map { throw failure }
        })
    }
}
