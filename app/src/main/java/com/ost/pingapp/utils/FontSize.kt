package com.khrd.pingapp.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class FontSize(
    val default: TextUnit = 0.sp,
    val extraSmall: TextUnit = 10.sp,
    val small: TextUnit = 12.sp,
    val medium: TextUnit = 14.sp,
    val large: TextUnit = 16.sp,
    val extraLarge: TextUnit = 30.sp,
)

val LocalFontSize = compositionLocalOf { FontSize() }