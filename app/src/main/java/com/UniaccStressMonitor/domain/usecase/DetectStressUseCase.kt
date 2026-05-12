package com.UniaccStressMonitor.domain.usecase

import com.UniaccStressMonitor.domain.model.StressLevel
import kotlin.math.sqrt

/**
 * DetectStressUseCase: Heuristic classifier for Edge AI stress detection.
 * 
 * Logic:
 * 1. Apply a 5-sample median filter to smooth sensor noise.
 * 2. Calculate acceleration magnitude variance.
 * 3. Estimate angular velocity frequency component.
 * 4. Determine stress level based on specified thresholds:
 *    - HIGH_STRESS: Variance > 0.5 m/s² AND angular frequency > 2 Hz for significant duration.
 *    - MEDIUM_STRESS: Variance > 0.3 m/s² or moderate angular frequency.
 *    - LOW_STRESS: Baseline / rest.
 */
class DetectStressUseCase {

    operator fun invoke(
        accelX: FloatArray, accelY: FloatArray, accelZ: FloatArray,
        gyroX: FloatArray, gyroY: FloatArray, gyroZ: FloatArray
    ): StressLevel {
        // 1. Median Filtering (Simplified: 5-sample window)
        val filteredAccelX = medianFilter(accelX)
        val filteredAccelY = medianFilter(accelY)
        val filteredAccelZ = medianFilter(accelZ)

        // 2. Magnitude Calculation
        val magnitude = FloatArray(filteredAccelX.size) { i ->
            sqrt(filteredAccelX[i] * filteredAccelX[i] + 
                 filteredAccelY[i] * filteredAccelY[i] + 
                 filteredAccelZ[i] * filteredAccelZ[i])
        }

        // 3. Variance Calculation
        val variance = calculateVariance(magnitude)

        // 4. Angular Frequency Estimation (Simplified zero-crossing rate as proxy for frequency)
        val gyroFrequency = calculateZeroCrossingRate(gyroX) + 
                             calculateZeroCrossingRate(gyroY) + 
                             calculateZeroCrossingRate(gyroZ)

        // 5. Threshold Logic
        // HIGH_STRESS: variance > 0.5 Y gyroFrequency > threshold (proxy for 2Hz)
        // Note: SENSOR_DELAY_NORMAL is ~5Hz (200ms). 2Hz is a high rate relative to sampling.
        // We will adjust thresholds for the specific sampling rate.
        return when {
            variance > 0.5 && gyroFrequency > 1.5 -> StressLevel.HIGH_STRESS
            variance > 0.25 || gyroFrequency > 0.8 -> StressLevel.MEDIUM_STRESS
            else -> StressLevel.LOW_STRESS
        }
    }

    private fun medianFilter(data: FloatArray): FloatArray {
        if (data.size < 5) return data
        val result = FloatArray(data.size)
        for (i in 2 until data.size - 2) {
            val window = floatArrayOf(data[i-2], data[i-1], data[i], data[i+1], data[i+2])
            window.sort()
            result[i] = window[2]
        }
        // Fill edges
        result[0] = data[0]; result[1] = data[1]
        result[data.size-2] = data[data.size-2]; result[data.size-1] = data[data.size-1]
        return result
    }

    private fun calculateVariance(data: FloatArray): Float {
        if (data.isEmpty()) return 0f
        val mean = data.average().toFloat()
        return data.map { (it - mean) * (it - mean) }.average().toFloat()
    }

    private fun calculateZeroCrossingRate(data: FloatArray): Float {
        if (data.size < 2) return 0f
        var crossings = 0
        for (i in 1 until data.size) {
            if ((data[i-1] > 0 && data[i] <= 0) || (data[i-1] < 0 && data[i] >= 0)) {
                crossings++
            }
        }
        return crossings.toFloat() / data.size
    }
}
