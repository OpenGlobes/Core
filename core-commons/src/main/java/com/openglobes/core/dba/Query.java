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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
class Query implements IQuery {

    private final String PRIMARY_KEY = "PRIMARY KEY";
    private final Connection conn;
    private final Map<String, MetaTable<?>> meta;

    Query(Connection connection) {
        conn = connection;
        meta = new HashMap<>(64);
    }

    @Override
    public <T> int insert(Class<T> clazz,
                          T object) throws SQLException,
                                           IllegalFieldCharacterException,
                                           UnsupportedTypeException,
                                           FieldAccessException,
                                           NoFieldException,
                                           MissingFieldException,
                                           IllegalFieldTypeException,
                                           NoPrimaryKeyException {
        return execute(getInsertSql(findMeta(clazz), object));
    }

    @Override
    public <T> int remove(Class<T> clazz,
                          ICondition<?> condition) throws SQLException,
                                                          IllegalFieldCharacterException,
                                                          UnsupportedTypeException,
                                                          MissingFieldException,
                                                          IllegalFieldTypeException,
                                                          NoPrimaryKeyException,
                                                          NoFieldException {
        return execute(getRemoveSql(findMeta(clazz),
                                    condition));
    }

    @Override
    public <T> Collection<T> select(Class<T> clazz,
                                    ICondition<?> condition,
                                    IDefaultFactory<T> factory) throws SQLException,
                                                                       FieldAccessException,
                                                                       FieldInjectionException,
                                                                       UnsupportedTypeException,
                                                                       IllegalFieldCharacterException {
        try {
            var m = findMeta(clazz);
            return executeSelect(m,
                                 getSelectSql(m,
                                              condition),
                                 factory);
        }
        catch (ReflectiveOperationException ex) {
            throw new FieldAccessException("Fail executing selection.", ex);
        }
    }

    @Override
    public <T> int update(Class<T> clazz,
                          T object,
                          ICondition<?> condition) throws SQLException,
                                                          IllegalFieldCharacterException,
                                                          UnsupportedTypeException,
                                                          NoFieldException,
                                                          FieldAccessException,
                                                          MissingFieldException,
                                                          IllegalFieldTypeException,
                                                          NoPrimaryKeyException {
        return execute(getUpdateSql(findMeta(clazz),
                                    object,
                                    condition));
    }

    private String buildFieldPair(MetaField f) throws UnsupportedTypeException {
        return f.getName() + " " + DbaUtils.convertSqlType(f.getType());
    }

    private <T> String buildFieldPairs(MetaTable<T> meta) throws NoFieldException,
                                                                 UnsupportedTypeException,
                                                                 NoPrimaryKeyException {
        if (meta.fields().isEmpty()) {
            throw new NoFieldException(meta.getName());
        }
        else {
            String sql = "";
            int i = 0;
            while (i < meta.fields().size() - 1) {
                var f = meta.fields().get(i);
                String sqlField = buildFieldWithKey(f,
                                                    meta);
                sql += sqlField + ",";
                ++i;
            }
            sql += buildFieldWithKey(meta.fields().get(i), meta);
            if (!sql.contains(PRIMARY_KEY)) {
                throw new NoPrimaryKeyException(meta.getName());
            }
            return sql;
        }
    }

    private <T> String buildFieldWithKey(MetaField f,
                                         MetaTable<T> meta) throws UnsupportedTypeException {
        String sql = buildFieldPair(f);
        boolean priKey = isPrimaryKey(f,
                                      meta);
        if (priKey) {
            sql += " " + PRIMARY_KEY;
        }
        return sql;
    }

    private <T> Collection<T> convert(MetaTable<T> meta,
                                      ResultSet rs,
                                      IDefaultFactory<T> factory) throws ReflectiveOperationException,
                                                                         SQLException,
                                                                         FieldInjectionException {
        Collection<T> c = new LinkedList<>();
        while (rs.next()) {
            c.add(rowT(meta,
                       rs,
                       factory));
        }
        return c;
    }

    private <T> void createTable(MetaTable<T> meta) throws SQLException,
                                                           NoPrimaryKeyException,
                                                           UnsupportedTypeException,
                                                           NoFieldException {
        String sql = "CREATE TABLE " + meta.getName() + "(";
        sql += buildFieldPairs(meta);
        sql += ")";
        execute(sql);
    }

