package com.example.itrade.database.model;

import javax.persistence.*;

@Entity
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;
    @Column(name="Name")
    private String name;
    @Column(name="Symbol")
    private String symbol;

    public Long getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public Stock(String name, String symbol){
        this.name = name;
        this.symbol = symbol;
    }
    public Stock(){}
}

