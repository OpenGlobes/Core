/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.utils;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Utils {

    /**
     * Test the now's time in the specified range denoted by the range
     * {@code (from, to]}, which is an exclusive begin to the inclusive end.
     *
     * @param now       now'time.
     * @param rangeFrom exclusive begin of the time rane.
     * @param rangeTo   inclusive end of the time range.
     *
     * @return {@code true} is the specifed now is in the time range.
     */
    public static boolean inRange(LocalTime now,
                                  LocalTime rangeFrom,
                                  LocalTime rangeTo) {
        if (rangeFrom.isBefore(rangeTo)) {
            return now.isAfter(rangeFrom) && (!now.isAfter(rangeTo));
        }
        else if (rangeFrom.isAfter(rangeTo)) {
            return now.isAfter(rangeFrom) ||(!now.isAfter(rangeTo));
        } else {
            return false;
        }
    }

    /**
     * Schedule task to run from the begin of next minute, an repeats for every
     * minute.
     *
     * @param task timer task.
     */
    public static void schedulePerMinute(TimerTask task) {
        var now = ZonedDateTime.now();
        var calendar = Calendar.getInstance();
        /*
         * Move time to next minute, may have fractional seconds of a minute.
         */
        now.plusMinutes(1);
        /*
         * Adjust month value to be used by Date/Calendar. Don't give the
         * seconds so it rounds by minute.
         */
        calendar.set(now.getYear(),
                     now.getMonthValue() - 1,
                     now.getDayOfMonth(),
                     now.getHour(),
                     now.getMinute());
        new Timer().schedule(task,
                             calendar.getTime(),
                             TimeUnit.MINUTES.toMillis(1));
    }

    private Utils() {
    }
}
