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

import com.openglobes.core.event.*;

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

    private static final Map<Handler, Object> handlers = new ConcurrentHashMap<>(64);
    private static final Map<String, Logger> loggers = new ConcurrentHashMap<>(64);
    private static final IEventSource events = new EventSource();
    private static final SimpleFormatter format = new SimpleFormatter();

    static {
        try {
            events.subscribe(LogRecord.class, event -> {
                         var l = event.get();
                         if (l != null) {
                             publishLog(l);
                         }
                     });
        } catch (InvalidSubscriptionException e) {
            e.printStackTrace();
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
                                                   try {
                                                       events.publish(LogRecord.class,
                                                                      record);
                                                   } catch (NoSubscribedClassException ex) {
                                                       ex.printStackTrace();
                                                   }
                                               }

                                               @Override
                                               public void flush() {
                                                   handlers.forEach((k, v) -> {
                                                       k.flush();
                                                   });
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
