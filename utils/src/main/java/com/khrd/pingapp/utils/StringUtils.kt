package com.khrd.pingapp.utils

import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit
import android.icu.text.RelativeDateTimeFormatter.Direction
import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

fun validateLetters(txt: String?): Boolean {
    val regx = "^[\\p{L} \\d'-]+\$"
    val pattern: Pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE)
    val matcher: Matcher = pattern.matcher(txt)
    return matcher.find()
}

fun validateOnlyLettersAndSpace(text: String?): Boolean {
    if (text != null) {
        return text.matches(Regex("^[\\p{L} ]+\$"))
    }
    return false
}

fun getDate(millis: Long): String = SimpleDateFormat("dd MMMM, HH:mm", Locale.getDefault()).format(millis)

@RequiresApi(Build.VERSION_CODES.N)
fun getRelativeDate(millis: Long): String {
    if (System.currentTimeMillis() > millis) {
        val relativeDateTimeFormatter = RelativeDateTimeFormatter.getInstance()
        when {
            // Today
            DateUtils.isToday(millis) -> {
                val elapsedTime = System.currentTimeMillis() - millis
                when {
                    // less than 5 minutes (now)
                    elapsedTime < DateUtils.MINUTE_IN_MILLIS * 5 -> {
                        return relativeDateTimeFormatter.format(Direction.PLAIN, AbsoluteUnit.NOW).replaceFirstChar { it.uppercase() }
                    }
                    // less than 5 hours (Today, 45 minutes ago || Today, 3 hours ago)
                    elapsedTime < DateUtils.HOUR_IN_MILLIS * 5 -> {
                        return relativeDateTimeFormatter.format(Direction.THIS, AbsoluteUnit.DAY).replaceFirstChar { it.uppercase() } +
                                ", " +
                                DateUtils.getRelativeTimeSpanString(
                                    millis,
                                    System.currentTimeMillis(),
                                    DateUtils.MINUTE_IN_MILLIS
                                )
                    }
                    // Today, 12:30
                    else -> {
                        return relativeDateTimeFormatter.format(Direction.THIS, AbsoluteUnit.DAY).replaceFirstChar { it.uppercase() } +
                                ", " +
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(millis)
                    }
                }
            }
            // Yesterday, 12:30
            DateUtils.isToday(millis + DateUtils.DAY_IN_MILLIS) -> {
                return relativeDateTimeFormatter.format(Direction.LAST, AbsoluteUnit.DAY).replaceFirstChar { it.uppercase() } +
                        ", " +
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(millis)
            }
            // 1+ days ago (12 June, 12:30)
            else -> {
                return getDate(millis)
            }
        }
    } else {
        return getDate(millis)
    }
}

fun getStringFromUnicode(unicode: Int) = String(Character.toChars(unicode))

fun String.containsDigit() = any { it.isDigit() }

fun String.containsUpperLetter() = any { it.isUpperCase() }

fun String.containsLowerLetter() = any { it.isLowerCase() }

fun String.validateName() = all { it.isLetterOrDigit() }