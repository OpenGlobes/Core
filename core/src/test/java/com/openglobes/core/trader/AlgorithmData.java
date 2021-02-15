package com.openglobes.core.trader;

import com.openglobes.core.utils.Utils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AlgorithmData {
    private final Collection<Deposit> deposits = new HashSet<>();
    private final Collection<Withdraw> withdraws = new HashSet<>();
    private final Map<String, Instrument> instruments = new HashMap<>();
    private final ITraderEngineAlgorithm algorithm = new DefaultTraderEngineAlgorithm();

    protected AlgorithmData() {
        setDeposits();
        setWithdraws();
        setInstruments();
    }

    private void setWithdraws() {
        Withdraw w = new Withdraw();
        w.setTradingDay(LocalDate.now());

        w.setAmount(1000.0);
        w.setTimestamp(ZonedDateTime.now());
        w.setWithdrawId(1L);
        withdraws.add(w);

        w = Utils.copy(w);
        w.setAmount(500.0);
        w.setTimestamp(ZonedDateTime.now());
        w.setWithdrawId(2L);
        withdraws.add(w);

        w = Utils.copy(w);
        w.setAmount(600.0);
        w.setTimestamp(ZonedDateTime.now());
        w.setWithdrawId(3L);
        withdraws.add(w);
    }

    private void setDeposits() {
        Deposit d = new Deposit();
        d.setTradingDay(LocalDate.now());

        d.setAmount(4000.0);
        d.setTimestamp(ZonedDateTime.now());
        d.setDepositId(1L);
        deposits.add(d);

        d = Utils.copy(d);
        d.setAmount(2500.0);
        d.setTimestamp(ZonedDateTime.now());
        d.setDepositId(2L);
        deposits.add(d);
    }

    protected Collection<Deposit> deposits() {
        return deposits;
    }

    protected Collection<Withdraw> withdraws() {
        return withdraws;
    }

    protected Instrument instrument(String instrumentId) {
        return instruments.get(instrumentId);
    }

    protected ITraderEngineAlgorithm algorithm() {
        return algorithm;
    }

    private void setInstruments() {
        Instrument i = new Instrument();

        i.setCommissionCloseRatio(0.0);
        i.setCommissionCloseTodayRatio(1.2);
        i.setInstrumentId("c2105");
        i.setTimestamp(ZonedDateTime.now());
        i.setEndDate(LocalDate.of(2021,
                                  5,
                                  15));
        i.setCommissionType(RatioType.BY_VOLUMN);
        i.setCommissionOpenRatio(1.2);
        i.setExchangeId("DCE");
        i.setMarginRatio(.09);
        i.setMarginType(RatioType.BY_MONEY);
        i.setMultiple(10L);
        i.setPriceTick(1.0);
        i.setStartDate(LocalDate.of(2020,
                                    5,
                                    1));
        instruments.put("c2105",
                        i);

        i = Utils.copy(i);
        i.setInstrumentId("c2109");
        i.setEndDate(LocalDate.of(2021,
                                  9,
                                  15));
        i.setStartDate(LocalDate.of(2020,
                                    9,
                                    1));
        instruments.put("c2109",
                        i);

        i = Utils.copy(i);
        i.setInstrumentId("x2109");
        i.setCommissionOpenRatio(.09);
        i.setCommissionCloseRatio(0.0);
        i.setCommissionCloseTodayRatio(.09);
        i.setCommissionType(RatioType.BY_MONEY);
        i.setMarginRatio(1234.0);
        i.setMarginType(RatioType.BY_VOLUMN);
        i.setEndDate(LocalDate.of(2021,
                                  9,
                                  15));
        i.setStartDate(LocalDate.of(2020,
                                    9,
                                    1));
        instruments.put("x2109",
                        i);
    }
}
