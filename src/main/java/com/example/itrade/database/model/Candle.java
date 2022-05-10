package com.example.itrade.database.model;

import javax.persistence.Entity;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Candle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ID;
    @Column(name="`Open`")
    Double open;
    @Column(name="`Close`")
    Double close;
    @Column(name="High")
    Double high;
    @Column(name="Low")
    Double low;
    @Column(name="Date")
    LocalDate date;
    Long stockID;

    public Long getID() {
        return ID;
    }

    public Double getOpen() {
        return open;
    }

    public Double getClose() {
        return close;
    }

    public Double getHigh() {
        return high;
    }

    public Double getLow() {
        return low;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getStockID() {
        return stockID;
    }

    public Candle(Double open, Double close, Double high, Double low,LocalDate date, Long stockID){
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.date = date;
        this.stockID = stockID;
    }
    public Candle(){}
}
