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
package com.openglobes.core.dba.tables;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class InvalidTableWithUnsupportedFieldTypes {

    private BigDecimal anualBonus;
    private Long       invalidTableWithUnsupportedFieldTypesId;
    private String     name;
    private Instant    timestamp;

    public InvalidTableWithUnsupportedFieldTypes() {
    }

    public BigDecimal getAnualBonus() {
        return anualBonus;
    }

    public void setAnualBonus(BigDecimal anualBonus) {
        this.anualBonus = anualBonus;
    }

    public Long getInvalidTableWithUnsupportedFieldTypesId() {
        return invalidTableWithUnsupportedFieldTypesId;
    }

    public void setInvalidTableWithUnsupportedFieldTypesId(Long id) {
        this.invalidTableWithUnsupportedFieldTypesId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

}
