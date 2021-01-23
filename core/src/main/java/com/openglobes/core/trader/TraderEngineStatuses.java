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
package com.openglobes.core.trader;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public enum TraderEngineStatuses {
    WORKING(0x0, "Working."),
    SETTLING(0x1, "Settling."),
    INITIALIZING(0x2, "Initializing."),
    INIT_FAILED(0x3, "Initialization failed."),
    SETTLE_FAILED(0x4, "Settlement failed."),
    STARTING(0x5, "Starting."),
    START_FAILED(0x6, "Start failed."),
    STOPPING(0x7, "Stopping."),
    STOPPED(0x8, "Stopped."),
    STOP_FAILED(0x9, "Stop failed.");

    private final int    code;
    private final String message;

    private TraderEngineStatuses(int code, String message) {
        this.code    = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
