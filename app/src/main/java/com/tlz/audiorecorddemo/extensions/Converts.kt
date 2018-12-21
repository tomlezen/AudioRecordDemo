package com.tlz.audiorecorddemo.extensions

import android.content.Context
import android.util.TypedValue

/**
 * By tomlezen
 * Create at 2018/12/21
 */

fun Context.dp2px(dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()