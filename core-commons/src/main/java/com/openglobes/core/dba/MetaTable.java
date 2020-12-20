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
package com.openglobes.core.dba;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class MetaTable<T> {

    private final List<MetaField> fields;
    private final String name;
    private final Class<T> type;

    public MetaTable(Class<T> clazz) {
        type = clazz;
        fields = new LinkedList<>();
        name = clazz.getSimpleName();
        parseFields(type);
    }

    public List<MetaField> fields() {
        return fields;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    private void parseFields(Class<T> clazz) {
        DbaUtils.inspectFields(clazz).forEach(f -> {
            fields.add(f);
        });
    }
}
