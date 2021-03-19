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
package com.openglobes.core.trader;

/**
 * @author Hongbao Chen
 */
public class IllegalRequestActionException extends TraderException {

    private static final long serialVersionUID = 42973610431L;

    public IllegalRequestActionException(String message) {
        super(message);
    }

    public IllegalRequestActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalRequestActionException(Throwable cause) {
        super(cause);
    }

    public IllegalRequestActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