    private <T> void ensureTable(MetaTable<T> meta) throws SQLException,
                                                           MissingFieldException,
                                                           IllegalFieldTypeException,
                                                           NoPrimaryKeyException,
                                                           NoFieldException,
                                                           UnsupportedTypeException {
        var dbm = conn.getMetaData();
        boolean has = hasTableName(meta,
                                   dbm);
        if (!has) {
            createTable(meta);
        }
        else {
            verifyTableColumns(meta,
                               dbm);
        }
    }

    private boolean equalsType(int columnType,
                               int semanticType) throws UnsupportedTypeException {
        switch (semanticType) {
            case Types.CHAR:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.DECIMAL:
                return columnType == semanticType;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return columnType == Types.CHAR;
        }
        throw new UnsupportedTypeException("Sql Type " + semanticType + " is not supported.");
    }

    private int execute(String sql) throws SQLException {
        try (Statement stat = conn.createStatement()) {
            stat.execute(sql);
            return stat.getUpdateCount();
        }
    }

    private <T> Collection<T> executeSelect(MetaTable<T> meta,
                                            String sql,
                                            IDefaultFactory<T> factory) throws SQLException,
                                                                               ReflectiveOperationException,
                                                                               FieldInjectionException {
        ResultSet rs;
        try (Statement stat = conn.createStatement()) {
            rs = stat.executeQuery(sql);
            return convert(meta,
                           rs,
                           factory);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> MetaTable<T> findMeta(Class<T> clazz) throws IllegalFieldCharacterException,
                                                             UnsupportedTypeException {
        var r = (MetaTable<T>) meta.get(clazz.getCanonicalName());
        if (r == null) {
            r = MetaTable.create(clazz);
            meta.put(clazz.getCanonicalName(), r);
        }
        return r;
    }

    private <T> String getInsertSql(MetaTable<T> meta,
                                    Object object) throws SQLException,
                                                          FieldAccessException,
                                                          UnsupportedTypeException,
                                                          NoFieldException,
                                                          MissingFieldException,
                                                          IllegalFieldTypeException,
                                                          NoPrimaryKeyException {
        if (meta.fields().isEmpty()) {
            throw new NoFieldException(meta.getName());
        }
        ensureTable(meta);
        var sql = "INSERT INTO " + meta.getName();
        String fields = "";
        String values = "";
        int i = 0;
        try {
            while (i < meta.fields().size() - 1) {
                var f = meta.fields().get(i);
                fields += f.getName() + ",";
                values += getValue(f, object) + ",";
            }
            var f = meta.fields().get(i);
            fields += f.getName();
            values += getValue(f, object);
            return sql + "(" + fields + ") VALUES (" + values + ")";
        }
        catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new FieldAccessException("Access field '" + meta.fields().get(i).getName() + "' failed.",
                                           ex);
        }
    }

    private <T> String getRemoveSql(MetaTable<T> meta,
                                    ICondition<?> condition) throws SQLException,
                                                                    MissingFieldException,
                                                                    IllegalFieldTypeException,
                                                                    NoPrimaryKeyException,
                                                                    NoFieldException,
                                                                    UnsupportedTypeException {
        ensureTable(meta);
        return "DELETE FROM " + meta.getName() + " WHERE " + ((Condition<?>) condition).getSql();
    }

    private <T> String getSelectSql(MetaTable<T> meta,
                                    ICondition<?> condition) throws SQLException {
        return "SELECT * FROM " + meta.getName() + " WHERE " + ((Condition<?>) condition).getSql();
    }

    private Map<String, Integer> getTableColumns(String name,
                                                 DatabaseMetaData dbMeta) throws SQLException {
        var t = new HashMap<String, Integer>(128);
        var cs = dbMeta.getColumns("",
                               "",
                               name,
                               "%");
        while (cs.next()) {
            t.put(cs.getString("COLUMN_NAME"),
                  cs.getInt("DATA_TYPE"));
        }
        return t;
    }

    private <T> String getUpdateSql(MetaTable<T> meta,
                                    Object object,
                                    ICondition<?> condition) throws SQLException,
                                                                    NoFieldException,
                                                                    UnsupportedTypeException,
                                                                    FieldAccessException,
                                                                    MissingFieldException,
                                                                    IllegalFieldTypeException,
                                                                    NoPrimaryKeyException {
        if (meta.fields().isEmpty()) {
            throw new NoFieldException(meta.getName());
        }
        ensureTable(meta);
        var sql = "UPDATE " + meta.getName() + " SET ";
        int i = 0;
        try {
            while (i < meta.fields().size() - 1) {
                var f = meta.fields().get(i);
                sql += f.getName() + "=" + getValue(f, object) + ",";
            }
            var f = meta.fields().get(i);
            sql += f.getName() + "=" + getValue(f, object);
            return sql + " WHERE " + ((Condition<?>) condition).getSql();
        }
        catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new FieldAccessException(meta.fields().get(i).getName(),
                                           ex);
        }
    }

