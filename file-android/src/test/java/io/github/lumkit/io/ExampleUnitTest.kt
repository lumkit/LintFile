package io.github.lumkit.io

import android.net.Uri
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun url() {
        println(Uri.decode("content://com.android.externalstorage.documents/tree/primary%3AAAndr%E2%80%8Boid%2Fda%E2%80%8Bta"))
    }
}