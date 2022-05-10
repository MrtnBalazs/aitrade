package com.example.itrade.database.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Pattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ID;
    @Column(name="Name")
    String name;
    @Column(name="Date")
    LocalDate date;
    @Column(name="Strength")
    int strength;
    Long stockID;

    public Long getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getStrength() {
        return strength;
    }

    public Long getStockID() {
        return stockID;
    }

    public Pattern(String name, LocalDate date, Long stockID, int strength){
        this.name = name;
        this.date = date;
        this.stockID = stockID;
        this.strength = strength;
    }
    public Pattern(){}
}
