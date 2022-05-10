package com.example.itrade.business;

import com.example.itrade.business.calculate.IndicatorCalculator;
import com.example.itrade.business.calculate.PatternRecogniser;
import com.example.itrade.business.calculate.PredictionMaker;
import com.example.itrade.database.Repository;
import com.example.itrade.database.model.Stock;
import com.example.itrade.service.CandleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class Scheduler {
    private static final Logger log = LoggerFactory.getLogger(IndicatorCalculator.class);
    public boolean loaderFinished = false;
    @Autowired
    Repository repository;
    @Autowired
    CandleService candleService;
    @Autowired
    IndicatorCalculator indicatorCalculator;
    @Autowired
    PatternRecogniser patternRecogniser;
    @Autowired
    PredictionMaker predictionMaker;

    // Runs every weekday at 16:15 US/Eastern time (15 minutes after the stock market is closed).
    // Gets new candle data and calculates new indicators, searches for new patterns and calculates new predictions,
    // and saves all this in the database.
    @Scheduled(cron = "0 15 16 * * 1-5", zone = "US/Eastern")
    public synchronized void dailyRequest(){
        if(loaderFinished){
            log.info("Daily request started.");
            List<Stock> allStockList = repository.getAllStocks();
            for(Stock stock: allStockList){
                var fromDate = repository.getLastCandleByStockID(stock.getID()).getDate().plus(1,ChronoUnit.DAYS);
                var untilDate = LocalDate.now(ZoneId.of("US/Eastern"));
                // Sometimes the api gives back data before the request day for some reason, just in case we only save the new candle data to avoid multiplication.
                repository.saveCandles(candleService.getCandlesForStockFromUntil(stock, fromDate, untilDate).stream().filter(candle -> candle.getDate().isAfter(fromDate.minus(1,ChronoUnit.DAYS))).toList());
                repository.saveIndicators(indicatorCalculator.calculateNewIndicatorsForStocks(stock));
                repository.savePatterns(patternRecogniser.findNewPatternsForStocksFrom(stock));
                repository.savePredictions(predictionMaker.makeNewPredictionsForStock(stock));
            }
            log.info("Daily request finished.");
        }else{
            log.info("ERROR! Loader have not finished yet, can not run daily request now.");
        }
    }
}
