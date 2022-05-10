package com.example.itrade.database.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ID;
    @Column(name="Strength")
    int strength;
    @Column(name="Date")
    LocalDate date;
    Long stockID;

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public Long getID() {
        return ID;
    }

    public int getStrength() {
        return strength;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getStockID() {
        return stockID;
    }

    public Prediction(int strength, LocalDate date, Long stockID){
        this.strength = strength;
        this.date = date;
        this.stockID = stockID;
    }
    public Prediction(){}
}
