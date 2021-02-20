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
 * @param <T>
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class MetaTable<T> {

    private final List<MetaField> fields;
    private final String name;
    private final Class<T> type;

    private MetaTable(Class<T> clazz) throws IllegalFieldCharacterException,
                                             UnsupportedFieldTypeException {
        type = clazz;
        fields = new LinkedList<>();
        name = buildTableName(clazz.getSimpleName());
        parseFields(type);
    }

    public static <T> MetaTable<T> create(Class<T> clazz) throws IllegalFieldCharacterException,
                                                                 UnsupportedFieldTypeException {
        return new MetaTable<>(clazz);
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

    private String buildTableName(String name) {
        return "TABLE_" + name.toUpperCase();
    }

    private void parseFields(Class<T> clazz) throws IllegalFieldCharacterException,
                                                    UnsupportedFieldTypeException {
        DbaUtils.inspectFields(clazz).forEach(f -> {
            fields.add(f);
        });
    }
}
