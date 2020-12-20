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
    public <T> int insert(Class<T> clazz, T object) throws DbaException {
        try {
            return execute(getInsertSql(findMeta(clazz), object));
        }
        catch (SQLException ex) {
            throw new DbaException("Fail executing insertion.", ex);
        }
    }

    @Override
    public <T> int remove(Class<T> clazz, ICondition<?> condition) throws DbaException {
        try {
            return execute(getRemoveSql(findMeta(clazz), condition));
        }
        catch (SQLException ex) {
            throw new DbaException("Fail executing removal.", ex);
        }
    }

    @Override
    public <T> Collection<T> select(Class<T> clazz,
                                    ICondition<?> condition,
                                    IDefaultFactory<T> factory) throws DbaException {
        try {
            var m = findMeta(clazz);
            return executeSelect(m, getSelectSql(m, condition), factory);
        }
        catch (SQLException | ReflectiveOperationException ex) {
            throw new DbaException("Fail executing selection.", ex);
        }
    }

    @Override
    public <T> int update(Class<T> clazz,
                          T object,
                          ICondition<?> condition) throws DbaException {
        try {
            return execute(getUpdateSql(findMeta(clazz), object, condition));
        }
        catch (SQLException ex) {
            throw new DbaException("Fail executing update.", ex);
        }
    }

    private String buildFieldPair(MetaField f) {
        return f.getName() + " " + DbaUtils.convertSqlType(f.getType());
    }

    private <T> String buildFieldPairs(MetaTable<T> meta) throws DbaException {
        if (meta.fields().isEmpty()) {
            throw new DbaException("No field.");
        }
        else {
            String sql = "";
            int i = 0;
            while (i < meta.fields().size() - 1) {
                var f = meta.fields().get(i);
                sql += buildFieldWithKey(f, meta) + ",";
                ++i;
            }
            sql += buildFieldWithKey(meta.fields().get(i), meta);
            if (!sql.contains(PRIMARY_KEY)) {
                throw new DbaException("Table '" + meta.getName() + "' has no primary key.");
            }
            return sql;
        }
    }

    private <T> String buildFieldWithKey(MetaField f, MetaTable<T> meta) {
        String sql = buildFieldPair(f);
        if (isPrimaryKey(f, meta)) {
            sql += " " + PRIMARY_KEY;
        }
        return sql;
    }

    private <T> Collection<T> convert(MetaTable<T> meta,
                                      ResultSet rs,
                                      IDefaultFactory<T> factory) throws ReflectiveOperationException,
                                                                         SQLException {
        Collection<T> c = new LinkedList<>();
        while (rs.next()) {
            c.add(rowT(meta, rs, factory));
        }
        return c;
    }

    private <T> void createTable(MetaTable<T> meta) throws DbaException, SQLException {
        String sql = "CREATE TABLE " + meta.getName() + "(";
        sql += buildFieldPairs(meta);
        sql += ")";
        execute(sql);
    }

    private <T> void ensureTable(MetaTable<T> meta) throws SQLException,
                                                           DbaException {
        var dbm = conn.getMetaData();
        if (!hasTableName(meta, dbm)) {
            createTable(meta);
        }
        else {
            verifyTableColumns(meta, dbm);
        }
    }

    private boolean equalsType(int columnType, int semanticType) throws DbaException {
        switch (semanticType) {
            case Types.CHAR:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.DECIMAL:
                return columnType == semanticType;
            case Types.DATE:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return columnType == Types.CHAR;
        }
        throw new DbaException("Invalid field type in table metadata.");
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
                                                                               ReflectiveOperationException {
        ResultSet rs;
        try (Statement stat = conn.createStatement()) {
            rs = stat.executeQuery(sql);
            return convert(meta, rs, factory);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> MetaTable<T> findMeta(Class<T> clazz) {
        return (MetaTable<T>) meta.computeIfAbsent(clazz.getCanonicalName(), k -> new MetaTable<T>(clazz));
    }

    private <T> String getInsertSql(MetaTable<T> meta,
                                    Object object) throws SQLException,
                                                          DbaException {
        if (meta.fields().isEmpty()) {
            throw new DbaException("No column in table '" + meta.getName() + "'.");
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
            throw new DbaException(
                    "Access field '" + meta.fields().get(i).getName() + "' failed.",
                    ex);
        }
    }

    private <T> String getRemoveSql(MetaTable<T> meta,
                                    ICondition<?> condition) throws SQLException,
                                                                    DbaException {
        ensureTable(meta);
        return "DELETE FROM " + meta.getName() + " WHERE " + ((Condition<?>) condition).getSql();
    }

    private <T> String getSelectSql(MetaTable<T> meta,
                                    ICondition<?> condition) throws SQLException,
                                                                    DbaException {
        ensureTable(meta);
        return "SELECT * FROM " + meta.getName() + " WHERE " + ((Condition<?>) condition).getSql();
    }

    private Map<String, Integer> getTableColumns(String name, DatabaseMetaData dbMeta) throws SQLException {
        var t = new HashMap<String, Integer>(128);
        var cs = dbMeta.getColumns("", "", name, "%");
        while (cs.next()) {
            t.put(cs.getString("COLUMN_NAME"), cs.getInt("DATA_TYPE"));
        }
        return t;
    }

    private <T> String getUpdateSql(MetaTable<T> meta,
                                    Object object,
                                    ICondition<?> condition) throws SQLException,
                                                                    DbaException {
        if (meta.fields().isEmpty()) {
            throw new DbaException("No column in table '" + meta.getName() + "'.");
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
            throw new DbaException(
                    "Access field '" + meta.fields().get(i).getName() + "' failed.",
                    ex);
        }
    }

    private String getValue(MetaField f, Object object) throws IllegalArgumentException,
                                                               IllegalAccessException,
                                                               DbaException {
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
            case Types.TIMESTAMP_WITH_TIMEZONE:
                var timestamp = (ZonedDateTime) f.getField().get(object);
                return timestamp != null ? sqlStringValue(timestamp.toString()) : null;
            case Types.CHAR:
                var str = (String) f.getField().get(object);
                return str != null ? sqlStringValue(str) : null;
            default:
                throw new DbaException("Unsupported SQL types: " + f.getType() + ".");
        }
    }

    private <T> boolean hasTableName(MetaTable<T> meta, DatabaseMetaData dbMeta) throws SQLException {
        try (var rs = dbMeta.getTables("", "", meta.getName(), null)) {
            return rs.next();
        }
    }

    private <T> boolean isPrimaryKey(MetaField f, MetaTable<T> table) {
        var pkn = table.getName().toLowerCase() + "id";
        return f.getField().getName().compareToIgnoreCase(pkn) == 0;
    }

    private <T> T rowT(MetaTable<T> meta, ResultSet rs, IDefaultFactory<T> factory) throws SQLException {
        @SuppressWarnings("unchecked")
        T r = factory.contruct();
        for (var f : meta.fields()) {
            setField(f, r, rs);
        }
        return r;
    }

    private void setField(MetaField field, Object object, ResultSet rs) throws SQLException {
        var f = field.getField();
        var n = field.getName();
        try {
            switch (field.getType()) {
                case Types.BIGINT:
                    f.setLong(object, rs.getLong(n));
                    break;
                case Types.INTEGER:
                    f.setInt(object, rs.getInt(n));
                    break;
                case Types.DECIMAL:
                    f.setDouble(object, rs.getDouble(n));
                    break;
                case Types.DATE:
                    var ds = rs.getString(n);
                    f.set(object, ds != null ? LocalDate.parse(ds) : null);
                    break;
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    var ts = rs.getString(n);
                    f.set(object, ts != null ? ZonedDateTime.parse(ts) : null);
                    break;
                case Types.CHAR:
                    f.set(object, rs.getString(n));
            }
        }
        catch (IllegalAccessException | IllegalArgumentException e) {
            throw new NoSuchFieldError("Fail setting field '" + f.getName() + "'.");
        }
    }

    private String sqlStringValue(String raw) {
        return "'" + raw + "'";
    }

    private <T> void verifyTableColumns(MetaTable<T> meta, DatabaseMetaData dbMeta) throws SQLException,
                                                                                           DbaException {
        var m = getTableColumns(meta.getName(), dbMeta);
        for (var f : meta.fields()) {
            var type = m.get(f.getName());
            if (type == null) {
                throw new DbaException("Field " + f.getName() + " not found in table.");
            }
            else if (!equalsType(type, f.getType())) {
                throw new DbaException("Field " + f.getName() + " has wrong type.");
            }
        }
    }

}
