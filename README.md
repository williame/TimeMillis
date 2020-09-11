# TimeMillis
Very fast Java library for working with timestamps as longs 

_e.g. as returned by_ `System.currentTimeMillis()`

Java 8's Time API comes with the [**Instant**](https://docs.oracle.com/javase/8/docs/api/java/time/Instant.html) class for dealing with timestamps.

However, the Instant class and supporting API can often be excruciatingly slow. 
Its slow to **read** and **write** from strings (a common occurance, e.g. when using timestamps in log files or JSON documents), 
its slow to do **arithmetic** such as **extracting fields** like days or **rounding**,
and, being a Java object, it takes a lot of **memory** and can generate a lot of **garbage**.

Its painfully common to find timestamp operations or timestamp-induced garbage-collection being a substaintial part of 'big data' Java running time.

The TimeMillis library offers helper functions to read, write and operate on timestamps stored as milliseconds in UNIX epoch.

Its actually several orders of magnitude faster to parse an Instant using TimeMillis and then instansiate an Instant from the milliseconds long, 
than it is to use the Instant's parse function directly!

## Benchmark

| Action | Instant code | TimeMillis code | Instant time | TimeMillis time | speedup |
| --- | --- | --- | ---: | ---: | ---: |
| parse string | `Instant.parse(s)` | `TimeMillis.parse(s)` | 1258 ns | 35 ns | 37x |
| to string | `instant.toString()` | `TimeMillis.toString(ts)` | 330 ns | 63 ns | 5x |
| of fields | `OffsetDateTime.of(y, m, d, .., UTC)` `.toInstant()` | `TimeMillis.of(y, m, d, ...)` | 33 ns | 3 ns | 11x |
| get year | `instant.atZone(UTC).getYear()` | `TimeMillis.getYear(ts)` | 39 ns | 2.5 ns | 15x |
| get month | `instant.atZone(UTC).getMonth()` | `TimeMillis.getMonth(ts)` | 39 ns | 4.3 ns | 9x |
| get day | `instant.atZone(UTC).getDayOfMonth()` | `TimeMillis.getDayOfMonth(ts)` | 40 ns | 4.3 ns | 9x |
| get hour | `instant.atZone(UTC).getHour()` | `TimeMillis.getHour(ts)` | 38 ns | 1.3 ns | 29x |
| truncate month | `instant.atZone(UTC).truncatedTo(DAYS)` `.withDayOfMonth(1).toInstant()` | `TimeMillis.truncateToMonths(ts)` | 110 ns | 6.5 ns | 17x |
| truncate day | `instant.truncatedTo(DAYS)` | `TimeMillis.truncateToDays(ts)` | 38 ns | 0.9 ns | 42x |
| truncate hour | `instant.truncatedTo(HOURS)` | `TimeMillis.truncateToHours(ts)` | 38 ns | 0.9 ns | 42x |
