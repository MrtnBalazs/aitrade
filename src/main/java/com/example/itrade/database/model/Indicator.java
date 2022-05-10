package com.example.itrade.database.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Indicator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ID;
    Double EMA12;
    Double EMA26;
    Double EMA100;
    Double MACD;
    @Column(name="Signal")
    Double signal;
    Double RSI;
    @Column(name="Average_Gain")
    Double avgGain;
    @Column(name="Average_Loss")
    Double avgLoss;
    @Column(name="Date")
    LocalDate date;
    Long stockID;

    public Double getEMA100() {return EMA100;}

    public Long getID() {
        return ID;
    }

    public Double getEMA12() {
        return EMA12;
    }

    public Double getEMA26() {
        return EMA26;
    }

    public Double getMACD() {
        return MACD;
    }

    public Double getSignal() {
        return signal;
    }

    public Double getRSI() {
        return RSI;
    }

    public Double getAvgGain() {
        return avgGain;
    }

    public Double getAvgLoss() {
        return avgLoss;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getStockID() {
        return stockID;
    }

    public Indicator(Double EMA12, Double EMA26, Double EMA100, Double MACD, Double signal, Rsi rsi, LocalDate date, Long stockID){
        this.EMA12 = EMA12;
        this.EMA26 = EMA26;
        this.EMA100 = EMA100;
        this.MACD = MACD;
        this.signal = signal;
        this.RSI = rsi.RSI;
        this.avgGain = rsi.avgGain;
        this.avgLoss = rsi.avgLoss;
        this.date = date;
        this.stockID = stockID;
    }
    public Indicator(){}
}
