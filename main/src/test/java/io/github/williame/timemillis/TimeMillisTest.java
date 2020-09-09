package io.github.williame.timemillis;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.*;

public class TimeMillisTest
{
    @Test
    public void test()
    {
        Collection<String> vectors = new ArrayList<>(Arrays.asList(
                "2038-01-04T06:31:08.892Z",
                "2030-03-03T17:57:17.740Z",
                "2020-02-12T03:30:20.034Z",
                "2020-02-12T13:30:20.034Z",
                "2020-02-29T13:30:20Z",
                "2020-02-29T13:30:20.034Z"));
        Random random = new Random();
        for (int i = 0; i < 100_000; i++) {
            vectors.add(Instant.ofEpochMilli(TimeMillis.nextRandomTimestamp(random)).toString());
        }
        for (String vector: vectors) {
            Instant instant = Instant.parse(vector);
            ZonedDateTime dateTime = instant.atZone(ZoneOffset.UTC);
            long timestamp = TimeMillis.parse(vector);
            assertEquals(vector,
                    instant.toEpochMilli(),
                    timestamp);
            assertEquals(vector,
                    instant.toString());
            assertEquals(vector,
                    TimeMillis.toIsoString(timestamp));
            Assert.assertEquals(vector,
                    instant.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).toInstant().toEpochMilli(),
                    TimeMillis.truncateToMonths(timestamp));
            Assert.assertEquals(vector,
                    instant.truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
                    TimeMillis.truncateToDays(timestamp));
            Assert.assertEquals(vector,
                    instant.truncatedTo(ChronoUnit.HALF_DAYS).toEpochMilli(),
                    TimeMillis.truncateToHours(timestamp, 12));
            Assert.assertEquals(vector,
                    instant.truncatedTo(ChronoUnit.HOURS).toEpochMilli(),
                    TimeMillis.truncateToHours(timestamp));
            Assert.assertEquals(vector,
                    instant.truncatedTo(ChronoUnit.MINUTES).toEpochMilli(),
                    TimeMillis.truncateToMinutes(timestamp));
            Assert.assertEquals(vector,
                    instant.truncatedTo(ChronoUnit.SECONDS).toEpochMilli(),
                    TimeMillis.truncatedToSeconds(timestamp));
            Assert.assertEquals(vector,
                    ChronoUnit.DAYS.between(Instant.EPOCH, instant),
                    TimeMillis.dayOfEpoch(timestamp));
            Assert.assertEquals(vector,
                    dateTime.getDayOfYear(),
                    TimeMillis.dayOfYear(timestamp));
            Assert.assertEquals(vector,
                    dateTime.getDayOfMonth(),
                    TimeMillis.dayOfMonth(timestamp));
            Assert.assertEquals(vector,
                    dateTime.getDayOfWeek().getValue(),
                    TimeMillis.dayOfWeek(timestamp));
            Assert.assertEquals(vector,
                    dateTime.getYear(),
                    TimeMillis.getYear(timestamp));
            Assert.assertEquals(vector,
                    dateTime.getMonthValue(),
                    TimeMillis.getMonth(timestamp));
            Assert.assertEquals(vector,
                    dateTime.getHour(),
                    TimeMillis.getHour(timestamp));
            Assert.assertEquals(vector,
                    dateTime.getMinute(),
                    TimeMillis.getMinute(timestamp));
            Assert.assertEquals(vector,
                    dateTime.getSecond(),
                    TimeMillis.getSecond(timestamp));
            Assert.assertEquals(vector,
                    instant.get(ChronoField.MILLI_OF_SECOND),
                    TimeMillis.getMilliseconds(timestamp));
            Assert.assertEquals(vector,
                    instant.getNano(),
                    TimeMillis.getNanoseconds(timestamp));
        }
    }
}