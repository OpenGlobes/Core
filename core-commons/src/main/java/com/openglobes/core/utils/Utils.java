/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.utils;

import java.io.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Utils {

    private static final AtomicLong AUTO_INC     = new AtomicLong(0);
    private static final AtomicLong EXECUTION_ID = new AtomicLong();

    private Utils() {
    }

    @SuppressWarnings("unchecked")

    public static <T> T copy(T copied) {
        try (ByteArrayOutputStream bo = new ByteArrayOutputStream()) {
            new ObjectOutputStream(bo).writeObject(copied);
            return (T) new ObjectInputStream(
                    new ByteArrayInputStream(bo.toByteArray())).readObject();
        } catch (IOException | ClassNotFoundException ignored) {
            return null;
        }
    }

    public static ZonedDateTime getAlignByMinute() {
        ZonedDateTime n = ZonedDateTime.now();
        ZonedDateTime r = ZonedDateTime.of(n.getYear(),
                                           n.getMonthValue(),
                                           n.getDayOfMonth(),
                                           n.getHour(),
                                           n.getMinute(),
                                           0,
                                           0,
                                           n.getZone());
        return n.getSecond() >= 30 ? r.plusMinutes(1) : r;
    }

    public synchronized static long getExecutionId() {
        if (EXECUTION_ID.get() == 0L) {
            var n = nextUuid().getLeastSignificantBits() >> 32;
            EXECUTION_ID.set(n);
        }
        return EXECUTION_ID.get();
    }

    /**
     * Test the now's time in the specified range denoted by the range
     * {@code (from, to]}, which is an exclusive begin to the inclusive end.
     *
     * @param now       now'time.
     * @param rangeFrom exclusive begin of the time rane.
     * @param rangeTo   inclusive end of the time range.
     * @return {@code true} is the specifed now is in the time range.
     */
    public static boolean inRange(LocalTime now,
                                  LocalTime rangeFrom,
                                  LocalTime rangeTo) {
        if (rangeFrom.isBefore(rangeTo)) {
            return now.isAfter(rangeFrom) && (!now.isAfter(rangeTo));
        } else if (rangeFrom.isAfter(rangeTo)) {
            return now.isAfter(rangeFrom) || (!now.isAfter(rangeTo));
        } else {
            return false;
        }
    }

    /**
     * Get incremental ID.
     *
     * @return auto-incremental ID
     */
    public static Long nextId() {
        return (getExecutionId() << 32) + AUTO_INC.incrementAndGet();
    }

    /**
     * @return
     */
    public static UUID nextUuid() {
        return UUID.randomUUID();
    }

    /**
     * Schedule task to run from the begin of next time when some durations past
     * since the begin of day, and repeats for every duration.
     *
     * @param task     timer task.
     * @param duration duration between two tasks
     * @return timer.
     */
    public static Timer schedulePerDuration(TimerTask task, Duration duration) {
        var now      = ZonedDateTime.now();
        var calendar = Calendar.getInstance();
        /*
         * Move time to next begin, may have fractional seconds of a minute.
         */
        var s = ZonedDateTime.of(now.toLocalDate(),
                                 LocalTime.of(0, 0),
                                 now.getZone());
        while (s.isBefore(now)) {
            s = s.plus(duration);
        }
        /*
         * Adjust month value to be used by Date/Calendar. Don't give the
         * seconds so it rounds by minute.
         */
        calendar.set(s.getYear(),
                     s.getMonthValue() - 1,
                     s.getDayOfMonth(),
                     s.getHour(),
                     s.getMinute(),
                     s.getSecond());
        var r = new Timer();
        r.schedule(task,
                   calendar.getTime(),
                   duration.toMillis());
        return r;
    }
}
