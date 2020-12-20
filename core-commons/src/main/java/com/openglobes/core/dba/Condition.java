/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.dba;

import java.lang.reflect.Field;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
class Condition<T> implements ICondition<T> {

    private final MetaField meta;
    private final String sqlv;
    private final ConditionType t;
    private T v0;
    private T v1;

    Condition(T value, ConditionType type) throws DbaException {
        if (type != ConditionType.NOT) {
            throw new IllegalArgumentException("Expect NOT but found " + type + ".");
        }
        if (!(value instanceof ICondition)) {
            throw new IllegalArgumentException("NOT needs ICondition as parameter.");
        }
        meta = null;
        v0 = value;
        t = type;
        sqlv = stringValue(v0);
    }

    Condition(Field field, ConditionType type) throws DbaException {
        if (type != ConditionType.IS_NULL && type != ConditionType.IS_NOT_NULL) {
            throw new IllegalArgumentException("Expect IS_NULL/IS_NOT_NULL but found " + type + ".");
        }
        meta = DbaUtils.inspectField(field);
        t = type;
        sqlv = stringValue(type);
    }

    Condition(Field field, T value, ConditionType type) throws DbaException {
        meta = DbaUtils.inspectField(field);
        v0 = value;
        t = type;
        sqlv = stringValue(v0);
    }

    Condition(T c0, T c1, ConditionType type) throws DbaException {
        if (type != ConditionType.AND && type != ConditionType.OR) {
            throw new IllegalArgumentException("Expect AND/OR but found " + type + ".");
        }
        this.meta = null;
        v0 = c0;
        v1 = c1;
        t = type;
        this.sqlv = stringValue(v0, v1);
    }

    private String stringValue(T v) throws DbaException {
        if (v instanceof Number) {
            return "" + v;
        }
        else if (v instanceof String) {
            return "'" + v + "'";
        }
        else if (v instanceof Condition) {
            return ((Condition) v).getSql();
        }
        throw new DbaException("Unsupported type <T>.");
    }

    private String stringValue(T v0, T v1) throws DbaException {
        if (!(v0 instanceof Condition) || !(v1 instanceof Condition)) {
            throw new DbaException("AND/OR need condition operand.");
        }
        var c0 = (Condition<?>) v0;
        var c1 = (Condition<?>) v1;
        switch (t) {
            case AND:
                return "(" + c0.getSql() + ") AND (" + c1.getSql() + ")";
            case OR:
                return "(" + c0.getSql() + ") OR (" + c1.getSql() + ")";
            default:
                throw new DbaException("Expect AND/OR but found " + t + ".");
        }
    }

    @Override
    public Field getField() {
        return meta.getField();
    }

    @Override
    public ConditionType getType() {
        return t;
    }

    @Override
    public T getValue0() {
        return v0;
    }

    @Override
    public T getValue1() {
        return v1;
    }

    private String stringValue(ConditionType type) {
        switch (type) {
            case IS_NULL:
                return "IS NULL";
            case IS_NOT_NULL:
                return "IS NOT NULL";
            default:
                throw new IllegalArgumentException("Expect IS_NULL/IS_NOT_NULL but found " + type + ".");
        }
    }

    boolean checkBelonging(Class<?> clazz) {
        return meta.getField().getDeclaringClass() == clazz;
    }

    String getSql() {
        switch (t) {
            case AND:
            case OR:
                return sqlv;
            case EQUALS:
                return meta.getName() + "=" + sqlv;
            case LESS_THAN:
                return meta.getName() + "<" + sqlv;
            case LARGER_THAN:
                return meta.getName() + ">" + sqlv;
            case LIKE:
                return meta.getName() + " LIKE " + sqlv;
            case NOT:
                return "NOT (" + sqlv + ")";
        }
        throw new IllegalArgumentException("Wrong condition type " + t + ".");
    }
}
