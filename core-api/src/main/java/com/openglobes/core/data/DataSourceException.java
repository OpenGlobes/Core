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
package com.openglobes.core.data;

import com.openglobes.core.exceptions.EngineException;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DataSourceException extends EngineException {

    private static final long serialVersionUID = 154638779982L;

    public DataSourceException(int code, String message) {
        super(code, message);
    }

    public DataSourceException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
