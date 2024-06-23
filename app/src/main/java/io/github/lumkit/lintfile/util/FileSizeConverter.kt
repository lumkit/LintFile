package io.github.lumkit.lintfile.util

import java.math.BigDecimal
import java.math.RoundingMode

object FileSizeConverter {

    enum class Unit(val factor: BigDecimal, val displayName: String) {
        B(BigDecimal(1), "B"),
        KB(BigDecimal(1024), "KB"),
        MB(BigDecimal(1024).pow(2), "MB"),
        GB(BigDecimal(1024).pow(3), "GB"),
        TB(BigDecimal(1024).pow(4), "TB"),
        PB(BigDecimal(1024).pow(5), "PB"),
        EB(BigDecimal(1024).pow(6), "EB");
    }

    fun convert(size: BigDecimal, fromUnit: Unit, toUnit: Unit): String {
        val sizeInBytes = size.multiply(fromUnit.factor)
        val convertedSize = sizeInBytes.divide(toUnit.factor, 2, RoundingMode.HALF_UP)
        return "$convertedSize ${toUnit.displayName}"
    }

    fun convert(size: Double, fromUnit: Unit, toUnit: Unit): String {
        return convert(BigDecimal(size), fromUnit, toUnit)
    }

    fun convert(size: Long, fromUnit: Unit, toUnit: Unit): String {
        return convert(BigDecimal(size), fromUnit, toUnit)
    }

    fun autoConvert(size: BigDecimal, fromUnit: Unit): String {
        val sizeInBytes = size.multiply(fromUnit.factor)
        val unit = Unit.entries.reversed().find { sizeInBytes >= it.factor } ?: Unit.B
        val convertedSize = sizeInBytes.divide(unit.factor, 2, RoundingMode.HALF_UP)
        return "$convertedSize ${unit.displayName}"
    }

    fun autoConvert(size: Double, fromUnit: Unit): String {
        return autoConvert(BigDecimal(size), fromUnit)
    }

    fun autoConvert(size: Long, fromUnit: Unit): String {
        return autoConvert(BigDecimal(size), fromUnit)
    }
}