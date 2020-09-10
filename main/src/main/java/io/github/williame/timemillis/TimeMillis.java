package io.github.williame.timemillis;

import java.util.Random;

public final class TimeMillis
{
    private TimeMillis() {}

    // Returns elapsed months * 32 + dayOfMonth
    public static int toMonthAndDays(int yearAndDays) {
        int daysLeft = yearAndDays & 511;
        // If needed: Adjust for leap year:
        if((yearAndDays >> 9) % 4 == 0) {
            return DAYS_TO_ISO_MONTH_DAY_LEAP[daysLeft];
        }
        return DAYS_TO_ISO_MONTH_DAY[daysLeft];
    }

    // Returns year * 512 + daysInYear
    public static int toYearAndDays(long timestamp) {
        int dayOfEpoch = dayOfEpoch(timestamp);

        int passedLeapCycles = (dayOfEpoch / 1461);
        int passedDaysCurrentCycle = (dayOfEpoch % 1461);

        // Adjust running leap year cycle:
        int year = MIN_YEAR + passedLeapCycles * 4;
        if(passedDaysCurrentCycle >= 1096) { // 365 + 365 + 366
            return (year+3) << 9 | (passedDaysCurrentCycle - 1096);
        } else if(passedDaysCurrentCycle >= 730) { // 365 + 365
            return (year+2) << 9 | (passedDaysCurrentCycle - 730);
        } else if(passedDaysCurrentCycle >= 365) { // 365
            return (year+1) << 9 | (passedDaysCurrentCycle - 365);
        } else {
            return year << 9 | passedDaysCurrentCycle;
        }
    }

    public static String toIsoString(long timestamp) {
        int yearAndDays = toYearAndDays(timestamp);
        int monthAndDays = toMonthAndDays(yearAndDays);

        int timeRemainder = (int) (timestamp % MILLIS_IN_DAY);
        final int millis = timeRemainder % 1000;
        final int secs = (timeRemainder /= 1000) % 60;
        final int minutes = (timeRemainder /= 60) % 60;
        final int hours = (timeRemainder / 60) % 24;

        char[] chars = new char[24];
        chars[4] = chars[7] = '-';
        chars[10] = 'T';
        chars[13] = chars[16] = ':';
        emit(chars, (yearAndDays >> 9), 0, 4);
        emit(chars, 1 + (monthAndDays >> 5), 5, 7);
        emit(chars, 1 + (monthAndDays & 31), 8, 10);
        emit(chars, hours, 11, 13);
        emit(chars, minutes, 14, 16);
        emit(chars, secs, 17, 19);
        if (millis > 0) {
            chars[19] = '.';
            emit(chars, millis, 20, 23);
            chars[23] = 'Z';
            return new String(chars, 0, 24);
        } else {
            chars[19] = 'Z';
            return new String(chars, 0, 20);
        }
    }

    private static void emit(char[] chars, long num, int start, int stop) {
        for (int i = stop - 1; i >= start; i--) {
            long nextNum = num / 10;
            chars[i] = (char)('0' + (num - nextNum * 10));
            num = nextNum;
        }
    }

    public static long parse(CharSequence timestamp) {
        return parse(timestamp, 0, timestamp.length());
    }

    public static long parse(CharSequence timestamp, int begin, int end) {
        final int year, month, day, hour, minute, second, millis;
        year = parseInt(timestamp, begin, Math.min(begin + 4, end));
        int gap = timestamp.charAt(begin + 4) == '-'? 1: 0;
        if (gap == 1 && timestamp.charAt(end - 1) == 'Z') {
            end--;
        }
        begin += 4 + gap;
        month = parseInt(timestamp, begin, Math.min(begin + 2, end));
        begin += 2 + gap;
        day = parseInt(timestamp, begin, Math.min(begin + 2, end));
        begin += 2 + gap;
        hour = (begin + 2 <= end)? parseInt(timestamp, begin, begin + 2): 0;
        begin += 2 + gap;
        minute = (begin + 2 <= end)? parseInt(timestamp, begin, begin + 2): 0;
        begin += 2 + gap;
        second = (begin + 2 <= end)? parseInt(timestamp, begin, begin + 2): 0;
        begin += 2 + gap;
        millis = (begin + 3 <= end)? parseInt(timestamp, begin, begin + 3): 0;
        return of(year, month, day, hour, minute, second, millis);
    }

