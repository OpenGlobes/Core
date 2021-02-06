/*
 * Copyright (C) 2021 Hongbao Chen <chenhongbao@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.openglobes.core.utils;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class Loggers {

    private static final Map<Handler, Object>     handlers = new ConcurrentHashMap<>(64);
    private static final Map<String, Logger>      loggers  = new ConcurrentHashMap<>(64);
    private static final BlockingQueue<LogRecord> logs     = new LinkedBlockingQueue<>();
    private static final SimpleFormatter          format   = new SimpleFormatter();

    private static final Thread d = new Thread(() -> {
        do {
            blockingPublishLogs();
        } while (!Thread.currentThread().isInterrupted());
        flushLogs();
    });

    private static void blockingPublishLogs() {
        try {
            LogRecord l = null;
            while ((l = logs.poll(1, TimeUnit.DAYS)) != null) {
                publishLog(l);
            }
        } catch (InterruptedException ignored) {
        }
    }

    private static void flushLogs() {
        LogRecord l = null;
        while ((l = logs.poll()) != null) {
            publishLog(l);
        }
    }

    private Loggers() {
    }

    public static void addLogHandler(Handler handler) {
        handlers.put(handler, handler);
    }

    public static Logger getLogger(Object name) {
        return loggers.computeIfAbsent(name.toString(),
                                       key -> {
                                           var x = Logger.getLogger(key);
                                           x.setUseParentHandlers(false);
                                           x.addHandler(new Handler() {
                                               @Override
                                               public void publish(LogRecord record) {
                                                   if (!logs.offer(record)) {
                                                       System.err.println("Fail appending new log record.\n"
                                                                          + format.format(record));
                                                   }
                                               }

                                               @Override
                                               public void flush() {
                                                   flushLogs();
                                               }

                                               @Override
                                               public void close() throws SecurityException {
                                                   handlers.forEach((k, v) -> {
                                                       k.close();
                                                   });
                                               }
                                           });
                                           return x;
                                       });
    }

    private static void publishLog(LogRecord record) {
        if (handlers.isEmpty()) {
            System.err.println(format.format(record));
        } else {
            handlers.keySet().forEach(h -> {
                h.publish(record);
            });
        }
    }

    public static void removeLogHandler(Handler handler) {
        handlers.remove(handler);
    }
}
