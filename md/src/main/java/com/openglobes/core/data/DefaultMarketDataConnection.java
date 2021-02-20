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

import com.openglobes.core.dba.*;
import com.openglobes.core.market.*;
import com.openglobes.core.trader.TradingDay;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

/**
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
    public void addHolidayTime(HolidayTime time) throws DataInsertionException {
        callInsert(HolidayTime.class, time);
    }

    @Override
    public void addHolidayTimeSet(HolidayTimeSet set) throws DataInsertionException {
        callInsert(HolidayTimeSet.class, set);
    }

    @Override
    public void addInstrumentStickSetting(InstrumentStickSetting setting) throws DataInsertionException {
        callInsert(InstrumentStickSetting.class, setting);
    }

    @Override
    public void addInstrumentTime(InstrumentTime time) throws DataInsertionException {
        callInsert(InstrumentTime.class, time);
    }

    @Override
    public void addWorkdayTime(WorkdayTime time) throws DataInsertionException {
        callInsert(WorkdayTime.class, time);
    }

    @Override
    public void addWorkdayTimeSet(WorkdayTimeSet set) throws DataInsertionException {
        callInsert(WorkdayTimeSet.class, set);
    }

    @Override
    public HolidayTime getHolidayTimeById(Long holidayTimeId) throws DataQueryException {
        try {
            return callGetSingle(HolidayTime.class,
                                 Queries.equals(HolidayTime.class.getDeclaredField("holidayTimeId"),
                                                holidayTimeId),
                                 HolidayTime::new);
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public HolidayTimeSet getHolidayTimeSetById(Long holidayTimeSetId) throws DataQueryException {
        try {
            return callGetSingle(HolidayTimeSet.class,
                                 Queries.equals(HolidayTimeSet.class.getDeclaredField("holidayTimeSetId"),
                                                holidayTimeSetId),
                                 HolidayTimeSet::new);
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<HolidayTimeSet> getHolidayTimeSets() throws DataQueryException {
        try {
            return callGetMany(HolidayTimeSet.class,
                               Queries.isNotNull(HolidayTimeSet.class.getDeclaredField("holidayTimeSetId")),
                               HolidayTimeSet::new);
        } catch (DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<HolidayTime> getHolidayTimes() throws DataQueryException {
        try {
            return callGetMany(HolidayTime.class,
                               Queries.isNotNull(HolidayTime.class.getDeclaredField("holidayTimeId")),
                               HolidayTime::new);
        } catch (DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<HolidayTime> getHolidayTimesByTimeSetId(Long holidayTimeSetId) throws DataQueryException {
        try {
            var pairs = callGetMany(HolidayTimePair.class,
                                Queries.equals(HolidayTimePair.class.getDeclaredField("holidayTimeSetId"),
                                               holidayTimeSetId),
                                HolidayTimePair::new);
            var r = new HashSet<HolidayTime>(8);
            for (var p : pairs) {
                r.add(callGetSingle(HolidayTime.class,
                                    Queries.equals(HolidayTime.class.getDeclaredField("holidayTimeId"),
                                                   p.getHolidayTimeId()),
                                    HolidayTime::new));
            }
            return r;
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public InstrumentStickSetting getInstrumentStickSettingById(Long instrumentStickSettingId) throws DataQueryException {
        try {
            return callGetSingle(InstrumentStickSetting.class,
                                 Queries.equals(InstrumentStickSetting.class.getDeclaredField("instrumentStickSettingId"),
                                                instrumentStickSettingId),
                                 InstrumentStickSetting::new);
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<InstrumentStickSetting> getInstrumentStickSettingByInstrumentId(String instrumentId) throws DataQueryException {
        try {
            return callGetMany(InstrumentStickSetting.class,
                               Queries.equals(InstrumentStickSetting.class.getDeclaredField("instrumentId"),
                                              instrumentId),
                               InstrumentStickSetting::new);
        } catch (DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<InstrumentStickSetting> getInstrumentStickSettings() throws DataQueryException {
        try {
            return callGetMany(InstrumentStickSetting.class,
                               Queries.isNotNull(InstrumentStickSetting.class.getDeclaredField("instrumentStickSettingId")),
                               InstrumentStickSetting::new);
        } catch (DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public InstrumentTime getInstrumentTimeByInstrumentId(String instrumentId) throws DataQueryException {
        try {
            return callGetSingle(InstrumentTime.class,
                                 Queries.equals(InstrumentTime.class.getDeclaredField("instrumentId"),
                                                instrumentId),
                                 InstrumentTime::new);
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<InstrumentTime> getInstrumentTimes() throws DataQueryException {
        try {
            return callGetMany(InstrumentTime.class,
                               Queries.isNotNull(InstrumentTime.class.getDeclaredField("instrumentTimeId")),
                               InstrumentTime::new);
        } catch (DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public TradingDay getTradingDay() throws DataQueryException {
        try {
            return callGetSingle(TradingDay.class,
                                 Queries.isNotNull(TradingDay.class.getDeclaredField("tradingDayId")),
                                 TradingDay::new);
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public WorkdayTime getWorkdayTimeById(Long workdayTimeId) throws DataQueryException {
        try {
            return callGetSingle(WorkdayTime.class,
                                 Queries.equals(WorkdayTime.class.getDeclaredField("workdayTimeId"),
                                                workdayTimeId),
                                 WorkdayTime::new);
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public WorkdayTimeSet getWorkdayTimeSetById(Long workdayTimeSetId) throws DataQueryException {
        try {
            return callGetSingle(WorkdayTimeSet.class,
                                 Queries.equals(WorkdayTimeSet.class.getDeclaredField("workdayTimeSetId"),
                                                workdayTimeSetId),
                                 WorkdayTimeSet::new);
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<WorkdayTimeSet> getWorkdayTimeSets() throws DataQueryException {
        try {
            return callGetMany(WorkdayTimeSet.class,
                               Queries.isNotNull(WorkdayTimeSet.class.getDeclaredField("workdayTimeSetId")),
                               WorkdayTimeSet::new);
        } catch (DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<WorkdayTime> getWorkdayTimes() throws DataQueryException {
        try {
            return callGetMany(WorkdayTime.class,
                               Queries.isNotNull(WorkdayTime.class.getDeclaredField("workdayTimeId")),
                               WorkdayTime::new);
        } catch (DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<WorkdayTime> getWorkdayTimesByTimeSetId(Long workdayTimeSetId) throws DataQueryException {
        try {
            var pairs = callGetMany(WorkdayTimePair.class,
                                Queries.equals(WorkdayTimePair.class.getDeclaredField("workdayTimeSetId"),
                                               workdayTimeSetId),
                                WorkdayTimePair::new);
            var r = new HashSet<WorkdayTime>(16);
            for (var p : pairs) {
                r.add(callGetSingle(WorkdayTime.class,
                                    Queries.equals(WorkdayTime.class.getDeclaredField("workdayTimeId"),
                                                   p.getWorkdayTimeId()),
                                    WorkdayTime::new));
            }
            return r;
        } catch (InvalidQueryResultException | DbaException | NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public void removeHolidayTimeById(Long holidayTimeId) throws DataRemovalException {
        callRemove(HolidayTime.class,
                   "holidayTimeId",
                   holidayTimeId);
    }

    @Override
    public void removeHolidayTimeSetById(Long holidayTimeSetId) throws DataRemovalException {
        callRemove(HolidayTimeSet.class,
                   "holidayTimeSetId",
                   holidayTimeSetId);
    }

    @Override
    public void removeInstrumentStickSettingById(Long instrumentStickSettingId) throws DataRemovalException {
        callRemove(InstrumentStickSetting.class,
                   "instrumentStickSettingId",
                   instrumentStickSettingId);
    }

    @Override
    public void removeInstrumentTimeById(Long instrumentTimeId) throws DataRemovalException {
        callRemove(InstrumentTime.class,
                   "instrumentTimeId",
                   instrumentTimeId);
    }

    @Override
    public void removeWorkdayTimeById(Long workdayTimeId) throws DataRemovalException {
        callRemove(WorkdayTime.class,
                   "workdayTimeId",
                   workdayTimeId);
    }

    @Override
    public void removeWorkdayTimeSetById(Long workdayTimeSetId) throws DataRemovalException {
        callRemove(WorkdayTimeSet.class,
                   "workdayTimeSetId",
                   workdayTimeSetId);
    }

    @Override
    public void updateHolidayTime(HolidayTime time) throws DataUpdateException {
        try {
            callUpdate(HolidayTime.class,
                       time,
                       HolidayTime.class.getDeclaredField("holidayTimeId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateHolidayTimeSet(HolidayTimeSet set) throws DataUpdateException {
        try {
            callUpdate(HolidayTimeSet.class,
                       set,
                       HolidayTimeSet.class.getDeclaredField("holidayTimeSetId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateInstrumentStickSetting(InstrumentStickSetting setting) throws DataUpdateException {
        try {
            callUpdate(InstrumentStickSetting.class,
                       setting,
                       InstrumentStickSetting.class.getDeclaredField("instrumentStickSettingId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateInstrumentTime(InstrumentTime time) throws DataUpdateException {
        try {
            callUpdate(InstrumentTime.class,
                       time,
                       InstrumentTime.class.getDeclaredField("instrumentTimeId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateTradingDay(TradingDay tradingDay) throws DataUpdateException {
        try {
            callUpdate(TradingDay.class,
                       tradingDay,
                       TradingDay.class.getDeclaredField("tradingDayId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateWorkdayTime(WorkdayTime time) throws DataUpdateException {
        try {
            callUpdate(WorkdayTime.class,
                       time,
                       WorkdayTime.class.getDeclaredField("workdayTimeId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateWorkdayTimeSet(WorkdayTimeSet set) throws DataUpdateException {
        try {
            callUpdate(WorkdayTimeSet.class,
                       set,
                       WorkdayTimeSet.class.getDeclaredField("workdayTimeSetId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    private <T> Collection<T> callGetMany(Class<T> clazz,
                                          ICondition<?> condition,
                                          IDefaultFactory<T> factory) throws DataQueryException {
        try {
            return query.select(clazz,
                                condition,
                                factory);
        } catch (SQLException | DbaException ex) {
            throw new DataQueryException(clazz.getCanonicalName(),
                                         ex);
        }
    }

    private <T> T callGetSingle(Class<T> clazz,
                                ICondition<?> condition,
                                IDefaultFactory<T> factory) throws InvalidQueryResultException,
                                                                   DataQueryException {
        Collection<T> c = callGetMany(clazz,
                                      condition,
                                      factory);
        if (c.size() > 1) {
            throw new InvalidQueryResultException(clazz.getCanonicalName());
        }
        if (c.isEmpty()) {
            throw new InvalidQueryResultException(clazz.getCanonicalName() + " has empty query result.");
        }
        return c.iterator().next();
    }

    private <T> void callInsert(Class<T> clazz,
                                T object) throws DataInsertionException {
        try {
            query.insert(clazz,
                         object);
        } catch (SQLException | DbaException ex) {
            throw new DataInsertionException(clazz.getCanonicalName(),
                                             ex);
        }
    }

    private <T, V> void callRemove(Class<T> clazz,
                                   String fieldName,
                                   V id) throws DataRemovalException {
        try {
            query.remove(clazz,
                         Queries.equals(clazz.getDeclaredField(fieldName),
                                        id));
        } catch (NoSuchFieldException | SecurityException | SQLException | DbaException ex) {
            throw new DataRemovalException(clazz.getCanonicalName(),
                                           ex);
        }
    }

    private <T> void callUpdate(Class<T> clazz,
                                T object,
                                Field field) throws DataUpdateException {
        try {
            query.update(clazz,
                         object,
                         Queries.equals(field,
                                        DbaUtils.getLong(field, object)));
        } catch (IllegalArgumentException | IllegalAccessException | SQLException | DbaException ex) {
            throw new DataUpdateException(clazz.getCanonicalName(),
                                          ex);
        }
    }

}
