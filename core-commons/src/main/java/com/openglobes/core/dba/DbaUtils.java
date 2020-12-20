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

import java.lang.reflect.Field;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Hongbao Chen
 * @sicne 1.0
 */
public class DbaUtils {

    public static MetaField inspectField(Field f) {
        com.openglobes.core.dba.MetaField info = new MetaField();
        var names = split(f.getName());
        if (names.size() == 1) {
            info.setName(names.get(0).toUpperCase());
        }
        else if (names.size() > 1) {
            var s = names.get(0).toUpperCase();
            for (int i = 1; i < names.size(); ++i) {
                s += "_" + names.get(i).toUpperCase();
            }
            info.setName(s);
        }
        info.setType(inspectType(f.getType()));
        info.setField(f);
        return info;
    }

    public static List<MetaField> inspectFields(Class<?> clazz) {
        var fs = clazz.getDeclaredFields();
        var r = new LinkedList<MetaField>();
        for (var f : fs) {
            var info = inspectField(f);
            if (info != null) {
                r.add(info);
            }
        }
        return r;
    }

    private static int inspectType(Class<?> clazz) {
        if (clazz == Long.class || clazz == long.class) {
            return Types.BIGINT;
        }
        else if (clazz == Integer.class || clazz == int.class) {
            return Types.INTEGER;
        }
        else if (clazz == Double.class || clazz == double.class) {
            return Types.DECIMAL;
        }
        else if (clazz == LocalDate.class) {
            return Types.DATE;
        }
        else if (clazz == ZonedDateTime.class) {
            return Types.TIMESTAMP_WITH_TIMEZONE;
        }
        else if (clazz == String.class) {
            return Types.CHAR;
        }
        else {
            throw new UnsupportedOperationException(
                    "Field type " + clazz.getCanonicalName() + " is not supported.");
        }
    }

    public static String convertSqlType(int semanticType) {
        switch (semanticType) {
            case Types.BIGINT:
                return "BIGINT";
            case Types.INTEGER:
                return "INT";
            case Types.DECIMAL:
                return "DECIMAL(38, 19)";
            case Types.DATE:
                return "CHAR(10)";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "CHAR(64)";
            case Types.CHAR:
                return "CHAR(128)";
        }
        throw new UnsupportedOperationException(
                "Semantic field type " + semanticType + " is not supported.");
    }

    private static List<String> split(String name) {
        var r = new LinkedList<String>();
        if (name.isBlank()) {
            return r;
        }
        var buffer = new StringBuffer(128);
        for (int i = 0; i < name.length(); ++i) {
            var c = name.charAt(i);
            if (c == '_') {
                throw new RuntimeException("Illegal character \'" + c + "\'.");
            }
            else if (Character.isUpperCase(c)) {
                var str = buffer.toString();
                if (!str.isBlank()) {
                    r.add(str);
                }
                buffer = new StringBuffer(128);
                buffer.append(Character.toLowerCase(c));
            }
            else {
                buffer.append(c);
            }
        }
        var str = buffer.toString();
        if (!str.isBlank()) {
            r.add(str);
        }
        return r;
    }

    private DbaUtils() {
    }
}
