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
package com.openglobes.core.data;

import com.openglobes.core.dba.DbaException;
import com.openglobes.core.dba.ICondition;
import com.openglobes.core.dba.IDefaultFactory;
import com.openglobes.core.dba.IPooledDataSource;
import com.openglobes.core.dba.IQuery;
import com.openglobes.core.dba.Queries;
import com.openglobes.core.market.HolidayTime;
import com.openglobes.core.market.HolidayTimePair;
import com.openglobes.core.market.HolidayTimeSet;
import com.openglobes.core.market.InstrumentStickSetting;
import com.openglobes.core.market.InstrumentTime;
import com.openglobes.core.market.WorkdayTime;
import com.openglobes.core.market.WorkdayTimePair;
import com.openglobes.core.market.WorkdayTimeSet;
import com.openglobes.core.stick.ErrorCode;
import com.openglobes.core.trader.TradingDay;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DefaultMarketDataConnection extends MarketDataConnection {

    private final IQuery query;

    public DefaultMarketDataConnection(Connection connection,
                                       IPooledDataSource source) {
        super(connection, source);
        query = Queries.createQuery(connection);
    }

    @Override
    public void addHolidayTime(HolidayTime time) throws MarketDataSourceException {
        callInsert(HolidayTime.class, time);
    }

    @Override
    public void addHolidayTimeSet(HolidayTimeSet set) throws MarketDataSourceException {
        callInsert(HolidayTimeSet.class, set);
    }

    @Override
    public void addInstrumentStickSetting(InstrumentStickSetting setting) throws MarketDataSourceException {
        callInsert(InstrumentStickSetting.class, setting);
    }

    @Override
    public void addInstrumentTime(InstrumentTime time) throws MarketDataSourceException {
        callInsert(InstrumentTime.class, time);
    }

    @Override
    public void addWorkdayTime(WorkdayTime time) throws MarketDataSourceException {
        callInsert(WorkdayTime.class, time);
    }

    @Override
    public void addWorkdayTimeSet(WorkdayTimeSet set) throws MarketDataSourceException {
        callInsert(WorkdayTimeSet.class, set);
    }

    @Override
    public HolidayTime getHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException {
        try {
            return callGetSingle(HolidayTime.class,
                                 Queries.equals(HolidayTime.class.getField("holidayTimeId"),
                                                holidayTimeId),
                                 HolidayTime::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public HolidayTimeSet getHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException {
        try {
            return callGetSingle(HolidayTimeSet.class,
                                 Queries.equals(HolidayTimeSet.class.getField("holidayTimeSetId"),
                                                holidayTimeSetId),
                                 HolidayTimeSet::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<HolidayTimeSet> getHolidayTimeSets() throws MarketDataSourceException {
        try {
            return callGetMany(HolidayTimeSet.class,
                               Queries.isNotNull(HolidayTimeSet.class.getDeclaredField("holidayTimeSetId")),
                               HolidayTimeSet::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<HolidayTime> getHolidayTimes() throws MarketDataSourceException {
        try {
            return callGetMany(HolidayTime.class,
                               Queries.isNotNull(HolidayTime.class.getDeclaredField("holidayTimeId")),
                               HolidayTime::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<HolidayTime> getHolidayTimesByTimeSetId(Long holidayTimeSetId) throws MarketDataSourceException {
        try {
            var pairs = callGetMany(HolidayTimePair.class,
                                Queries.equals(HolidayTimePair.class.getField("holidayTimeSetId"),
                                               holidayTimeSetId),
                                HolidayTimePair::new);
            var r = new HashSet<HolidayTime>(8);
            for (var p : pairs) {
                r.add(callGetSingle(HolidayTime.class,
                                    Queries.equals(HolidayTime.class.getField("holidayTimeId"),
                                                   p.getHolidayTimeId()),
                                    HolidayTime::new));
            }
            return r;
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public InstrumentStickSetting getInstrumentStickSettingById(Long instrumentStickSettingId) throws MarketDataSourceException {
        try {
            return callGetSingle(InstrumentStickSetting.class,
                                 Queries.equals(InstrumentStickSetting.class.getField("instrumentStickSettingId"),
                                                instrumentStickSettingId),
                                 InstrumentStickSetting::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<InstrumentStickSetting> getInstrumentStickSettingByInstrumentId(String instrumentId) throws MarketDataSourceException {
        try {
            return callGetMany(InstrumentStickSetting.class,
                               Queries.equals(InstrumentStickSetting.class.getDeclaredField("instrumentId"),
                                              instrumentId),
                               InstrumentStickSetting::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<InstrumentStickSetting> getInstrumentStickSettings() throws MarketDataSourceException {
        try {
            return callGetMany(InstrumentStickSetting.class,
                               Queries.isNotNull(InstrumentStickSetting.class.getDeclaredField("instrumentStickSettingId")),
                               InstrumentStickSetting::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public InstrumentTime getInstrumentTimeByInstrumentId(String instrumentId) throws MarketDataSourceException {
        try {
            return callGetSingle(InstrumentTime.class,
                                 Queries.equals(InstrumentTime.class.getField("instrumentId"),
                                                instrumentId),
                                 InstrumentTime::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<InstrumentTime> getInstrumentTimes() throws MarketDataSourceException {
        try {
            return callGetMany(InstrumentTime.class,
                               Queries.isNotNull(InstrumentTime.class.getDeclaredField("instrumentTimeId")),
                               InstrumentTime::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public TradingDay getTradingDay() throws MarketDataSourceException {
        try {
            return callGetSingle(TradingDay.class,
                                 Queries.isNotNull(TradingDay.class.getDeclaredField("tradingDayId")),
                                 TradingDay::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public WorkdayTime getWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException {
        try {
            return callGetSingle(WorkdayTime.class,
                                 Queries.equals(WorkdayTime.class.getDeclaredField("workdayTimeId"),
                                                workdayTimeId),
                                 WorkdayTime::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public WorkdayTimeSet getWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException {
        try {
            return callGetSingle(WorkdayTimeSet.class,
                                 Queries.equals(WorkdayTimeSet.class.getDeclaredField("workdayTimeSetId"),
                                                workdayTimeSetId),
                                 WorkdayTimeSet::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<WorkdayTimeSet> getWorkdayTimeSets() throws MarketDataSourceException {
        try {
            return callGetMany(WorkdayTimeSet.class,
                               Queries.isNotNull(WorkdayTimeSet.class.getDeclaredField("workdayTimeSetId")),
                               WorkdayTimeSet::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<WorkdayTime> getWorkdayTimes() throws MarketDataSourceException {
        try {
            return callGetMany(WorkdayTime.class,
                               Queries.isNotNull(WorkdayTime.class.getDeclaredField("workdayTimeId")),
                               WorkdayTime::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public Collection<WorkdayTime> getWorkdayTimesByTimeSetId(Long workdayTimeSetId) throws MarketDataSourceException {
        try {
            var pairs = callGetMany(WorkdayTimePair.class,
                                Queries.equals(WorkdayTimePair.class.getField("workdayTimeSetId"),
                                               workdayTimeSetId),
                                WorkdayTimePair::new);
            var r = new HashSet<WorkdayTime>(16);
            for (var p : pairs) {
                r.add(callGetSingle(WorkdayTime.class,
                                    Queries.equals(WorkdayTime.class.getDeclaredField("workdayTimeId"), p.getWorkdayTimeId()),
                                    WorkdayTime::new));
            }
            return r;
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public void removeHolidayTimeById(Long holidayTimeId) throws MarketDataSourceException {
        callRemove(HolidayTime.class,
                   "holidayTimeId",
                   holidayTimeId);
    }

    @Override
    public void removeHolidayTimeSetById(Long holidayTimeSetId) throws MarketDataSourceException {
        callRemove(HolidayTimeSet.class,
                   "holidayTimeSetId",
                   holidayTimeSetId);
    }

    @Override
    public void removeInstrumentStickSettingById(Long instrumentStickSettingId) throws MarketDataSourceException {
        callRemove(InstrumentStickSetting.class,
                   "instrumentStickSettingId",
                   instrumentStickSettingId);
    }

    @Override
    public void removeInstrumentTimeById(Long instrumentTimeId) throws MarketDataSourceException {
        callRemove(InstrumentTime.class,
                   "instrumentTimeId",
                   instrumentTimeId);
    }

    @Override
    public void removeWorkdayTimeById(Long workdayTimeId) throws MarketDataSourceException {
        callRemove(WorkdayTime.class,
                   "workdayTimeId",
                   workdayTimeId);
    }

    @Override
    public void removeWorkdayTimeSetById(Long workdayTimeSetId) throws MarketDataSourceException {
        callRemove(WorkdayTimeSet.class,
                   "workdayTimeSetId",
                   workdayTimeSetId);
    }

    @Override
    public void updateHolidayTime(HolidayTime time) throws MarketDataSourceException {
        try {
            callUpdate(HolidayTime.class,
                       time,
                       HolidayTime.class.getDeclaredField("holidayTimeId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public void updateHolidayTimeSet(HolidayTimeSet set) throws MarketDataSourceException {
        try {
            callUpdate(HolidayTimeSet.class,
                       set,
                       HolidayTimeSet.class.getDeclaredField("holidayTimeSetId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public void updateInstrumentStickSetting(InstrumentStickSetting setting) throws MarketDataSourceException {
        try {
            callUpdate(InstrumentStickSetting.class,
                       setting,
                       InstrumentStickSetting.class.getDeclaredField("instrumentStickSettingId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public void updateInstrumentTime(InstrumentTime time) throws MarketDataSourceException {
        try {
            callUpdate(InstrumentTime.class,
                       time,
                       InstrumentTime.class.getDeclaredField("instrumentTimeId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public void updateTradingDay(TradingDay tradingDay) throws MarketDataSourceException {
        try {
            callUpdate(TradingDay.class,
                       tradingDay,
                       TradingDay.class.getDeclaredField("tradingDayId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public void updateWorkdayTime(WorkdayTime time) throws MarketDataSourceException {
        try {
            callUpdate(WorkdayTime.class,
                       time,
                       WorkdayTime.class.getDeclaredField("workdayTimeId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

    @Override
    public void updateWorkdayTimeSet(WorkdayTimeSet set) throws MarketDataSourceException {
        try {
            callUpdate(WorkdayTimeSet.class,
                       set,
                       WorkdayTimeSet.class.getDeclaredField("workdayTimeSetId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

    private <T> Collection<T> callGetMany(Class<T> clazz,
                                          ICondition<?> condition,
                                          IDefaultFactory<T> factory) throws MarketDataSourceException {
        try {
            return query.select(clazz, condition, factory);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.DBA_SELECT_FAIL.code(),
                                                ErrorCode.DBA_SELECT_FAIL.message() + " " + clazz.getCanonicalName(),
                                                ex);
        }
    }

    private <T> T callGetSingle(Class<T> clazz,
                                ICondition<?> condition,
                                IDefaultFactory<T> factory) throws MarketDataSourceException {
        var c = callGetMany(clazz, condition, factory);
        if (c.size() > 1) {
            throw new MarketDataSourceException(
                    ErrorCode.MORE_ROWS_THAN_EXPECTED.code(),
                    ErrorCode.MORE_ROWS_THAN_EXPECTED.message() + " " + clazz.getCanonicalName());
        }
        if (c.isEmpty()) {
            throw new MarketDataSourceException(
                    ErrorCode.LESS_ROWS_THAN_EXPECTED.code(),
                    ErrorCode.LESS_ROWS_THAN_EXPECTED.message() + " " + clazz.getCanonicalName());
        }
        return c.iterator().next();
    }

    private <T> void callInsert(Class<T> clazz,
                                T object) throws MarketDataSourceException {
        try {
            query.insert(clazz,
                         object);
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.DBA_INSERT_FAIL.code(),
                                                ErrorCode.DBA_INSERT_FAIL.message(),
                                                ex);
        }
    }

    private <T, V> void callRemove(Class<T> clazz,
                                   String fieldName,
                                   V id) throws MarketDataSourceException {
        try {
            query.remove(clazz,
                         Queries.equals(clazz.getField(fieldName), id));
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                                ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                                ex);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

    private <T> void callUpdate(Class<T> clazz,
                                T object,
                                Field field) throws MarketDataSourceException {
        try {
            query.update(clazz,
                         object,
                         Queries.equals(field, field.getLong(object)));
        }
        catch (DbaException ex) {
            throw new MarketDataSourceException(ErrorCode.DBA_UPDATE_FAIL.code(),
                                                ErrorCode.DBA_UPDATE_FAIL.message() + " " + clazz.getCanonicalName(),
                                                ex);
        }
        catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new MarketDataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                                ErrorCode.REFLECTION_FAIL.message(),
                                                ex);
        }
    }

}
