package com.example.itrade.service.wrapper;

import com.example.itrade.database.model.Candle;
import com.example.itrade.service.Result;
import java.util.List;

public class CandleServiceResult {
    public Result result;
    public List<Candle> candleList;

    public CandleServiceResult(List<Candle> candleList, Result result){
        this.candleList = candleList;
        this.result = result;
    }
}
