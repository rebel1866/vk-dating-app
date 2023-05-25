package com.melnikov.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class DateUtil {
    public static short parseAge(String bDate) throws DateTimeException {
        if (bDate == null) {
            throw new DateTimeException("bDate is null");
        }
        String[] arr = bDate.split("\\.");
        if (arr.length != 3) {
            throw new DateTimeException("Year is not defined");
        }
        String day = arr[0];
        String month = arr[1];
        if (day.length() == 1) {
            day = "0" + day;
        }
        if (month.length() == 1) {
            month = "0" + month;
        }
        bDate = String.format("%s.%s.%s", day, month, arr[2]);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate birthDate = LocalDate.parse(bDate, dateTimeFormatter);
        return (short) Math.abs(Period.between(LocalDate.now(), birthDate).getYears());
    }

    public static LocalDateTime unixToLocalDateTime(long unix) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(unix),
                TimeZone.getDefault().toZoneId());
    }
}
