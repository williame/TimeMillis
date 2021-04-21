package io.github.williame.timemillis;

public final class TimeSecs {

    private TimeSecs() {}

    public static StringBuilder toDateTime(int secs, int millis, StringBuilder out) {
        return toDateTime(secs, millis, ' ', out);
    }

    public static StringBuilder toIsoDateTime(int secs, int millis, StringBuilder out) {
        return toDateTime(secs, millis, 'T', out).append('Z');
    }

    private static StringBuilder toDateTime(int secs, int millis, char sep, StringBuilder out) {
        char[] chars = new char[24];
        return out.append(chars, 0, doToDateTime(chars, secs, millis, sep));
    }

    static int doToDateTime(char[] chars, int secs, int millis, char sep) {
        doToYYYYmmdd(chars, TimeMillis.toYearAndDays(dayOfEpoch(secs)));
        chars[10] = sep;
        doToHHMMSS(chars, 11, secs);
        if (millis > 0) {
            chars[19] = '.';
            emit(chars, millis, 20, 23);
            return 23;
        }
        return 19;
    }

    public static StringBuilder toDate(int secs, StringBuilder out) {
        char[] chars = new char[10];
        doToYYYYmmdd(chars, TimeMillis.toYearAndDays(dayOfEpoch(secs)));
        return out.append(chars);
    }

    public static StringBuilder toTime(int secs, StringBuilder out) {
        char[] chars = new char[8];
        doToHHMMSS(chars, 0, secs);
        return out.append(chars);
    }

    private static void emit(char[] chars, long num, int start, int stop) {
        for (int i = stop - 1; i >= start; i--) {
            long nextNum = num / 10;
            chars[i] = (char)('0' + (num - nextNum * 10));
            num = nextNum;
        }
    }

    private static void doToYYYYmmdd(char[] chars, int yearAndDays) {
        int monthAndDays = TimeMillis.toMonthAndDays(yearAndDays);
        chars[4] = chars[7] = '-';
        emit(chars, (yearAndDays >> 9), 0, 4);
        emit(chars, 1 + (monthAndDays >> 5), 5, 7);
        emit(chars, 1 + (monthAndDays & 31), 8, 10);
    }

    private static void doToHHMMSS(char[] chars, int ofs, int secs) {
        chars[ofs + 2] = chars[ofs + 5] = ':';
        final int seconds = secs % 60;
        final int minutes = (secs /= 60) % 60;
        final int hours = (secs / 60) % 24;
        emit(chars, hours, ofs, ofs + 2);
        emit(chars, minutes, ofs + 3, ofs + 5);
        emit(chars, seconds, ofs + 6, ofs + 8);
    }

    public static int dayOfEpoch(int secs) {
        return secs / SECS_IN_DAY;
    }

    static final int
            SECS_IN_MINUTE = 60,
            SECS_IN_HOUR = SECS_IN_MINUTE * 60,
            SECS_IN_DAY = SECS_IN_HOUR * 24;
}
