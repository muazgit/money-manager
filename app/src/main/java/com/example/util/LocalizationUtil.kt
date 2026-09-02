package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppLanguage {
    BENGALI,
    ENGLISH
}

object LocalizationUtil {
    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    val bengaliMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatMoney(
        amount: Double,
        isBengaliDigits: Boolean = true
    ): String {
        val df = DecimalFormat("#,##0")
        val formatted = df.format(amount)
        val withSymbol = "৳ $formatted"
        return if (isBengaliDigits) toBengaliDigits(withSymbol) else withSymbol
    }

    fun formatMoneyWithoutSymbol(
        amount: Double,
        isBengaliDigits: Boolean = true
    ): String {
        val df = DecimalFormat("#,##0")
        val formatted = df.format(amount)
        return if (isBengaliDigits) toBengaliDigits(formatted) else formatted
    }

    fun formatDate(
        timestamp: Long,
        isBengali: Boolean = true,
        isBengaliDigits: Boolean = true
    ): String {
        val now = Calendar.getInstance()
        val itemCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = now.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - itemCal.get(Calendar.DAY_OF_YEAR) == 1

        if (isToday) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val timeStr = timeFormat.format(Date(timestamp))
            return if (isBengali) {
                val bnTime = if (isBengaliDigits) toBengaliDigits(timeStr) else timeStr
                "আজ, $bnTime"
            } else {
                "Today, $timeStr"
            }
        }

        if (isYesterday) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val timeStr = timeFormat.format(Date(timestamp))
            return if (isBengali) {
                val bnTime = if (isBengaliDigits) toBengaliDigits(timeStr) else timeStr
                "গতকাল, $bnTime"
            } else {
                "Yesterday, $timeStr"
            }
        }

        val day = itemCal.get(Calendar.DAY_OF_MONTH)
        val monthIdx = itemCal.get(Calendar.MONTH)
        val year = itemCal.get(Calendar.YEAR)

        return if (isBengali) {
            val monthName = bengaliMonths.getOrElse(monthIdx) { "" }
            val dayStr = if (isBengaliDigits) toBengaliDigits(day.toString()) else day.toString()
            val yearStr = if (isBengaliDigits) toBengaliDigits(year.toString()) else year.toString()
            "$dayStr $monthName, $yearStr"
        } else {
            val format = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
            format.format(Date(timestamp))
        }
    }

    fun getCurrentMonthYearString(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    fun getCurrentMonthName(isBengali: Boolean): String {
        val cal = Calendar.getInstance()
        val monthIdx = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        return if (isBengali) {
            "${bengaliMonths[monthIdx]} ${toBengaliDigits(year.toString())}"
        } else {
            val format = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            format.format(Date())
        }
    }
}
