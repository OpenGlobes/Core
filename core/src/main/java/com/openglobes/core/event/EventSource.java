/*
 * Copyright (C) 2020 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.event;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.openglobes.core.exceptions.Exceptions;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class EventSource implements IEventSource {

    private final Map<Class<?>, Disruptor<?>> disruptors;
    private final Map<Class<?>, IEventHandler<?>> handlers;

    private boolean started = false;

    public EventSource() {
        handlers = new ConcurrentHashMap<>(64);
        disruptors = new ConcurrentHashMap<>(64);
    }

    @Override
    public Collection<Class<?>> getSubscribedTypes() {
        return new HashSet<>(disruptors.keySet());
    }

    @Override
    public Map<Class<?>, IEventHandler<?>> handlers() {
        return handlers;
    }

    @Override
    public boolean isEmpty() {
        return disruptors.isEmpty();
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void publish(Class<T> clazz, T object) throws EventSourceException {
        var c = findDisruptor(clazz, false);
        if (c != null) {
            ((Disruptor<Event<T>>) c).publishEvent(new DefaultEventTranslator<>(),
                                                   object,
                                                   clazz);
        }
    }

    @Override
    public synchronized void start() throws EventSourceException {
        if (started) {
            throw new EventSourceException(Exceptions.DUPLICATED_START.code(),
                                           Exceptions.DUPLICATED_START.message());
        }
        disruptors.values().forEach(d -> {
            d.start();
        });
        started = true;
    }

    @Override

    public synchronized void stop() throws EventSourceException {
        if (!started) {
            throw new EventSourceException(Exceptions.DUPLICATED_STOP.code(),
                                           Exceptions.DUPLICATED_STOP.message());
        }
        disruptors.values().forEach(d -> {
            d.shutdown();
        });
        disruptors.clear();
        handlers.clear();
        started = false;
    }

    @Override
    public <T> void subscribe(Class<T> clazz, IEventHandler<T> handler) throws EventSourceException {
        if (handlers.containsKey(clazz)) {
            throw new EventSourceException(Exceptions.DUPLCATED_SUBSCRIBE_EVENT.code(),
                                           Exceptions.DUPLCATED_SUBSCRIBE_EVENT.message()
                                           + " " + clazz.getCanonicalName());
        }
        handlers.put(clazz, handler);
        @SuppressWarnings("unchecked")
        var c = (Disruptor<Event<T>>) findDisruptor(clazz, true);
        c.handleEventsWith((Event<T> event, long sequence, boolean endOfBatch) -> {
            handler.handle(event);
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Disruptor<T> findDisruptor(Class<T> clazz, boolean created) {
        var v = disruptors.get(clazz);
        if (v == null) {
            if (created) {
                v = new Disruptor<Event<T>>(new DefaultFactory<>(),
                                            1024,
                                            DaemonThreadFactory.INSTANCE);
                disruptors.put(clazz, v);
            }
            else {
                return null;
            }
        }
        return (Disruptor<T>) v;
    }

    private class DefaultEventTranslator<T> implements EventTranslatorTwoArg<Event<T>, T, Class<T>> {

        @Override
        @SuppressWarnings("unchecked")
        public void translateTo(Event<T> event, long sequence, T arg0, Class<T> arg1) {
            event.set(arg0);
            event.setType(arg1);
        }
    }

    private class DefaultFactory<T> implements EventFactory<Event<T>> {

        DefaultFactory() {
        }

        @Override
        public Event<T> newInstance() {
            return new Event<>();
        }

    }

}