    private String getValue(MetaField f, Object object) throws IllegalArgumentException,
                                                               IllegalAccessException,
                                                               UnsupportedTypeException {
        switch (f.getType()) {
            case Types.BIGINT:
                return Long.toString(f.getField().getLong(object));
            case Types.INTEGER:
                return Integer.toString(f.getField().getInt(object));
            case Types.DECIMAL:
                return Double.toString(f.getField().getDouble(object));
            case Types.DATE:
                var date = (LocalDate) f.getField().get(object);
                return date != null ? sqlStringValue(date.toString()) : null;
            case Types.TIME:
                var time = (LocalTime) f.getField().get(object);
                return time != null ? sqlStringValue(time.toString()) : null;
            case Types.TIMESTAMP_WITH_TIMEZONE:
                var timestamp = (ZonedDateTime) f.getField().get(object);
                return timestamp != null ? sqlStringValue(timestamp.toString()) : null;
            case Types.CHAR:
                var str = (String) f.getField().get(object);
                return str != null ? sqlStringValue(str) : null;
            default:
                throw new UnsupportedTypeException("Sql type " + f.getType() + " is not supported.");
        }
    }

    private <T> boolean hasTableName(MetaTable<T> meta,
                                     DatabaseMetaData dbMeta) throws SQLException {
        try (var rs = dbMeta.getTables("", "", meta.getName(), null)) {
            return rs.next();
        }
    }

    private <T> boolean isPrimaryKey(MetaField f,
                                     MetaTable<T> table) {
        var pkn = table.getName().toLowerCase() + "id";
        return f.getField().getName().compareToIgnoreCase(pkn) == 0;
    }

    private <T> T rowT(MetaTable<T> meta, ResultSet rs, IDefaultFactory<T> factory) throws SQLException,
                                                                                           FieldInjectionException {
        @SuppressWarnings("unchecked")
        T r = factory.contruct();
        for (var f : meta.fields()) {
            setField(f,
                     r,
                     rs);
        }
        return r;
    }

    private void setField(MetaField field,
                          Object object,
                          ResultSet rs) throws SQLException,
                                               FieldInjectionException {
        var f = field.getField();
        var n = field.getName();
        try {
            switch (field.getType()) {
                case Types.BIGINT:
                    f.setLong(object,
                              rs.getLong(n));
                    break;
                case Types.INTEGER:
                    f.setInt(object,
                             rs.getInt(n));
                    break;
                case Types.DECIMAL:
                    f.setDouble(object,
                                rs.getDouble(n));
                    break;
                case Types.DATE:
                    var ds = rs.getString(n);
                    f.set(object, ds != null ? LocalDate.parse(ds) : null);
                    break;
                case Types.TIME:
                    var tm = rs.getString(n);
                    f.set(object,
                          tm != null ? LocalTime.parse(tm) : null);
                    break;
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    var ts = rs.getString(n);
                    f.set(object,
                          ts != null ? ZonedDateTime.parse(ts) : null);
                    break;
                case Types.CHAR:
                    f.set(object,
                          rs.getString(n));
            }
        }
        catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new FieldInjectionException(f.getName(),
                                              ex);
        }
    }

    private String sqlStringValue(String raw) {
        return "'" + raw + "'";
    }

    private <T> void verifyTableColumns(MetaTable<T> meta,
                                        DatabaseMetaData dbMeta) throws SQLException,
                                                                        MissingFieldException,
                                                                        IllegalFieldTypeException,
                                                                        UnsupportedTypeException {
        var m = getTableColumns(meta.getName(), dbMeta);
        for (var f : meta.fields()) {
            var type = m.get(f.getName());
            if (type == null) {
                throw new MissingFieldException(f.getName() + " not found in table.");
            }
            else if (!equalsType(type, f.getType())) {
                throw new IllegalFieldTypeException(f.getName() + " has wrong type.");
            }
        }
    }

}
