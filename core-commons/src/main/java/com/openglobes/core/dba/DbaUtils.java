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
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Hongbao Chen
 * @sicne 1.0
 */
public class DbaUtils {

    private DbaUtils() {
    }

    public static String convertSqlType(int semanticType) throws UnsupportedFieldTypeException {
        switch (semanticType) {
            case Types.BIGINT:
                return "BIGINT";
            case Types.INTEGER:
                return "INT";
            case Types.DECIMAL:
                return "DECIMAL(38, 19)";
            case Types.DATE:
                return "CHAR(16)";
            case Types.TIME:
                return "CHAR(24)";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "CHAR(64)";
            case Types.CHAR:
                return "CHAR(128)";
        }
        throw new UnsupportedFieldTypeException("Sql type" + semanticType + " is not supported.");
    }

    public static Double getDouble(Field field,
                                   Object object) throws IllegalAccessException {
        if (field.getType() == Double.class) {
            var o = field.get(object);
            return o != null ? (Double) o : null;
        } else if (field.getType() == double.class) {
            return field.getDouble(object);
        } else {
            throw new IllegalAccessException("Field must be declared as double or Double.");
        }
    }

    public static Integer getInt(Field field,
                                 Object object) throws IllegalAccessException {
        if (field.getType() == Integer.class) {
            var o = field.get(object);
            return o != null ? (Integer) o : null;
        } else if (field.getType() == int.class) {
            return field.getInt(object);
        } else {
            throw new IllegalAccessException("Field must be declared as int or Integer.");
        }
    }

    public static Long getLong(Field field,
                               Object object) throws IllegalAccessException {
        if (field.getType() == Long.class) {
            var o = field.get(object);
            return o != null ? (Long) o : null;
        } else if (field.getType() == long.class) {
            return field.getLong(object);
        } else {
            throw new IllegalAccessException("Field must be declared as long or Long.");
        }
    }

    public static MetaField inspectField(Field f) throws IllegalFieldCharacterException,
                                                         UnsupportedFieldTypeException {
        MetaField info = new MetaField();
        var names = split(f.getName());
        var prefix = "FIELD_";
        if (names.size() == 1) {
            info.setName(prefix + names.get(0).toUpperCase());
        } else if (names.size() > 1) {
            var s = prefix + names.get(0).toUpperCase();
            for (int i = 1; i < names.size(); ++i) {
                s += "_" + names.get(i).toUpperCase();
            }
            info.setName(s);
        }
        info.setType(inspectType(f.getType()));
        info.setField(f);
        return info;
    }

    public static List<MetaField> inspectFields(Class<?> clazz) throws IllegalFieldCharacterException,
                                                                       UnsupportedFieldTypeException {
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

    public static void setDouble(Field field,
                                 Object object,
                                 Double d) throws IllegalArgumentException, IllegalAccessException {
        if (field.getType() == Double.class) {
            field.set(object,
                      d);
        } else if (field.getType() == double.class) {
            field.setDouble(object,
                            d);
        } else {
            throw new IllegalAccessException("Field must be declared as double or Double.");
        }
    }

    public static void setInteger(Field field,
                                  Object object,
                                  Integer integer) throws IllegalArgumentException,
                                                          IllegalAccessException {
        if (field.getType() == Integer.class) {
            field.set(object,
                      integer);
        } else if (field.getType() == int.class) {
            field.setInt(object,
                         integer);
        } else {
            throw new IllegalAccessException("Field must be declared as int or Integer.");
        }
    }

    public static void enableAccess(Field field) {
        if (!isPublic(field)) {
            field.setAccessible(true);
        }
    }

    private static boolean isPublic(Field fd) {
        return (fd.getModifiers() & Modifier.PUBLIC) != 0;
    }

    public static void setLong(Field field,
                               Object object,
                               Long l) throws IllegalArgumentException,
                                              IllegalAccessException {
        if (field.getType() == Long.class) {
            field.set(object,
                      l);
        } else if (field.getType() == long.class) {
            field.setLong(object,
                          l);
        } else {
            throw new IllegalAccessException("Field must be declared as long or Long.");
        }
    }

    private static int inspectType(Class<?> clazz) throws UnsupportedFieldTypeException {
        if (clazz == Long.class || clazz == long.class) {
            return Types.BIGINT;
        } else if (clazz == Integer.class || clazz == int.class) {
            return Types.INTEGER;
        } else if (clazz == Double.class || clazz == double.class) {
            return Types.DECIMAL;
        } else if (clazz == LocalDate.class) {
            return Types.DATE;
        } else if (clazz == LocalTime.class) {
            return Types.TIME;
        } else if (clazz == ZonedDateTime.class) {
            return Types.TIMESTAMP_WITH_TIMEZONE;
        } else if (clazz == String.class) {
            return Types.CHAR;
        } else {
            throw new UnsupportedFieldTypeException(clazz.getCanonicalName() + " is not supported.");
        }
    }

    private static List<String> split(String name) throws IllegalFieldCharacterException {
        var r = new LinkedList<String>();
        if (name.isBlank()) {
            return r;
        }
        var buffer = new StringBuffer(128);
        for (int i = 0; i < name.length(); ++i) {
            var c = name.charAt(i);
            if (c == '_') {
                throw new IllegalFieldCharacterException("Illegal character \'" + c + "\'.");
            } else if (Character.isUpperCase(c)) {
                var str = buffer.toString();
                if (!str.isBlank()) {
                    r.add(str);
                }
                buffer = new StringBuffer(128);
                buffer.append(Character.toLowerCase(c));
            } else {
                buffer.append(c);
            }
        }
        var str = buffer.toString();
        if (!str.isBlank()) {
            r.add(str);
        }
        return r;
    }
}
