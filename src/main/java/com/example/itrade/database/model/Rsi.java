package com.example.itrade.database.model;

public class Rsi {
    Double RSI;
    Double avgGain;
    Double avgLoss;

    public Rsi (Double RSI, Double avgGain, Double avgLoss){
        this.RSI = RSI;
        this.avgGain = avgGain;
        this.avgLoss = avgLoss;
    }
}
