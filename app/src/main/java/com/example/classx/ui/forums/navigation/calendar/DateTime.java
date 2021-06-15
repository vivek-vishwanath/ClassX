package com.example.classx.ui.forums.navigation.calendar;

import java.util.Date;

public class DateTime {

    int year;
    int month;
    int day;
    int hour;
    int minute;

    public DateTime() {
        Date date = new Date();
        year = date.getYear();
        month = date.getMonth();
        day = date.getDay();
    }

    public Date getDate() {
        return new Date(year - 1900, month, day, hour, minute, 0);
    }
}