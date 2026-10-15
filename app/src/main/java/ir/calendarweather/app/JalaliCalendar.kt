package ir.calendarweather.app

/**
 * تبدیل تاریخ میلادی <-> هجری شمسی (جلالی)
 * بر پایهٔ الگوریتم دقیق و شناخته‌شدهٔ jalaali-js
 * (تقویم خیام/بورکوفسکی) — بدون خطا برای سال‌های ۱۱۷۸ تا ۳۳۴۳ شمسی
 */
object JalaliCalendar {

    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private fun div(a: Int, b: Int): Int = a / b

    /**
     * محاسبهٔ ساختار سال جلالی
     * خروجی: [leap (0 یعنی کبیسه), سال میلادی معادل, روز ۱ فروردین در مارس]
     */
    private fun jalCal(jy: Int): IntArray {
        val gy = jy + 621
        var leapJ = -14
        var jp = BREAKS[0]
        var jump = 0
        var n = 0

        for (i in 1 until BREAKS.size) {
            val jm = BREAKS[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(jump % 33, 4)
            jp = jm
        }
        n = jy - jp

        leapJ += div(n, 33) * 8 + div((n % 33) + 3, 4)
        if (jump % 33 == 4 && jump - n == 4) leapJ += 1

        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG

        if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
        var leap = ((n + 1) % 33 - 1) % 4
        if (leap == -1) leap = 4

        return intArrayOf(leap, gy, march)
    }

    /** آیا سال شمسی کبیسه است؟ */
    fun isLeapJalali(jy: Int): Boolean = jalCal(jy)[0] == 0

    /** تبدیل میلادی به شمسی — خروجی: [سال، ماه، روز] شمسی */
    fun toJalali(gy: Int, gm: Int, gd: Int): IntArray = d2j(g2d(gy, gm, gd))

    /** تبدیل شمسی به میلادی — خروجی: [سال، ماه، روز] میلادی */
    fun toGregorian(jy: Int, jm: Int, jd: Int): IntArray = d2g(j2d(jy, jm, jd))

    /** تعداد روزهای یک ماه شمسی */
    fun monthLength(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isLeapJalali(jy)) 30 else 29
    }

    /** شمارهٔ روز جولیَن از تاریخ میلادی */
    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
            div(153 * ((gm + 9) % 12) + 2, 5) + gd - 34840408
        d = d - div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) + 752
        return d
    }

    /** تاریخ میلادی از شمارهٔ روز جولیَن */
    private fun d2g(jdn: Int): IntArray {
        var j = 4 * jdn + 139361631
        j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = div(j % 1461, 4) * 5 + 308
        val gd = div(i % 153, 5) + 1
        val gm = (div(i, 153) % 12) + 1
        val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
        return intArrayOf(gy, gm, gd)
    }

    /** شمارهٔ روز جولیَن از تاریخ شمسی */
    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r[1], 3, r[2]) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
    }

    /** تاریخ شمسی از شمارهٔ روز جولیَن */
    private fun d2j(jdn: Int): IntArray {
        val gy = d2g(jdn)[0]
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(gy, 3, r[2])
        var k = jdn - jdn1f

        if (k >= 0) {
            if (k <= 185) {
                // ۶ ماه اول سال (۳۱ روزه)
                val jm = 1 + div(k, 31)
                val jd = (k % 31) + 1
                return intArrayOf(jy, jm, jd)
            }
            k -= 186
        } else {
            // سال شمسی قبل
            jy -= 1
            k += 179
            if (r[0] == 1) k += 1
        }
        val jm = 7 + div(k, 30)
        val jd = (k % 30) + 1
        return intArrayOf(jy, jm, jd)
    }
}
