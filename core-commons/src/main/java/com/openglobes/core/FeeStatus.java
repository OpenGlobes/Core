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
package com.openglobes.core;

/**
 * Fee status.
 *
 * @author Hongbao Chen
 * @sine 1.0
 */
public enum FeeStatus {
    FORZEN(0x20),
    DEALED(0x21),
    REMOVED(0x22);

    private final int code;

    private FeeStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
