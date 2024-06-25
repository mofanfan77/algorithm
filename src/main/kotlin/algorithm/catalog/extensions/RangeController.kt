package org.example.algorithm.catalog.extensions

import catalog.extensions.range.ConstraintEq
import catalog.extensions.range.IRange

interface RangeController {
    fun createLeRange(s: ConstraintEq, value: Double): IRange
}