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
package com.openglobes.core.trader;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Order status.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Order implements Serializable {

    private Double        amount;
    private ZonedDateTime deleteTimestamp;
    private Boolean       deleted;
    private Integer       direction;
    private ZonedDateTime insertTimestamp;
    private String        instrumentId;
    private Integer       offset;
    private Long          orderId;
    private Double        price;
    private Long          quantity;
    private Integer       status;
    private Integer       statusCode;
    private String        statusMessage;
    private Long          tradedVolumn;
    private Integer       traderId;
    private LocalDate     tradingDay;
    private ZonedDateTime updateTimestamp;

    public Order() {
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public ZonedDateTime getDeleteTimestamp() {
        return deleteTimestamp;
    }

    public void setDeleteTimestamp(ZonedDateTime cancelTimestamp) {
        this.deleteTimestamp = cancelTimestamp;
    }

    public void setDeleted(Boolean isCanceled) {
        this.deleted = isCanceled;

    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public ZonedDateTime getInsertTimestamp() {
        return insertTimestamp;
    }

    public void setInsertTimestamp(ZonedDateTime insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Long getTradedVolumn() {
        return tradedVolumn;
    }

    public void setTradedVolumn(Long tradedVolumn) {
        this.tradedVolumn = tradedVolumn;
    }

    public Integer getTraderId() {
        return traderId;
    }

    public void setTraderId(Integer traderId) {
        this.traderId = traderId;
    }

    public LocalDate getTradingDay() {
        return tradingDay;
    }

    public void setTradingDay(LocalDate tradingDay) {
        this.tradingDay = tradingDay;
    }

    public ZonedDateTime getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(ZonedDateTime updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Boolean isDeleted() {
        return deleted;
    }

}
