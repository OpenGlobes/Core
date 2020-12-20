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

/**
 * Engine status with code and message.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public enum EngineStatus {
    WORKING(0x10),
    STARTING(0x11),
    STOPPING(0x12),
    STOPPED(0x13),
    INITIALIZING(0x14),
    SETTLING(0x15),
    START_FAILED(0x16),
    STOP_FAILED(0x17),
    INIT_FAILED(0x18),
    SETTLE_FAILED(0x19);

    public static EngineStatus getSTARTING() {
        return STARTING;
    }

    private final int c;

    private EngineStatus(int code) {
        c = code;
    }

    public int code() {
        return c;
    }
}
