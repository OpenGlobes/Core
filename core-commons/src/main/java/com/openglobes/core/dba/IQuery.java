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

import java.sql.SQLException;
import java.util.Collection;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IQuery {

    <T> Collection<T> select(Class<T> clazz,
                             ICondition<?> condition,
                             IDefaultFactory<T> factory) throws SQLException,
                                                                FieldAccessException,
                                                                FieldInjectionException,
                                                                UnsupportedFieldTypeException,
                                                                IllegalFieldCharacterException,
                                                                MissingFieldException,
                                                                IllegalFieldTypeException,
                                                                NoPrimaryKeyException,
                                                                NoFieldException;

    <T> int update(Class<T> clazz,
                   T object,
                   ICondition<?> condition) throws SQLException,
                                                   IllegalFieldCharacterException,
                                                   UnsupportedFieldTypeException,
                                                   NoFieldException,
                                                   FieldAccessException,
                                                   MissingFieldException,
                                                   IllegalFieldTypeException,
                                                   NoPrimaryKeyException;

    <T> int insert(Class<T> clazz,
                   T object) throws SQLException,
                                    IllegalFieldCharacterException,
                                    UnsupportedFieldTypeException,
                                    FieldAccessException,
                                    NoFieldException,
                                    MissingFieldException,
                                    IllegalFieldTypeException,
                                    NoPrimaryKeyException;

    <T> int remove(Class<T> clazz,
                   ICondition<?> condition) throws SQLException,
                                                   IllegalFieldCharacterException,
                                                   UnsupportedFieldTypeException,
                                                   MissingFieldException,
                                                   IllegalFieldTypeException,
                                                   NoPrimaryKeyException,
                                                   NoFieldException;
}
