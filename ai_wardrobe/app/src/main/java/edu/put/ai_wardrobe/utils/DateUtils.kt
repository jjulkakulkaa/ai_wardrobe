package edu.put.ai_wardrobe.utils
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {

    fun formatDateForDisplay(date: LocalDate): Pair<String, String> {
        val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE")
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val dayOfWeek = date.format(dayOfWeekFormatter)
        val formattedDate = date.format(dateFormatter)
        return Pair(dayOfWeek, formattedDate)
    }

    fun formatDate(date: LocalDate, format: String): String {
        val dateFormatter = DateTimeFormatter.ofPattern(format)
        val formattedDate = date.format(dateFormatter)
        return formattedDate
    }

    fun changeDay(date: LocalDate, offset: Long): LocalDate {
        return date.plusDays(offset)
    }
}
