package com.example.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testuje wydajność przewijania listy trenerów na ekranie głównym.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollHomeScreenList() = benchmarkRule.measureRepeated(
        packageName = "com.example.myapplication",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
    ) {

        startActivityAndWait()

        val list = device.findObject(By.res("trainer_list"))

        list.setGestureMargin(device.displayWidth / 5)
        list.fling(Direction.DOWN)
    }
}
