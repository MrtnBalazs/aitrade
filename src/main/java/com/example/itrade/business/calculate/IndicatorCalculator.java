package com.example.itrade.business.calculate;

import com.example.itrade.database.Repository;
import com.example.itrade.database.model.Candle;
import com.example.itrade.database.model.Indicator;
import com.example.itrade.database.model.Rsi;
import com.example.itrade.database.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class IndicatorCalculator {
    private static final Logger log = LoggerFactory.getLogger(IndicatorCalculator.class);
    @Autowired
    Repository repository;

    public List<Indicator> calculateNewIndicatorsForStocks(Stock stock){
        List<Indicator> indicatorList = new ArrayList<>();
        Indicator lastIndicator = repository.getLastIndicatorByStockID(stock.getID());
        if(lastIndicator == null)
            return indicatorList;
        List<Candle> candles = repository.getCandlesForStockFrom(stock.getID(), lastIndicator.getDate());
        if(candles == null || candles.isEmpty())
            return indicatorList;
        log.info("Calculating new indicators for stock: " + stock.getSymbol() + ", from: " + lastIndicator.getDate().plus(1, ChronoUnit.DAYS));
        List<Double> closingPrices = getClosingPricesFromCandles(candles);
        List<Double> ema12 = calculateEma(12, lastIndicator.getEMA12(), closingPrices.subList(1,closingPrices.size()));
        List<Double> ema26 = calculateEma(26, lastIndicator.getEMA26(), closingPrices.subList(1,closingPrices.size()));
        List<Double> ema100 = calculateEma(100, lastIndicator.getEMA26(), closingPrices.subList(1,closingPrices.size()));
        List<Double> macd = calculateMacd(ema12, ema26);
        List<Double> signal = calculateEma(9, lastIndicator.getSignal(), macd);
        List<Rsi> rsi = calculateRsiWithHistory(14, lastIndicator.getAvgGain(), lastIndicator.getAvgLoss(), closingPrices);
        for(int i = 0; i < signal.size(); i++){
            indicatorList.add(new Indicator(
                    ema12.get(i),
                    ema26.get(i),
                    ema100.get(i),
                    macd.get(i),
                    signal.get(i),
                    rsi.get(i),
                    candles.get(i + candles.size() - signal.size()).getDate(),
                    stock.getID())
            );
        }
        return indicatorList;
    }

    public List<Indicator> initializeIndicatorsForStocks(Stock stock){
        log.info("Calculating indicators for stock: " + stock.getSymbol());
        List<Indicator> indicatorList = new ArrayList<>();
        List<Candle> candles = repository.getAllCandlesForStock(stock.getID());
        if(candles == null || candles.isEmpty())
            return indicatorList;
        List<Double> closingPrices = getClosingPricesFromCandles(candles);
        List<Double> ema12 = calculateEma(12, calculateSma(12, closingPrices), closingPrices.subList(12, closingPrices.size()));
        List<Double> ema26 = calculateEma(26, calculateSma(26, closingPrices), closingPrices.subList(26, closingPrices.size()));
        List<Double> ema100 = calculateEma(100, calculateSma(100, closingPrices), closingPrices.subList(100, closingPrices.size()));
        List<Double> macd = calculateMacd(ema12, ema26);
        List<Double> signal = calculateEma(9, calculateSma(9, macd), macd.subList(12, macd.size()));
        List<Rsi> rsi = calculateRsiFirstTime(14, closingPrices);
        for(int i = 0; i < ema100.size(); i++){
            indicatorList.add(new Indicator(
                    ema12.get(i + ema12.size() - ema100.size()),
                    ema26.get(i + ema26.size() - ema100.size()),
                    ema100.get(i),
                    macd.get(i + macd.size() - ema100.size()),
                    signal.get(i + signal.size() - ema100.size()),
                    rsi.get(i + rsi.size() - ema100.size()),
                    candles.get(i + candles.size() - ema100.size()).getDate(),
                    stock.getID())
            );
        }
        return indicatorList;
    }

    private Double calculateSma(int period, List<Double> doubleList){
        Double sum = 0.0;
        for(int i = 0; i < period; i++){
            sum+=doubleList.get(i);
        }
        return sum/period;
    }

    private List<Double> calculateEma(int period, Double emaYesterday, List<Double> doubleList){
        List<Double> ema = new ArrayList<>();
        Double multiplier = (2 / ((double)period + 1));
        for(int i = 0; i<doubleList.size();i++){
            Double emaToday = doubleList.get(i) * multiplier + emaYesterday * (1.0 - multiplier);
            ema.add(emaToday);
            emaYesterday = emaToday;
        }
        return ema;
    }

    private List<Double> calculateMacd(List<Double> ema12, List<Double> ema26){
        List<Double> macd = new ArrayList<>();
        for(int i = 0; i < ema26.size(); i++){
            macd.add(ema12.get(i + (ema12.size()-ema26.size())) - ema26.get(i));
        }
        return macd;
    }

    private List<Rsi> calculateRsiFirstTime(int period, List<Double> closingPrices){
        Double gain = 0.0;
        Double loss = 0.0;
        for(int i = 1; i < period; i++){
            if(closingPrices.get(i - 1) < closingPrices.get(i)){
                gain += closingPrices.get(i) - closingPrices.get(i - 1);
            }else if(closingPrices.get(i - 1) > closingPrices.get(i)){
                loss += closingPrices.get(i - 1) - closingPrices.get(i);
            }
        }
        double avgGain = gain / period;
        double avgLoss = loss / period;
        return calculateRsiWithHistory(period,avgGain,avgLoss,closingPrices);
    }

    private List<Rsi> calculateRsiWithHistory(int period, Double oldAvgGain, Double oldAvgLoss, List<Double> closingPrices){
        List<Rsi> rsiList = new ArrayList<>();
        Double avgGain = oldAvgGain;
        Double avgLoss = oldAvgLoss;
        for(int i = 1; i < closingPrices.size(); i++){
            if(closingPrices.get(i) > closingPrices.get(i - 1)){
                avgGain = (avgGain * (period - 1) + (closingPrices.get(i) - closingPrices.get(i - 1))) / period;
                avgLoss = (avgLoss * (period - 1)) / period;
            }else if(closingPrices.get(i) < closingPrices.get(i - 1)){
                avgLoss = (avgLoss * (period - 1) + (closingPrices.get(i - 1) - closingPrices.get(i))) / period;
                avgGain = (avgGain * (period - 1) ) / period;
            }
            double rs = avgGain / avgLoss;
            if(avgLoss == 0.0)
                rsiList.add(new Rsi(100.0, avgGain, avgLoss));
            else
                rsiList.add(new Rsi(100 - (100 / (1 + rs)), avgGain, avgLoss));
        }
        return rsiList;
    }

    private List<Double> getClosingPricesFromCandles(List<Candle> candleList){
        List<Double> closingPrices = new ArrayList<>();
        for(Candle c: candleList){
            closingPrices.add(c.getClose());
        }
        return closingPrices;
    }
}
