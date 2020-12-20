/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Utils {

    private static final AtomicLong AUTO_INC = new AtomicLong(0);
    private static final AtomicLong EXECUTION_ID = new AtomicLong();

    @SuppressWarnings("unchecked")

    public static <T> T copy(T copied) {
        try (ByteArrayOutputStream bo = new ByteArrayOutputStream()) {
            new ObjectOutputStream(bo).writeObject(copied);
            return (T) new ObjectInputStream(
                    new ByteArrayInputStream(bo.toByteArray())).readObject();
        }
        catch (IOException | ClassNotFoundException ignored) {
            return null;
        }
    }

    public synchronized static long getExecutionId() {
        if (EXECUTION_ID.get() == 0L) {
            var n = nextUuid().getLeastSignificantBits() >> 32;
            EXECUTION_ID.set(n);
        }
        return EXECUTION_ID.get();
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
     *
     * @return
     */
    public static UUID nextUuid() {
        return UUID.randomUUID();
    }

    private Utils() {
    }
}
