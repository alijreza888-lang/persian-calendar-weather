package ir.calendarweather.app

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ir.calendarweather.app.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
import java.util.Calendar
import kotlin.concurrent.thread

/**
 * اکتیویتی اصلی: نمایش تقویم شمسی + میلادی + قمری، رویدادهای امروز
 * و آب‌وهوای زندهٔ شهر انتخابی از سرویس رایگان Open-Meteo
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        renderCalendar()
        renderEvents()

        binding.btnPickCity.setOnClickListener { showCityPicker() }
        binding.btnRetry.setOnClickListener { fetchWeather() }
        binding.swipeRefresh.setOnRefreshListener { fetchWeather() }
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.primary)
        )

        fetchWeather()
    }

    /* ==================== تقویم ==================== */

    private fun renderCalendar() {
        val now = Calendar.getInstance()
        val gy = now.get(Calendar.YEAR)
        val gm = now.get(Calendar.MONTH) + 1
        val gd = now.get(Calendar.DAY_OF_MONTH)

        val (jy, jm, jd) = JalaliCalendar.toJalali(gy, gm, gd)

        binding.textWeekday.text = weekdayName(now.get(Calendar.DAY_OF_WEEK))
        binding.textJalali.text = "${faNumber(jd)} ${JALALI_MONTHS[jm - 1]} ${faNumber(jy)}"
        binding.textGregorian.text =
            "میلادی: ${faNumber(gd)} ${GREGORIAN_MONTHS_FA[gm - 1]} ${faNumber(gy)}"
        binding.textHijri.text = hijriToday()

        val dayOfYear = (1 until jm).sumOf { JalaliCalendar.monthLength(jy, it) } + jd
        binding.textDayinfo.text =
            "روز ${faNumber(dayOfYear)} از سال ${faNumber(jy)}" +
            " • ${JALALI_MONTHS[jm - 1]} ${faNumber(JalaliCalendar.monthLength(jy, jm))} روزه"
    }

    /** تاریخ امروز به تقویم هجری قمری (با استفاده از ICU داخلی اندروید) */
    private fun hijriToday(): String {
        return try {
            val ic = android.icu.util.IslamicCalendar()
            ic.timeInMillis = System.currentTimeMillis()
            val d = ic.get(android.icu.util.Calendar.DAY_OF_MONTH)
            val m = ic.get(android.icu.util.Calendar.MONTH)
            val y = ic.get(android.icu.util.Calendar.YEAR)
            "قمری: ${faNumber(d)} ${HIJRI_MONTHS[m]} ${faNumber(y)}"
        } catch (e: Exception) {
            ""
        }
    }

    private fun renderEvents() {
        val now = Calendar.getInstance()
        val (jy, jm, jd) = JalaliCalendar.toJalali(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH) + 1,
            now.get(Calendar.DAY_OF_MONTH)
        )
        val event = JALALI_EVENTS[jm * 100 + jd]
        binding.textEvent.text = event ?: getString(R.string.no_event)
    }

    /* ==================== انتخاب شهر ==================== */

    private fun showCityPicker() {
        val names = Cities.ALL.map { it.name }.toTypedArray()
        val current = prefs.getInt(KEY_CITY, 0).coerceIn(0, names.size - 1)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.pick_city))
            .setSingleChoiceItems(names, current) { dialog, which ->
                prefs.edit().putInt(KEY_CITY, which).apply()
                dialog.dismiss()
                fetchWeather()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /* ==================== آب‌وهوا ==================== */

    private fun fetchWeather() {
        val index = prefs.getInt(KEY_CITY, 0).coerceIn(0, Cities.ALL.size - 1)
        val city = Cities.ALL[index]
        binding.textCity.text = city.name

        // حالت بارگذاری
        binding.progress.visibility = View.VISIBLE
        binding.weatherContent.visibility = View.GONE
        binding.weatherError.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = true

        thread {
            try {
                val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=${city.lat}&longitude=${city.lon}" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                    "&timezone=auto&forecast_days=4"

                val json = URL(url).readText()
                val root = JSONObject(json)

                // ---- وضعیت فعلی ----
                val current = root.getJSONObject("current")
                val code = current.getInt("weather_code")
                val isDay = current.optInt("is_day", 1) == 1
                val visual = weatherVisual(code, isDay)
                val temp = current.getDouble("temperature_2m")
                val feels = current.getDouble("apparent_temperature")
                val humidity = current.getInt("relative_humidity_2m")
                val wind = current.getDouble("wind_speed_10m")
                val updated = current.optString("time", "")

                // ---- پیش‌بینی روزهای آینده ----
                val daily = root.getJSONObject("daily")
                val times = daily.getJSONArray("time")
                val codes = daily.getJSONArray("weather_code")
                val maxes = daily.getJSONArray("temperature_2m_max")
                val mins = daily.getJSONArray("temperature_2m_min")

                val forecast = ArrayList<DayForecast>(times.length())
                for (i in 0 until times.length()) {
                    forecast.add(
                        DayForecast(
                            date = times.getString(i),
                            code = codes.getInt(i),
                            max = maxes.getDouble(i),
                            min = mins.getDouble(i)
                        )
                    )
                }

                runOnUiThread {
                    binding.progress.visibility = View.GONE
                    binding.weatherError.visibility = View.GONE
                    binding.weatherContent.visibility = View.VISIBLE

                    binding.textIcon.text = visual.emoji
                    binding.textCondition.text = visual.description
                    binding.textTemp.text = "${faNumber(temp.roundInt())}°"
                    binding.textFeels.text = "دمای احساسی: ${faNumber(feels.roundInt())}°"
                    binding.textDetails.text =
                        "رطوبت: ${faNumber(humidity)}٪  •  باد: ${faNumber(wind.roundInt())} km/h"
                    binding.textUpdated.text =
                        if (updated.length >= 16) {
                            "آخرین به‌روزرسانی: ${faNumber(updated.substring(11, 16))}"
                        } else ""

                    renderForecast(forecast)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progress.visibility = View.GONE
                    binding.weatherContent.visibility = View.GONE
                    binding.weatherError.visibility = View.VISIBLE
                }
            } finally {
                runOnUiThread { binding.swipeRefresh.isRefreshing = false }
            }
        }
    }

    private fun renderForecast(days: List<DayForecast>) {
        binding.forecastRow.removeAllViews()
        days.forEachIndexed { index, day ->
            val label = when (index) {
                0 -> "امروز"
                1 -> "فردا"
                else -> weekdayOfIsoDate(day.date)
            }
            val visual = weatherVisual(day.code, true)

            val view = layoutInflater.inflate(
                R.layout.item_forecast, binding.forecastRow, false
            ) as LinearLayout
            view.findViewById<TextView>(R.id.text_day).text = label
            view.findViewById<TextView>(R.id.text_f_icon).text = visual.emoji
            view.findViewById<TextView>(R.id.text_range).text =
                "${faNumber(day.max.roundInt())}°\n${faNumber(day.min.roundInt())}°"
            binding.forecastRow.addView(view)
        }
    }

    companion object {
        private const val KEY_CITY = "selected_city"
    }
}

/** پیش‌بینی یک روز */
data class DayForecast(val date: String, val code: Int, val max: Double, val min: Double)
