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
package com.openglobes.core.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Translate source ID to destinated ID, and trans back.
 * <p>
 * It also provides a simple count down mechanism to member the rest volumn to
 * trade. If it counts down to zero, the order is fulfilled.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class IdTranslator {

    private final HashMap<Long, Long> destDownCounts;
    private final HashMap<Long, Long> dests;
    private final AtomicLong id = new AtomicLong(0);
    private final HashMap<Long, Set<Long>> srcs;

    public IdTranslator() {
        dests = new HashMap<>(1024);
        destDownCounts = new HashMap<>(1024);
        srcs = new HashMap<>(1024);
    }

    public void clear() {
        dests.clear();
        destDownCounts.clear();
        srcs.clear();
    }

    public void countDown(Long destId, Long count) {
        Long c;
        synchronized (destDownCounts) {
            c = destDownCounts.get(destId);
            if (c == null) {
                throw new NullPointerException("Count down not found(" + destId + ").");
            }
            c -= count;
            destDownCounts.put(destId, c);
        }
        if (c < 0) {
            throw new IllegalStateException("Count down too many(" + count + ").");
        }
    }

    public void setBase(Long b) {
        id.set(b);
    }

    public Long getDestinatedId(Long srcId, Long downCount) {
        var i = getDestinatedId(srcId);
        initCountDown(i, downCount);
        return i;
    }

    public Long getDestinatedId(Long srcId) {
        var i = id.incrementAndGet();
        dests.put(i, srcId);
        var s = srcs.computeIfAbsent(srcId, (Long k) -> new HashSet<>(64));
        s.add(i);
        return i;
    }

    public Collection<Long> getDestinatedIds(Long srcId) {
        return srcs.get(srcId);
    }

    public Long getDownCountByDestId(Long destId) {
        synchronized (destDownCounts) {
            return destDownCounts.get(destId);
        }
    }

    public Long getSourceId(Long destId) {
        return dests.get(destId);
    }

    private void initCountDown(Long destId, Long count) {
        synchronized (destDownCounts) {
            destDownCounts.put(destId, count);
        }
    }

}
