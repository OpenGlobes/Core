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
 * aLong with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.openglobes.core;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Instrument.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Instrument {

    private Double commissionCloseRatio;
    private Double commissionCloseTodayRatio;
    private Double commissionOpenRatio;
    private RatioType commissionType;
    private LocalDate endDate;
    private String exchangeId;
    private String instrumentId;
    private Double marginRatio;
    private RatioType marginType;
    private Long multiple;
    private Double priceTick;
    private LocalDate startDate;
    private ZonedDateTime timestamp;

    public Instrument() {
    }

    public Double getCommissionCloseRatio() {
        return commissionCloseRatio;
    }

    public void setCommissionCloseRatio(Double commissionCloseRatio) {
        this.commissionCloseRatio = commissionCloseRatio;
    }

    public Double getCommissionCloseTodayRatio() {
        return commissionCloseTodayRatio;
    }

    public void setCommissionCloseTodayRatio(Double commissionCloseTodayRatio) {
        this.commissionCloseTodayRatio = commissionCloseTodayRatio;
    }

    public Double getCommissionOpenRatio() {
        return commissionOpenRatio;
    }

    public void setCommissionOpenRatio(Double commissionOpenRatio) {
        this.commissionOpenRatio = commissionOpenRatio;
    }

    public RatioType getCommissionType() {
        return commissionType;
    }

    public void setCommissionType(RatioType commissionType) {
        this.commissionType = commissionType;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Double getMarginRatio() {
        return marginRatio;
    }

    public void setMarginRatio(Double marginRatio) {
        this.marginRatio = marginRatio;
    }

    public RatioType getMarginType() {
        return marginType;
    }

    public void setMarginType(RatioType marginType) {
        this.marginType = marginType;
    }

    public Long getMultiple() {
        return multiple;
    }

    public void setMultiple(Long multiple) {
        this.multiple = multiple;
    }

    public Double getPriceTick() {
        return priceTick;
    }

    public void setPriceTick(Double priceTick) {
        this.priceTick = priceTick;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
