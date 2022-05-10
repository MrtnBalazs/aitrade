package com.example.itrade.business.calculate;

import com.example.itrade.database.Repository;
import com.example.itrade.database.model.Candle;
import com.example.itrade.database.model.Pattern;
import com.example.itrade.database.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class PatternRecogniser {
    private static final Logger log = LoggerFactory.getLogger(IndicatorCalculator.class);
    @Autowired
    Repository repository;
    int TLS_STRENGTH = 1;
    double TLS_CLOSING_PERCENTAGE_RANGE_TO_LOW = 1.5;

    int TBG_STRENGTH = -1;
    int TBG_UPTREND_LENGTH = 6;
    double TBG_UPTREND_PERCENTAGE_CHANGE = 102.0;
    int TBG_UPTREND_EXCEPTION_LIMIT = 0;
    double TBG_FALL_GAP_PERCENT = 99.0;

    int TBC_STRENGTH = -1;
    int TBC_UPTREND_LENGTH = 6;
    double TBC_UPTREND_PERCENTAGE_CHANGE = 102.0;
    int TBC_UPTREND_EXCEPTION_LIMIT = 0;
    double TBC_FALL_PERCENT = 96.0;

    int ES_STRENGTH = -1;
    double ES_NARROW_RANGE_PERCENT = 0.5;
    double ES_RISE_PERCENT = 104.0;
    double ES_FALL_PERCENT = 98.0;

    int AB_STRENGTH = 1;
    int AB_DOWNTREND_LENGTH = 5;
    double AB_DOWNTREND_PERCENT_CHANGE = 98.0;
    int AB_UPTREND_EXCEPTION_LIMIT = 1;
    double AB_NARROW_RANGE_PERCENT = 0.8;
    double AB_RISE_PERCENT = 103.5;

    public List<Pattern> findNewPatternsForStocksFrom(Stock stock){
        List<Pattern> patterns = new ArrayList<>();
        // To find all the new patterns we need some old candle data to recognise the patterns which started before, and are now finished in the new data.
        // We need to get the last couple of candles by number not date, because some stocks may miss candle data here and there.
        var lastCandles = repository.getLast20CandleByStockID(stock.getID());
        if(lastCandles == null || lastCandles.size() < 20)
            return patterns;
        List<Candle> candles = repository.getCandlesForStockFrom(stock.getID(), lastCandles.get(0).getDate());
        if(candles == null)
            return patterns;
        log.info("Finding patterns for stocks from " + lastCandles.get(lastCandles.size() - 1).getDate());
        var tempPatterns = new ArrayList<Pattern>();
        tempPatterns.addAll(findThreeLineStrike(stock, candles, TLS_CLOSING_PERCENTAGE_RANGE_TO_LOW, TLS_STRENGTH));
        tempPatterns.addAll(findTwoBlackGapping(stock, candles, TBG_UPTREND_LENGTH, TBG_UPTREND_PERCENTAGE_CHANGE, TBG_UPTREND_EXCEPTION_LIMIT, TBG_FALL_GAP_PERCENT, TBG_STRENGTH));
        tempPatterns.addAll(findThreeBlackCrows(stock, candles, TBC_UPTREND_LENGTH, TBC_UPTREND_PERCENTAGE_CHANGE, TBC_UPTREND_EXCEPTION_LIMIT, TBC_FALL_PERCENT, TBC_STRENGTH));
        tempPatterns.addAll(findEveningStar(stock, candles, ES_NARROW_RANGE_PERCENT, ES_RISE_PERCENT, ES_FALL_PERCENT, ES_STRENGTH));
        tempPatterns.addAll(findAbandonedBaby(stock, candles, AB_DOWNTREND_LENGTH, AB_DOWNTREND_PERCENT_CHANGE, AB_UPTREND_EXCEPTION_LIMIT, AB_NARROW_RANGE_PERCENT, AB_RISE_PERCENT, AB_STRENGTH));
        // Because we search in old data too, we might find patterns that have already been found, so we only give back the new ones.
        patterns.addAll(tempPatterns.stream().filter(pattern -> pattern.getDate().isAfter(lastCandles.get(lastCandles.size() - 1).getDate())).toList());
        return patterns;
    }

    public List<Pattern> findAllPatternsForStocks(Stock stock){
        List<Pattern> patterns = new ArrayList<>();
        List<Candle> candles = repository.getAllCandlesForStock(stock.getID());
        if(candles == null || candles.isEmpty())
            return patterns;
        log.info("Finding all patterns for stock: " + stock.getSymbol());
        patterns.addAll(findThreeLineStrike(stock, candles, TLS_CLOSING_PERCENTAGE_RANGE_TO_LOW, TLS_STRENGTH));
        patterns.addAll(findTwoBlackGapping(stock, candles, TBG_UPTREND_LENGTH, TBG_UPTREND_PERCENTAGE_CHANGE, TBG_UPTREND_EXCEPTION_LIMIT, TBG_FALL_GAP_PERCENT, TBG_STRENGTH));
        patterns.addAll(findThreeBlackCrows(stock, candles, TBC_UPTREND_LENGTH, TBC_UPTREND_PERCENTAGE_CHANGE, TBC_UPTREND_EXCEPTION_LIMIT, TBC_FALL_PERCENT, TBC_STRENGTH));
        patterns.addAll(findEveningStar(stock, candles, ES_NARROW_RANGE_PERCENT, ES_RISE_PERCENT, ES_FALL_PERCENT, ES_STRENGTH));
        patterns.addAll(findAbandonedBaby(stock, candles, AB_DOWNTREND_LENGTH, AB_DOWNTREND_PERCENT_CHANGE, AB_UPTREND_EXCEPTION_LIMIT, AB_NARROW_RANGE_PERCENT, AB_RISE_PERCENT, AB_STRENGTH));
        return patterns;
    }

    private List<Pattern> findThreeLineStrike(
            Stock s,
            List<Candle> candles,
            double closingPercentRangeToLow,
            int strength
    ){
        List<Pattern> patterns = new ArrayList<>();
        for(int i = 0; i < candles.size()-3;i++){
            if(
                    !isGreenCandle(candles.get(i)) &&
                            !isGreenCandle(candles.get(i + 1)) &&
                            !isGreenCandle(candles.get(i + 2)) &&
                            isGreenCandle(candles.get(i + 3)) &&
                            isInPercentRange(candles.get(i).getLow(),candles.get(i).getClose(), closingPercentRangeToLow) &&
                            isInPercentRange(candles.get(i + 1).getLow(),candles.get(i + 1).getClose(), closingPercentRangeToLow) &&
                            isInPercentRange(candles.get(i + 2).getLow(),candles.get(i + 2).getClose(), closingPercentRangeToLow) &&
                            (candles.get(i).getLow() > candles.get(i + 1).getLow()) &&
                            (candles.get(i + 1).getLow() > candles.get(i + 2).getLow()) &&
                            (candles.get(i + 2).getLow() > candles.get(i + 3).getOpen()) &&
                            (candles.get(i).getHigh() < candles.get(i + 3).getClose())
            ){
                patterns.add(new Pattern("Three Line Strike", candles.get(i + 3).getDate(),s.getID(), strength));
            }
        }
        return patterns;
    }

    private List<Pattern> findTwoBlackGapping(
            Stock s,
            List<Candle> candles,
            int upTrendLength,
            double uptrendPercentChange,
            int trendExceptionLimit,
            double fallGapPercent,
            int strength
    ){
        List<Pattern> patterns = new ArrayList<>();
        for(int i = 0; i < candles.size() - (upTrendLength + 1); i++){
            if(
                    isUpTrend(candles.subList(i, i + upTrendLength), trendExceptionLimit, uptrendPercentChange) &&
                            !isGreenCandle(candles.get(i + upTrendLength)) &&
                            !isGreenCandle(candles.get(i + upTrendLength + 1)) &&
                            (candles.get(i + upTrendLength).getLow() > candles.get(i + upTrendLength + 1).getHigh()) &&
                            (isLowerThanPercent(candles.get(i + upTrendLength).getLow(), candles.get(i + upTrendLength + 1).getHigh(), fallGapPercent))
            ){
                patterns.add(new Pattern("Two Black Gapping", candles.get(i + upTrendLength + 1).getDate(),s.getID(), strength));
            }
        }
        return patterns;
    }

    private List<Pattern> findThreeBlackCrows(
            Stock s,
            List<Candle> candles,
            int upTrendLength,
            double uptrendPercentChange,
            int trendExceptionLimit,
            double fallPercent,
            int strength
    ){
        List<Pattern> patterns = new ArrayList<>();
        for(int i = 0; i < candles.size() - (upTrendLength + 2); i++){
            if(
                    isUpTrend(candles.subList(i, i + upTrendLength), trendExceptionLimit, uptrendPercentChange) &&
                            (!isGreenCandle(candles.get(i + upTrendLength))) &&
                            (!isGreenCandle(candles.get(i + upTrendLength + 1))) &&
                            (!isGreenCandle(candles.get(i + upTrendLength + 2))) &&
                            (candles.get(i + upTrendLength).getLow() > candles.get(i + upTrendLength + 1).getLow()) &&
                            (candles.get(i + upTrendLength + 1).getLow() > candles.get(i + upTrendLength + 2).getLow()) &&
                            (isLowerThanPercent(candles.get(i + upTrendLength).getClose(), candles.get(i + upTrendLength + 2).getClose(), fallPercent))
            ){
                patterns.add(new Pattern("Three Black Crows", candles.get(i + upTrendLength + 2).getDate(),s.getID(), strength));
            }
        }
        return patterns;
    }

    private List<Pattern> findEveningStar(
            Stock s,
            List<Candle> candles,
            double narrowRangePercent,
            double risePercent,
            double fallPercent,
            int strength){
        List<Pattern> patterns = new ArrayList<>();
        for (int i = 0; i < candles.size() - 2; i++) {
            if (
                    isGreenCandle(candles.get(i)) &&
                            !isGreenCandle(candles.get(i + 2)) &&
                            isInPercentRange(candles.get(i + 1).getOpen(), candles.get(i + 1).getClose(), narrowRangePercent) &&
                            isHigherThanPercent(candles.get(i).getOpen(), candles.get(i).getClose(), risePercent) &&
                            isLowerThanPercent(candles.get(i + 2).getOpen(), candles.get(i + 2).getClose(), fallPercent) &&
                            (candles.get(i).getClose() < candles.get(i + 1).getLow()) &&
                            (candles.get(i + 1).getLow() > candles.get(i + 2).getOpen())
            ) {
                patterns.add(new Pattern("Evening Star", candles.get(i + 2).getDate(), s.getID(), strength));
            }
        }
        return patterns;
    }

    private List<Pattern> findAbandonedBaby(
            Stock s,
            List<Candle> candles,
            int downTrendLength,
            double downTrendPercentageChange,
            int trendExceptionLimit,
            double narrowRangePercent,
            double risePercent,
            int strength
    ){
        List<Pattern> patterns = new ArrayList<>();
        for (int i = 0; i < candles.size() - (downTrendLength + 1); i++) {
            if (
                    isDownTrend(candles.subList(i, i + downTrendLength), trendExceptionLimit, downTrendPercentageChange) &&
                            isGreenCandle(candles.get(i + downTrendLength + 1)) &&
                            isInPercentRange(candles.get(i + downTrendLength).getOpen(), candles.get(i + downTrendLength).getClose(), narrowRangePercent) &&
                            isHigherThanPercent(candles.get(i + downTrendLength + 1).getOpen(), candles.get(i + downTrendLength + 1).getClose(), risePercent) &&
                            (candles.get(i + downTrendLength - 1).getClose() > candles.get(i + downTrendLength).getHigh()) &&
                            (candles.get(i + downTrendLength).getHigh() < candles.get(i + downTrendLength + 1).getOpen())
            ) {
                patterns.add(new Pattern("Abandoned Baby", candles.get(i + downTrendLength + 1).getDate(), s.getID(), strength));
            }
        }
        return patterns;
    }

    // Trend exception limit means: how many candles can fall out of line in the trend, so that one or two miss behaving candles
    // don t ruin our recognition of the trend.
    private boolean isUpTrend(List<Candle> candles, int trendExceptionLimit, double uptrendPercentChange){
        int trendExceptionCounter = 0;
        if(!isHigherThanPercent(candles.get(0).getClose(), candles.get(candles.size() - 1).getClose(), uptrendPercentChange))
            return false;
        for(int i = 0; i < candles.size() - 1; i++){
            if((candles.get(i).getClose() > candles.get(i + 1).getClose()) || (candles.get(i).getHigh() > candles.get(i + 1).getHigh())){
                if(trendExceptionCounter == trendExceptionLimit) {
                    return false;
                }
                trendExceptionCounter++;
            }
        }
        return true;
    }

    private boolean isDownTrend(List<Candle> candles, int trendExceptionLimit, double downTrendPercentageChange){
        int trendExceptionCounter = 0;
        if(!isLowerThanPercent(candles.get(0).getClose(), candles.get(candles.size() - 1).getClose(), downTrendPercentageChange))
            return false;
        for(int i = 0; i < candles.size() - 1; i++){
            if((candles.get(i).getClose() < candles.get(i + 1).getClose()) || (candles.get(i).getLow() < candles.get(i + 1).getLow())){
                if(trendExceptionCounter == trendExceptionLimit) {
                    return false;
                }
                trendExceptionCounter++;
            }
        }
        return true;
    }

    private boolean isGreenCandle(Candle c){
        return (c.getClose() > c.getOpen());
    }

    private boolean isInPercentRange(Double a, Double b, Double percent){
        return (b < (a * (1.0 + percent / 100))) && (b > (a * (1.0 - percent / 100)));
    }

    private boolean isHigherThanPercent(Double a, Double b, Double percent){
        if((a * (percent / 100)) < b){
            return true;
        }
        return false;
    }

    private boolean isLowerThanPercent(Double a, Double b, Double percent){
        if((a * (percent / 100)) > b){
            return true;
        }
        return false;
    }
}
