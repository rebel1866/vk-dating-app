package com.melnikov.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class DateUtil {
    public static short parseAge(String bDate) throws DateTimeException {
        if (bDate == null) {
            throw new DateTimeException("bDate is null");
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate birthDate = LocalDate.parse(bDate, dateTimeFormatter);
        return (short) Math.abs(Period.between(LocalDate.now(), birthDate).getYears());
    }

    public static LocalDateTime unixToLocalDateTime(long unix) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(unix),
                TimeZone.getDefault().toZoneId());
    }
}
