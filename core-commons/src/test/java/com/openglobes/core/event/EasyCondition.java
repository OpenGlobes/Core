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
package com.openglobes.core.event;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class EasyCondition {

    private final Condition c;
    private final Lock      l;

    public EasyCondition() {
        l = new ReentrantLock();
        c = l.newCondition();
    }

    public void signalAll() {
        l.lock();
        try {
            c.signalAll();
        } finally {
            l.unlock();
        }
    }

    public void signalOne() {
        l.lock();
        try {
            c.signal();
        } finally {
            l.unlock();
        }
    }

    public void waitSignal() throws InterruptedException {
        l.lock();
        try {
            c.await();
        } finally {
            l.unlock();
        }
    }

    public boolean waitSignal(int timeout,
                              TimeUnit unit) throws InterruptedException {
        l.lock();
        try {
            return c.await(timeout,
                           unit);
        } finally {
            l.unlock();
        }
    }

}
