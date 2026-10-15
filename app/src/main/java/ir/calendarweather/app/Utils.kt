package ir.calendarweather.app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/* ================= اعداد و نام‌های فارسی ================= */

/** تبدیل ارقام انگلیسی به فارسی */
fun faNumber(input: String): String = buildString {
    for (c in input) append(if (c in '0'..'9') ('۰' + (c - '0')) else c)
}

fun faNumber(n: Int): String = faNumber(n.toString())

fun Double.roundInt(): Int = Math.round(this).toInt()

/** نام ماه‌های شمسی */
val JALALI_MONTHS = arrayOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
)

/** نام ماه‌های میلادی به فارسی */
val GREGORIAN_MONTHS_FA = arrayOf(
    "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
    "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
)

/** نام ماه‌های قمری */
val HIJRI_MONTHS = arrayOf(
    "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
    "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
)

/** نام روز هفته از روی ثابت‌های Calendar */
fun weekdayName(dayOfWeek: Int): String = when (dayOfWeek) {
    Calendar.SATURDAY -> "شنبه"
    Calendar.SUNDAY -> "یکشنبه"
    Calendar.MONDAY -> "دوشنبه"
    Calendar.TUESDAY -> "سه‌شنبه"
    Calendar.WEDNESDAY -> "چهارشنبه"
    Calendar.THURSDAY -> "پنجشنبه"
    Calendar.FRIDAY -> "جمعه"
    else -> ""
}

/** نام روز هفته از روی تاریخ ISO (مثل 2025-07-24) */
fun weekdayOfIsoDate(date: String): String {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val utc = TimeZone.getTimeZone("UTC")
        fmt.timeZone = utc
        val cal = Calendar.getInstance(utc)
        cal.time = fmt.parse(date)!!
        weekdayName(cal.get(Calendar.DAY_OF_WEEK))
    } catch (e: Exception) {
        ""
    }
}

/* ================= رویدادهای ثابت تقویم شمسی =================
 * کلید: ماه × ۱۰۰ + روز
 * توجه: تعطیلات قمری (مانند عید فطر و عید قربان) هر سال جابه‌جا
 * می‌شوند و در این لیست ثابت گنجانده نشده‌اند.
 */
val JALALI_EVENTS: Map<Int, String> = mapOf(
    101 to "🌱 نوروز — جشن آغاز سال نو (تعطیل رسمی)",
    102 to "🌱 نوروز — تعطیل رسمی",
    103 to "🌱 نوروز — تعطیل رسمی",
    104 to "🌱 نوروز — تعطیل رسمی",
    112 to "🇮🇷 روز جمهوری اسلامی (تعطیل رسمی)",
    113 to "🌳 روز طبیعت — سیزده‌بدر",
    210 to "🌊 روز ملی خلیج فارس",
    211 to "📜 روز بزرگداشت فردوسی و پاسداشت زبان فارسی",
    314 to "🕊️ رحلت امام خمینی (تعطیل رسمی)",
    315 to "✊ سالروز قیام ۱۵ خرداد",
    720 to "📖 روز بزرگداشت حافظ",
    813 to "🎓 ۱۳ آبان — روز ملی مبارزه با استکبار (تعطیل رسمی)",
    824 to "📚 روز کتاب و کتاب‌خوانی",
    916 to "🎓 روز دانشجو",
    930 to "❄️ شب یلدا — بلندترین شب سال",
    1122 to "🇮🇷 سالروز پیروزی انقلاب اسلامی (تعطیل رسمی)",
    1229 to "🛢️ روز ملی شدن صنعت نفت (تعطیل رسمی)"
)
