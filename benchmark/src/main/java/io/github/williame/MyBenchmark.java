/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package io.github.williame;

import io.github.williame.timemillis.TimeMillis;
import org.openjdk.jmh.annotations.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Fork(1)
@State(Scope.Thread)
@OperationsPerInvocation(MyBenchmark.ITERATIONS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 2)
public class MyBenchmark {

    public static final int ITERATIONS = 1000;

    long[] timestamps, timestampsRet;
    Instant[] instants, instantsRet;
    String[] timeStrs, timeStrsRet;
    int[] intsRet;

    @Setup
    public void setup() {
        Random random = new Random();
        timestamps = new long[ITERATIONS];
        instants = new Instant[ITERATIONS];
        timeStrs = new String[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long timestamp = TimeMillis.nextRandomTimestamp(random);
            timestamps[i] = timestamp;
            instants[i] = Instant.ofEpochMilli(timestamp);
            timeStrs[i] = instants[i].toString();
        }
        timestampsRet = new long[ITERATIONS];
        instantsRet = new Instant[ITERATIONS];
        timeStrsRet = new String[ITERATIONS];
        intsRet = new int[ITERATIONS];
    }

    @Benchmark
    public void testParse_Instant() {
        for (int i = 0; i < ITERATIONS; i++) {
            instantsRet[i] = Instant.parse(timeStrs[i]);
        }
    }

    @Benchmark
    public void testParse_Custom_OffsetDateTime() {
        for (int i = 0; i < ITERATIONS; i++) {
            String timeStr = timeStrs[i];
            // this chopping up approach is much faster than using a timestamp formatter and produces much less garbage;
            // previous versions of this function that used a timestamp formatter were pain points identified by profiling
            instantsRet[i] = OffsetDateTime.of(
                    parseInt(timeStr, 0, 4),
                    parseInt(timeStr, 5, 7),
                    parseInt(timeStr, 8, 10),
                    parseInt(timeStr, 11, 13),
                    parseInt(timeStr, 14, 16),
                    parseInt(timeStr, 17, 19),
                    timeStr.charAt(19) == '.'? parseInt(timeStr, 20, 23) * 1_000_000: 0,
                    ZoneOffset.UTC).toInstant();
        }
    }

    private static int parseInt(CharSequence s, int begin, int end) {
        int ret = 0;
        for (int i = begin; i < end; i++) {
            ret *= 10;
            ret += s.charAt(i) - '0';
        }
        return ret;
    }

    @Benchmark
    public void testParse_TimeMillis() {
        for (int i = 0; i < ITERATIONS; i++) {
            timestampsRet[i] = TimeMillis.parse(timeStrs[i]);
        }
    }

    @Benchmark
    public void testToString_Instant() {
        for (int i = 0; i < ITERATIONS; i++) {
            timeStrsRet[i] = instants[i].toString();
        }
    }

    @Benchmark
    public void testToString_TimeMillis() {
        for (int i = 0; i < ITERATIONS; i++) {
            timeStrsRet[i] = TimeMillis.toIsoString(timestamps[i]);
        }
    }

    @Benchmark
    public void testToStringBuilder_TimeMillis() {
        StringBuilder stringBuilder = new StringBuilder(25 * ITERATIONS);
        for (int i = 0; i < ITERATIONS; i++) {
            TimeMillis.toIsoString(timestamps[i], stringBuilder);
        }
        timeStrsRet[0] = stringBuilder.toString();
    }

    @Benchmark
    public void testTruncateDays_Instant() {
        for (int i = 0; i < ITERATIONS; i++) {
            instantsRet[i] = instants[i].truncatedTo(ChronoUnit.DAYS);
        }
    }

    @Benchmark
    public void testTruncateDays_TimeMillis() {
        for (int i = 0; i < ITERATIONS; i++) {
            timestampsRet[i] = TimeMillis.truncateToDays(timestamps[i]);
        }
    }

    @Benchmark
    public void testGetYear_Instant() {
        for (int i = 0; i < ITERATIONS; i++) {
            intsRet[i] = instants[i].atZone(ZoneOffset.UTC).getYear();
        }
    }

    @Benchmark
    public void testGetYear_TimeMillis() {
        for (int i = 0; i < ITERATIONS; i++) {
            intsRet[i] = TimeMillis.getYear(timestamps[i]);
        }
    }

    @Benchmark
    public void testGetMonth_Instant() {
        for (int i = 0; i < ITERATIONS; i++) {
            intsRet[i] = instants[i].atZone(ZoneOffset.UTC).getMonthValue();
        }
    }

    @Benchmark
    public void testGetMonth_TimeMillis() {
        for (int i = 0; i < ITERATIONS; i++) {
            intsRet[i] = TimeMillis.getMonth(timestamps[i]);
        }
    }

    @Benchmark
    public void testTruncateMonth_Instant() {
        for (int i = 0; i < ITERATIONS; i++) {
            instantsRet[i] = instants[i].atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).toInstant();
        }
    }

    @Benchmark
    public void testTruncateMonth_TimeMillis() {
        for (int i = 0; i < ITERATIONS; i++) {
            timestampsRet[i] = TimeMillis.truncateToMonths(timestamps[i]);
        }
    }

}
