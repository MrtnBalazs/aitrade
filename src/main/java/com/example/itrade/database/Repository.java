package com.example.itrade.database;

import com.example.itrade.database.dao.*;
import com.example.itrade.database.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.*;

@Component
public class Repository {
    private static final Logger log = LoggerFactory.getLogger(Repository.class);
    @Autowired
    StockRepository stockRepository;
    @Autowired
    CandleRepository candleRepository;
    @Autowired
    IndicatorRepository indicatorRepository;
    @Autowired
    PatternRepository patternRepository;
    @Autowired
    PredictionRepository predictionRepository;
    @Autowired
    LogRepository logRepository;

    public void deleteDatabase(){
        log.info("Deleting database...");
        candleRepository.deleteAll();
        indicatorRepository.deleteAll();
        patternRepository.deleteAll();
        logRepository.deleteAll();
        predictionRepository.deleteAll();
        stockRepository.deleteAll();
    }

    public void deleteStockById(Long ID){
        stockRepository.deleteById(ID);
    }

    public List<Candle> getLast20CandleByStockID(Long stockID){
        var candleList = candleRepository.findLast20ByStockID(stockID);
        if(candleList == null)
            return null;
        Collections.reverse(candleList);
        return candleList;
    }

    public Candle getLastCandleByStockID(Long stockID){
        return candleRepository.findLastByStockID(stockID);
    }

    public List<Candle> getCandlesForStockFrom(Long stockID, LocalDate from){
        return candleRepository.findFromDateAndStockID(from, stockID);
    }

    public List<Candle> getAllCandlesForStock(Long stockID){
        return candleRepository.findAllByStockID(stockID);
    }

    public List<Prediction> getLast20PredictionByStockID(Long stockID){
        var predictionList = predictionRepository.findLast20ByStockID(stockID);
        if(predictionList == null)
            return null;
        Collections.reverse(predictionList);
        return predictionList;
    }

    public List<Prediction> getAllPredictionsForStock(Long stockID){
        return predictionRepository.findAllByStockID(stockID);
    }

    public Indicator getLastIndicatorByStockID(Long stockID){
        return indicatorRepository.findLastByStockID(stockID);
    }

    public List<Indicator> getAllIndicatorsForStock(Long stockID){
        return indicatorRepository.findAllByStockID(stockID);
    }

    public Indicator getIndicatorByDateAndStock(Long stockID, LocalDate date){
        return indicatorRepository.findByDateAndStockID(date, stockID);
    }

    public List<Indicator> getIndicatorsForStockFrom(Long stockID, LocalDate from){
        return indicatorRepository.findFromDateAndStockID(from, stockID);
    }

    public List<Pattern> getPatternsForStockFrom(Long stockID, LocalDate fromDate){
        return patternRepository.findFromDateAndStockID(fromDate, stockID);
    }

    public List<Pattern> getAllPatternsForStock(Long stockID){
        return patternRepository.findAllByStockID(stockID);
    }

    public List<Log> getAllLogs(){
        return logRepository.findAll();
    }

    public Log getFirstLog(){
        return logRepository.findFirstLog();
    }

    public Stock getStockById(Long stockID){
        Optional<Stock> stockOption = stockRepository.findByID(stockID);
        if(stockOption.isPresent()){
            return stockOption.get();
        }else{
            log.info("Could not find stock with id: " + stockID + ", returning null.");
            return null;
        }
    }

    public List<Stock> getAllStocks(){
        return stockRepository.findAll();
    }


    public Stock getStockBySymbol(String symbol){
        return stockRepository.findBySymbol(symbol);
    }

    public void saveStock(Stock s){
        log.info("Saving stock: " + s.getSymbol());
        stockRepository.save(s);
    }

    public void saveStocks(List<Stock> stockList){
        log.info("Saving stocks..");
        stockRepository.saveAll(stockList);
    }

    public void saveCandles(List<Candle> candlesList){
        if(candlesList == null || candlesList.size() == 0)
            return;
        log.info("Saving candles..");
        candleRepository.saveAll(candlesList);
    }

    public void saveLog(Log log){
        logRepository.save(log);
    }

    public void savePredictions(List<Prediction> predictionList){
        log.info("Saving predictions..");
        if(predictionList == null || predictionList.size() == 0)
            return;
        predictionRepository.saveAll(predictionList);
    }

    public void saveIndicators(List<Indicator> indicatorList){
        log.info("Saving indicators..");
        if(indicatorList == null || indicatorList.size() == 0)
            return;
        indicatorRepository.saveAll(indicatorList);
    }

    public void savePatterns(List<Pattern> patternList){
        log.info("Saving patterns..");
        if(patternList == null || patternList.size() == 0)
            return;
        patternRepository.saveAll(patternList);
    }
}