    private static int parseInt(CharSequence s, int begin, int end) {
        int ret = 0;
        for (int i = begin; i < end; i++) {
            ret *= 10;
            ret += s.charAt(i) - '0';
        }
        return ret;
    }

    public static long of(int year, int month, int day, int hour, int minute, int second, int millis) {
        long timestamp =
                (MONTHS[(year - MIN_YEAR) * 12 + month - 1] >> TIMESTAMP_SHIFT) +
                (long)(day - 1) * MILLIS_IN_DAY +
                hour * MILLIS_IN_HOUR +
                minute * MILLIS_IN_MINUTE +
                second * MILLIS +
                millis;
        assert timestamp >= 0 && timestamp < MAX_TIMESTAMP: timestamp;
        return timestamp;
    }

    public static long of(int year, int month, int day, int hour, int minute, int second) {
        return of(year, month, day, hour, minute, second, 0);
    }

    public static long of(int year, int month, int day) {
        return of(year, month, day, 0, 0, 0, 0);
    }

    public static long truncateToMonths(long timestamp) {
        int yearAndDays = toYearAndDays(timestamp);
        int monthAndDays = toMonthAndDays(yearAndDays);
        return of(yearAndDays >> 9, 1 + ( monthAndDays >> 5), 1, 0, 0, 0, 0);
    }

    public static long truncateToDays(long timestamp) {
        return  timestamp / MILLIS_IN_DAY * MILLIS_IN_DAY;
    }

    public static long truncateToHours(long timestamp) {
        return  timestamp / MILLIS_IN_HOUR * MILLIS_IN_HOUR;
    }

    public static long truncateToHours(long timestamp, int numOfHours) {
        assert 24 / numOfHours * numOfHours == 24: "should not truncate to unequal number of hours";
        return timestamp / (MILLIS_IN_HOUR * numOfHours) * MILLIS_IN_HOUR * numOfHours;
    }

    public static long truncateToMinutes(long timestamp) {
        return timestamp / MILLIS_IN_MINUTE * MILLIS_IN_MINUTE;
    }

    public static long truncateToMinutes(long timestamp, int numOfMinutes) {
        assert 60 / numOfMinutes * numOfMinutes == 60: "should not truncate to unequal number of minutes";
        return timestamp / (MILLIS_IN_MINUTE * numOfMinutes) * MILLIS_IN_MINUTE * numOfMinutes;
    }

    public static long truncatedToSeconds(long timestamp) {
        return timestamp / 1000 * 1000;
    }

    public static int dayOfEpoch(long timestamp) {
        return (int)(timestamp / MILLIS_IN_DAY);
    }

    // 1 to 365 (or 366 in a leap year)
    public static int dayOfYear(long timestamp) {
        return (toYearAndDays(timestamp) & 511) + 1;
    }

    // 1 to 7 (Monday to Sunday) following the ISO-8601 standard
    // Compatible with java.time.DayOfWeek.of()
    public static int dayOfWeek(long timestamp) {
        // 1970-01-01 was a Thursday
        return (dayOfEpoch(timestamp) + 3) % 7 + 1;
    }

    // 1 to 31
    public static int dayOfMonth(long timestamp) {
        return 1 + (toMonthAndDays(toYearAndDays(timestamp)) & 31);
    }

    public static int getYear(long timestamp) {
        return toYearAndDays(timestamp) >> 9;
    }

