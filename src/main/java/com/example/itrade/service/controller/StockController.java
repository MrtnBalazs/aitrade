package com.example.itrade.service.controller;

import com.example.itrade.database.Repository;
import com.example.itrade.database.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StockController {
    private final Repository repository;
    StockController(Repository repository){
        this.repository = repository;
    }

    @GetMapping("/stocks")
    ResponseEntity<List<Stock>> getAllStocks() {
        var stockList = repository.getAllStocks();
        if(stockList == null)
            return ResponseEntity.status(404).body(null);
        else if (stockList.isEmpty())
            return ResponseEntity.status(204).body(null);
        else
            return ResponseEntity.status(200).body(stockList);
    }
    @GetMapping("/stocks/{ID}")
    ResponseEntity<Stock> getStock(@PathVariable long ID) {
        var stock = repository.getStockById(ID);
        if(stock == null)
            return ResponseEntity.status(404).body(null);
        else
            return ResponseEntity.status(200).body(stock);
    }
    @GetMapping("/candles/{stockID}")
    ResponseEntity<List<Candle>> getAllCandlesForStock(@PathVariable long stockID) {
        var candleList = repository.getAllCandlesForStock(stockID);
        if(candleList == null)
            return ResponseEntity.status(404).body(null);
        else if(candleList.isEmpty())
            return ResponseEntity.status(204).body(null);
        else
            return ResponseEntity.status(200).body(candleList);
    }
    @GetMapping("/indicators/{stockID}")
    ResponseEntity<List<Indicator>> getAllIndicatorsForStock(@PathVariable long stockID) {
        var indicatorList = repository.getAllIndicatorsForStock(stockID);
        if(indicatorList == null)
            return ResponseEntity.status(404).body(null);
        else if(indicatorList.isEmpty())
            return ResponseEntity.status(204).body(null);
        else
            return ResponseEntity.status(200).body(indicatorList);
    }
    @GetMapping("/patterns/{stockID}")
    ResponseEntity<List<Pattern>> getAllPatternsForStock(@PathVariable long stockID) {
        var patternList = repository.getAllPatternsForStock(stockID);
        if(patternList == null)
            return ResponseEntity.status(404).body(null);
        else if(patternList.isEmpty())
            return ResponseEntity.status(204).body(null);
        else
            return ResponseEntity.status(200).body(patternList);
    }
    @GetMapping("/predictions/{stockID}")
    ResponseEntity<List<Prediction>> getAllPredictionsForStock(@PathVariable long stockID) {
        var predictionList = repository.getAllPredictionsForStock(stockID);
        if(predictionList == null)
            return ResponseEntity.status(404).body(null);
        else if(predictionList.isEmpty())
            return ResponseEntity.status(204).body(null);
        else
            return ResponseEntity.status(200).body(predictionList);
    }
    @GetMapping("/logs")
    ResponseEntity<List<Log>> getAllPredictionsForStock() {
        var logList = repository.getAllLogs();
        if(logList == null)
            return ResponseEntity.status(404).body(null);
        else if(logList.isEmpty())
            return ResponseEntity.status(204).body(null);
        else
            return ResponseEntity.status(200).body(logList);
    }
}
