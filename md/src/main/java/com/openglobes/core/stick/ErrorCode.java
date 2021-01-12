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
package com.openglobes.core.stick;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public enum ErrorCode {
    DATASOURCE_NULL(0x2000, "Market data source null."),
    NO_WORKDAY_TIME(0x2001, "No workday time."),
    NO_HOLIDAY_TIME(0x2002, "No holiday time."),
    DATASOURCE_AUTOCLOSE_FAIL(0x2003, "Auto close failed."),
    STICKBUILDER_NOT_FOUND(0x2004, "Stick builder not found.");
    
    private final int code;
    private final String message;

    private ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