    // 1 to 12
    public static int getMonth(long timestamp) {
        return 1 + (toMonthAndDays(toYearAndDays(timestamp)) >> 5);
    }

    // 0 to 23
    public static int getHour(long timestamp) {
        return (int)((timestamp % MILLIS_IN_DAY) / MILLIS_IN_HOUR);
    }

    // 0 to 59
    public static int getMinute(long timestamp) {
        return (int)((timestamp % MILLIS_IN_HOUR) / MILLIS_IN_MINUTE);
    }

    // 0 to 59
    public static int getSecond(long timestamp) {
        return (int)((timestamp % MILLIS_IN_MINUTE) / MILLIS);
    }

    // 0 to 9999
    public static int getMilliseconds(long timestamp) {
        return (int)(timestamp % MILLIS);
    }

    // 0 to 999_999_999
    public static int getNanoseconds(long timestamp) {
        return getMilliseconds(timestamp) * 1_000_000;
    }

    private static final int
            YEAR_SHIFT = 5,
            TIMESTAMP_SHIFT = YEAR_SHIFT + 11,
            MIN_YEAR = 1970,
            MAX_YEAR = 2038;
    private static final long
            MILLIS = 1000,
            MILLIS_IN_MINUTE = 60 * MILLIS,
            MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60,
            MILLIS_IN_DAY = MILLIS_IN_HOUR * 24,
            MONTH_MASK = 0xfL,
            YEAR_MASK = 0x7ffL << YEAR_SHIFT,
            MAX_TIMESTAMP = (long)Integer.MAX_VALUE * 1000 + 999;

    private static final int[] DAYS_IN_MONTH = new int[]{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    private static final int[] DAYS_IN_MONTH_LEAP = new int[]{ 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    /**
     * Lookup table to transform a single day into the correct ISO 31-day month
     */
    private static final int[] DAYS_TO_ISO_MONTH_DAY = new int[365];
    private static final int[] DAYS_TO_ISO_MONTH_DAY_LEAP = new int[366];
    static {
        int ptr1 = 0;
        int ptr2 = 0;
        for(int month = 0; month < DAYS_IN_MONTH.length; month++) {
            for(int x = 0; x < DAYS_IN_MONTH[month]; x++) {
                DAYS_TO_ISO_MONTH_DAY[ptr1++] = month << 5 | x;
            }
            for(int x = 0; x < DAYS_IN_MONTH_LEAP[month]; x++) {
                DAYS_TO_ISO_MONTH_DAY_LEAP[ptr2++] = month << 5 | x;
            }
        }
    }


    private static final long[] MONTHS;
    static {
        final int[] daysInMonth = new int[] {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        long nextMonth = 0;
        MONTHS = new long[(MAX_YEAR - MIN_YEAR) * 12 + 1];
        for (int year = MIN_YEAR, i = 0; year < MAX_YEAR; year++) {
            boolean isLeapYear = year % 4 == 0;
            for (int month = 0; month < 12; month++) {
                final long timestamp = nextMonth,
                    encoded = (timestamp << TIMESTAMP_SHIFT) |  (year << YEAR_SHIFT) | month;
                assert (encoded >> TIMESTAMP_SHIFT) == timestamp;
                assert (int)((encoded & YEAR_MASK) >> YEAR_SHIFT) == year;
                assert (int)(encoded & MONTH_MASK) == month;
                MONTHS[i++] = encoded;
                nextMonth += (daysInMonth[month] + (isLeapYear && month == 1? 1: 0)) * MILLIS_IN_DAY;
            }
        }
        MONTHS[MONTHS.length - 1] = (nextMonth << TIMESTAMP_SHIFT) | (MAX_YEAR << YEAR_SHIFT);  // Jan 2038
    }


    public static long nextRandomTimestamp(Random random) {
        return Math.abs(random.nextLong()) % MAX_TIMESTAMP;
    }
}
