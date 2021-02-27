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
package com.openglobes.core.interceptor;

import com.openglobes.core.trader.EngineRequestError;
import com.openglobes.core.trader.Response;
import com.openglobes.core.trader.Trade;
import com.openglobes.core.utils.Loggers;
import com.openglobes.core.utils.QuickCondition;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class InterceptorChain implements IInterceptorChain {

    private final Comparator<InterceptorContext> c = (InterceptorContext o1,
                                                      InterceptorContext o2) -> {
        return Integer.compare(o1.getPosition(), o2.getPosition());
    };

    private final List<InterceptorContext> chain;
    private final QuickCondition cond;
    private final Queue<EngineRequestError> errors;
    private final ReentrantReadWriteLock lock;
    private final Queue<RequestInterceptingContext> requests;
    private final Queue<Response> responses;
    private int timeout = 60;
    private final Queue<Trade> trades;
    private final Worker worker;
    

    public InterceptorChain() {
        cond = new QuickCondition();
        lock = new ReentrantReadWriteLock();
        chain = new LinkedList<>();
        requests = new LinkedList<>();
        responses = new LinkedList<>();
        trades = new LinkedList<>();
        errors = new LinkedList<>();
        worker = new Worker(this);
    }

    @Override
    public <T, V> void addInterceptor(int position,
                                      Class<T> tClazz,
                                      Class<V> vClazz,
                                      IInterceptor<T, V> interceptor) throws InterceptorException {
        lock.writeLock().lock();
        try {
            chain.add(new InterceptorContext(position,
                                             tClazz,
                                             vClazz,
                                             interceptor));
            chain.sort(c);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public <T> void addInterceptor(int position,
                                   Class<T> clazz, 
                                   AbstractRequestInterceptor<T> interceptor) throws InterceptorException {
        addInterceptor(position,
                       clazz, 
                       Object.class,
                       interceptor);
    }

    @Override
    public <R> void addInterceptor(int position,
                                   Class<R> clazz,
                                   AbstractResponseInterceptor<R> interceptor) throws InterceptorException {
        addInterceptor(position,
                       Object.class,
                       clazz,
                       interceptor);
    }

    @Override
    public void setEachTimeout(int timeout) {
        if (timeout <= 0) {
            throw new java.lang.IllegalArgumentException("Negative timeout: " + timeout + ".");
        }
        this.timeout = timeout;
    }

    @Override
    public IInterceptor<?, ?> removeInterceptor() throws InterceptorException {
        lock.writeLock().lock();
        if (chain.isEmpty()) {
            return null;
        }
        try {
            return chain.remove(chain.size() - 1).getInterceptor();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public <T> void request(Class<T> clazz, T request) throws InterceptorException {
        if (clazz == RequestInterceptingContext.class) {
            addRequest((RequestInterceptingContext) request);
        } else {
            throw new UnsupportedInterceptingTypeException(clazz.getCanonicalName());
        }
    }

    @Override
    public <T> void respond(Class<T> clazz, T response) throws InterceptorException {
        if (clazz == Trade.class) {
            addTrade((Trade) response);
        } else if (clazz == EngineRequestError.class) {
            addError((EngineRequestError) response);
        } else if (clazz == Response.class) {
            addResponse((Response) response);
        } else {
            throw new UnsupportedInterceptingTypeException(clazz.getCanonicalName());
        }
    }

    private void addError(EngineRequestError e) {
        synchronized (errors) {
            errors.add(e);
        }
        signal0();
    }

    private void addRequest(RequestInterceptingContext ctx) {
        synchronized (requests) {
            requests.add(ctx);
        }
        signal0();
    }

    private void addResponse(Response r) {
        synchronized (responses) {
            responses.add(r);
        }
        signal0();
    }

    private void addTrade(Trade t) {
        synchronized (trades) {
            trades.add(t);
        }
        signal0();
    }

    @SuppressWarnings({"unchecked"})
    private <T> T pop(Class<T> clazz) throws InterceptorException {
        if (clazz == Trade.class) {
            synchronized (trades) {
                return (T) trades.poll();
            }
        } else if (clazz == Response.class) {
            synchronized (responses) {
                return (T) responses.poll();
            }
        } else if (clazz == RequestInterceptingContext.class) {
            synchronized (requests) {
                return (T) requests.poll();
            }
        } else if (clazz == EngineRequestError.class) {
            synchronized (errors) {
                return (T) errors.poll();
            }
        } else {
            throw new UnsupportedInterceptingTypeException(clazz.getCanonicalName());
        }
    }

    private void signal0() {
        cond.signalAll();
    }

    private boolean wait0() {
        try {
            /*
             * Don't wait infinitely.
             * Wake up every second and check the enqueued data.
             */
            return cond.waitSignal(1, 
                                   TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Loggers.getLogger(InterceptorChain.class.getCanonicalName()).log(Level.SEVERE,
                                                                             ex.getMessage(),
                                                                             ex);
            return false;
        }
    }

    private static class InterceptorContext {

        private final IInterceptor<?, ?> intc;
        private final int pos;
        private final Class<?> reqClz;
        private final Class<?> rspClz;

        InterceptorContext(int pos,
                           Class<?> reqClz,
                           Class<?> rspClz,
                           IInterceptor<?, ?> intc) {
            this.intc = intc;
            this.reqClz = reqClz;
            this.rspClz = rspClz;
            this.pos = pos;
        }

        public IInterceptor<?, ?> getInterceptor() {
            return intc;
        }

        public int getPosition() {
            return pos;
        }

        public Class<?> getRequestClass() {
            return reqClz;
        }

        public Class<?> getResponseClass() {
            return rspClz;
        }
    }

    private class Worker implements Runnable {

        private final IInterceptorChain c;
        private final ExecutorService es;
        private final Future<?> future;

        Worker(IInterceptorChain chain) {
            this.c = chain;
            this.es = Executors.newCachedThreadPool();
            future = es.submit(this);
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                if (!wait0()) {
                    continue;
                }
                lock.readLock().lock();
                try {
                    execute(Trade.class,
                            timeout,
                            TimeUnit.SECONDS);
                    execute(Response.class,
                            timeout,
                            TimeUnit.SECONDS);
                    execute(RequestInterceptingContext.class,
                            timeout,
                            TimeUnit.SECONDS);
                    execute(EngineRequestError.class,
                            timeout,
                            TimeUnit.SECONDS);
                } finally {
                    lock.readLock().unlock();
                }
            }
        }

        private <R> void execute(Class<R> clazz,
                                 R object,
                                 int timeout,
                                 TimeUnit unit) throws InterceptorException {
            if (object instanceof Trade || object instanceof Response || object instanceof EngineRequestError) {
                executeResponse(clazz,
                                object,
                                timeout,
                                unit);
            } else if (object instanceof RequestInterceptingContext) {
                executeRequest(clazz,
                               object,
                               timeout,
                               unit);
            } else {
                throw new UnsupportedInterceptingTypeException(clazz.getCanonicalName());
            }
        }

        private <T> void execute(Class<T> clazz,
                                 int timeout,
                                 TimeUnit unit) {
            T object;
            try {
                while ((object = pop(clazz)) != null) {
                    execute(clazz, 
                            object, 
                            timeout,
                            unit);
                }
            } catch (InterceptorException ex) {
                Loggers.getLogger(InterceptorChain.class.getCanonicalName()).log(Level.SEVERE,
                                                                                 ex.toString(),
                                                                                 ex);
            }
        }

        private <T> void executeRequest(Class<T> clazz,
                                        T request,
                                        int timeout,
                                        TimeUnit unit) {
            var nanos = TimeUnit.NANOSECONDS.convert(timeout, unit);
            long s0 = System.nanoTime();
            for (var i = 0; i < chain.size(); ++i) {
                var interceptor = chain.get(i);
                if (interceptor.getRequestClass() == clazz) {
                    @SuppressWarnings("unchecked")
                    var v = (IInterceptor<T, ?>) interceptor.getInterceptor();
                    if (InterceptOperation.CONTINUE == interceptRequestWithTimeout(v,
                                                                                   request,
                                                                                   nanos)) {
                        nanos -= (System.nanoTime() - s0);
                        if (nanos <= 0) {
                            Loggers.getLogger(InterceptorChain.class.getCanonicalName())
                                    .log(Level.SEVERE,
                                         "Interceptor request chaining run out of time({0}ns)",
                                         TimeUnit.NANOSECONDS.convert(timeout, unit));
                            break;
                        }
                        s0 = System.nanoTime();
                    } else {
                        break;
                    }
                }
            }
        }

        private <R> void executeResponse(Class<R> clazz,
                                         R object,
                                         int timeout,
                                         TimeUnit unit) {
            var nanos = TimeUnit.NANOSECONDS.convert(timeout, unit);
            long s0 = System.nanoTime();
            for (var i = chain.size() - 1; i >= 0; --i) {
                var interceptor = chain.get(i);
                if (interceptor.getResponseClass() == clazz) {
                    @SuppressWarnings("unchecked")
                    var v = (IInterceptor<?, R>) interceptor.getInterceptor();
                    if (InterceptOperation.CONTINUE == interceptResponseWithTimeout(v,
                                                                                    object,
                                                                                    nanos)) {
                        nanos -= (System.nanoTime() - s0);
                        if (nanos <= 0) {
                            Loggers.getLogger(InterceptorChain.class.getCanonicalName())
                                    .log(Level.SEVERE,
                                         "Interceptor response chaining run out of time({0}ns)",
                                         TimeUnit.NANOSECONDS.convert(timeout, unit));
                            break;
                        }
                        s0 = System.nanoTime();
                    } else {
                        break;
                    }
                }
            }
        }

        private <T> InterceptOperation interceptRequestWithTimeout(IInterceptor<T, ?> interceptor,
                                                                   T request,
                                                                   long nanos) {
            Future<InterceptOperation> fut = es.submit(() -> {
                return interceptor.onRequest(request, c);
            });
            try {
                return fut.get(nanos, TimeUnit.NANOSECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                Loggers.getLogger(InterceptorChain.class.getCanonicalName()).log(Level.SEVERE,
                                                                                 ex.getMessage(),
                                                                                 ex);
                return InterceptOperation.TERMINATE;
            }
        }

        private <R> InterceptOperation interceptResponseWithTimeout(IInterceptor<?, R> interceptor,
                                                                    R object,
                                                                    long nanos) {
            Future<InterceptOperation> fut = es.submit(() -> {
                return interceptor.onResponse(object, c);
            });
            try {
                return fut.get(nanos, TimeUnit.NANOSECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                Loggers.getLogger(InterceptorChain.class.getCanonicalName()).log(Level.SEVERE,
                                                                                 ex.getMessage(),
                                                                                 ex);
                return InterceptOperation.TERMINATE;
            }
        }

        boolean abort() {
            return future.cancel(true);
        }
    }
}
