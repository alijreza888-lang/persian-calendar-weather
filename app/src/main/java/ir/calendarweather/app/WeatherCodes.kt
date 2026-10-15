package ir.calendarweather.app

/**
 * نگاشت کدهای وضعیت هوا (WMO) که Open-Meteo برمی‌گرداند
 * به ایموجی و توضیح فارسی
 */
data class WeatherVisual(val emoji: String, val description: String)

fun weatherVisual(code: Int, isDay: Boolean): WeatherVisual = when (code) {
    0 -> WeatherVisual(if (isDay) "☀️" else "🌙", "صاف")
    1 -> WeatherVisual(if (isDay) "🌤️" else "🌙", "عمدتاً صاف")
    2 -> WeatherVisual("⛅", "نیمه‌ابری")
    3 -> WeatherVisual("☁️", "ابری")
    45, 48 -> WeatherVisual("🌫️", "مه")
    51, 53, 55 -> WeatherVisual("🌦️", "نم‌نم باران")
    56, 57 -> WeatherVisual("🌧️", "نم‌نم باران یخ‌زده")
    61 -> WeatherVisual("🌧️", "باران کم‌شدت")
    63 -> WeatherVisual("🌧️", "باران")
    65 -> WeatherVisual("🌧️", "باران شدید")
    66, 67 -> WeatherVisual("🌧️", "باران یخ‌زده")
    71 -> WeatherVisual("🌨️", "بارش کم‌شدت برف")
    73 -> WeatherVisual("🌨️", "بارش برف")
    75 -> WeatherVisual("❄️", "بارش شدید برف")
    77 -> WeatherVisual("🌨️", "دانه‌های برف")
    80, 81 -> WeatherVisual("🌦️", "رگبار باران")
    82 -> WeatherVisual("⛈️", "رگبار باران شدید")
    85, 86 -> WeatherVisual("🌨️", "رگبار برف")
    95 -> WeatherVisual("⛈️", "رعد و برق")
    96, 99 -> WeatherVisual("⛈️", "رعد و برق با تگرگ")
    else -> WeatherVisual("🌡️", "نامشخص")
}
