/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.market;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Tick {

    private Double askPrice;
    private Long askVolumn;
    private Double bidPrice;
    private Long bidVolumn;
    private String instrumentId;
    private Long openInterest;
    private Double price;
    private Long quantity;
    private Double settlementPrice;
    private Long tickId;
    private ZonedDateTime timestamp;
    private LocalDate tradingDay;
    private Long volumn;

    public Tick() {
    }

    public Double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(Double askPrice) {
        this.askPrice = askPrice;
    }

    public Long getAskVolumn() {
        return askVolumn;
    }

    public void setAskVolumn(Long askVolumn) {
        this.askVolumn = askVolumn;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public Long getBidVolumn() {
        return bidVolumn;
    }

    public void setBidVolumn(Long bidVolumn) {
        this.bidVolumn = bidVolumn;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Long openInterest) {
        this.openInterest = openInterest;
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

    public void setQuantity(Long volumn) {
        this.quantity = volumn;
    }

    public Double getSettlementPrice() {
        return settlementPrice;
    }

    public void setSettlementPrice(Double settlementPrice) {
        this.settlementPrice = settlementPrice;
    }

    public Long getTickId() {
        return tickId;
    }

    public void setTickId(Long tickId) {
        this.tickId = tickId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getTradingDay() {
        return tradingDay;
    }

    public void setTradingDay(LocalDate tradingDay) {
        this.tradingDay = tradingDay;
    }

    public Long getVolumn() {
        return volumn;
    }

    public void setVolumn(Long volumn) {
        this.volumn = volumn;
    }

}
