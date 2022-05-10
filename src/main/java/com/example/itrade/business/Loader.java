package com.example.itrade.business;

import com.example.itrade.business.calculate.IndicatorCalculator;
import com.example.itrade.business.calculate.PatternRecogniser;
import com.example.itrade.business.calculate.PredictionMaker;
import com.example.itrade.database.Repository;
import com.example.itrade.database.model.Log;
import com.example.itrade.database.model.Stock;
import com.example.itrade.service.CandleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import static com.example.itrade.business.StartMode.*;

@Component
public class Loader {
    String SETTINGS_FILE_NAME = "settings.txt";
    int MIN_REQUIRED_CANDLE_NUMBER = 110; // We need minimum 100 candles for calculating ema for 100 period.
    Settings settings;
    private static final Logger log = LoggerFactory.getLogger(Loader.class);
    @Autowired
    Repository repository;
    @Autowired
    Scheduler scheduler;
    @Autowired
    IndicatorCalculator indicatorCalculator;
    @Autowired
    CandleService candleService;
    @Autowired
    PatternRecogniser patternRecogniser;
    @Autowired
    PredictionMaker predictionMaker;

    @Autowired
    private ApplicationContext context;

    @Bean
    public CommandLineRunner mainCode() {
        return (args) -> {
            settings = getSettingsFromFile(SETTINGS_FILE_NAME);
            if(!settings.isValid()){
                log.info("Failed to read application settings from \"" + SETTINGS_FILE_NAME + "\", failed to start application, shutting down the application.");
                System.exit(SpringApplication.exit(context));
            }
            switch (settings.startMode){
                case INIT:
                    init();
                    break;
                case REG:
                    regularStart();
                    break;
                case TEST:
                    testStart();
                    break;
            }
            log.info("Server is running.");
            scheduler.loaderFinished = true;
        };
    }

    private void init(){
        log.info("Initializing started.");
        var initSuccess = false;
        List<Stock> stockListFromFile = getStocksFromFile(settings.stockListFileName);
        if(stockListFromFile.isEmpty()){
            log.info("Failed to read stocks from file, initialization failed, shutting down the application.");
            System.exit(SpringApplication.exit(context));
        }
        repository.deleteDatabase();
        repository.saveStocks(stockListFromFile);
        var stockListFromDB = repository.getAllStocks();
        LocalDate requestFromDate = LocalDate.now(ZoneId.of("US/Eastern")).minus(1, ChronoUnit.YEARS);
        LocalDate requestUntilDate = LocalDate.now(ZoneId.of("US/Eastern"));
        for(Stock stock: stockListFromDB) {
            var candles = candleService.getCandlesForStockFromUntil(stock, requestFromDate, requestUntilDate);
            if (candles.size() < MIN_REQUIRED_CANDLE_NUMBER) {
                log.info("ERROR! There are not enough candle data for stock " + stock.getSymbol() + ", deleting it from database.");
                repository.deleteStockById(stock.getID());
            } else {
                repository.saveCandles(candles);
                repository.saveIndicators(indicatorCalculator.initializeIndicatorsForStocks(stock));
                repository.savePatterns(patternRecogniser.findAllPatternsForStocks(stock));
                repository.savePredictions(predictionMaker.initPredictionsForStock(stock));
                // If we get data for at least 1 stock the initialization is successful.
                initSuccess = true;
            }
        }
        if(initSuccess) {
            repository.saveLog(new Log(LocalDateTime.now(ZoneId.of("US/Eastern")), "INIT SUCCESS"));
        }else{
            log.info("Initialization failed, can not find data for any of the stocks.");
            System.exit(SpringApplication.exit(context));
        }
        log.info("Initializing finished.");
    }
    private void regularStart(){
        log.info("Starting server in regular mode");
        // We have to check if the server have been initialized, because we need data for daily candle requests and calculations.
        // The app does not initialize itself automatically because if the initialization is a consequence of an error
        // (for example someone deletes the init log) then during the initialization the database is deleted and valuable data is lost.
        if(isAppInitialized()){
            repository.saveLog(new Log(LocalDateTime.now(ZoneId.of("US/Eastern")), "REG START"));
            scheduler.loaderFinished = true;
            scheduler.dailyRequest();
        }else{
            log.info("Application is not initialized yet, run it in initialization mode once first, shutting down.");
            System.exit(SpringApplication.exit(context));
        }
    }

    // Test mode for testing purposes.
    private void testStart(){
        log.info("Server starting in test mode");
        predictionTest();
    }

    private boolean isAppInitialized(){
        var initLog = repository.getFirstLog();
        return initLog != null && initLog.getMessage().equals("INIT SUCCESS");
    }

    private Settings getSettingsFromFile(String fileName) {
        log.info("Getting start mode from file: " + fileName);
        Settings result = new Settings();
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                var splittedLine = line.split(":");
                if(splittedLine.length == 2){
                    if(splittedLine[0].equals("start_mode")){
                        if(Objects.equals(splittedLine[1], "TEST"))
                            result.startMode = TEST;
                        else if(Objects.equals(splittedLine[1], "REG"))
                            result.startMode = REG;
                        else if(Objects.equals(splittedLine[1], "INIT"))
                            result.startMode = INIT;
                    }else if(splittedLine[0].equals("stock_list_filename")){
                        result.stockListFileName = splittedLine[1];
                    }
                }
            }
            myReader.close();
            return result;
        } catch (FileNotFoundException e) {
            log.info("ERROR! Reading application settings from file failed! Maybe \"" + SETTINGS_FILE_NAME + "\" is missing.");
            return new Settings();
        }
    }

    private List<Stock> getStocksFromFile(String fileName) {
        log.info("Getting stocks from file: " + fileName);
        List<Stock> stocksFromFile = new ArrayList<>();
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] splittedData = data.split("\\t", 30);
                String name = splittedData[0];
                String symbol = splittedData[1];
                stocksFromFile.add(new Stock(name, symbol));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            log.info("ERROR! Reading stocks from file failed! There are no file named: " + fileName);
        }
        return stocksFromFile;
    }

    // Predicting algorithm testing function.
    private void predictionTest(){
        var stocks = repository.getAllStocks();
        var allBuySignal = 0;
        var allBuySignalSuccess = 0;
        var allSellSignal = 0;
        var allSellSignalSuccess = 0;
        for(Stock stock: stocks){
            var allCandles = repository.getAllCandlesForStock(stock.getID());
            var predictions = predictionMaker.initPredictionsForStock(stock);
            var relevantCandles = allCandles.subList(allCandles.size() - predictions.size(), allCandles.size());
            var buySignalCounter = 0;
            var buySuccessCounter = 0;
            for(int i = 0; i < relevantCandles.size() - 10; i++){
                if(predictions.get(i).getStrength() == 2) {
                    buySignalCounter++;
                    var isSuccess = false;
                    for(int j = i; j < (i + 10); j++){
                        if(relevantCandles.get(j).getClose() > relevantCandles.get(i).getClose() * 1.05){
                            isSuccess = true;
                        }
                    }
                    if(isSuccess)
                        buySuccessCounter++;
                }
            }
            var sellSignalCounter = 0;
            var sellSuccessCounter = 0;
            for(int i = 0; i < relevantCandles.size() - 10; i++){
                if(predictions.get(i).getStrength() == -2) {
                    sellSignalCounter++;
                    var isSuccess = false;
                    for(int j = i; j < (i + 10); j++){
                        if(relevantCandles.get(j).getClose() < relevantCandles.get(i).getClose() * 0.95){
                            isSuccess = true;
                        }
                    }
                    if(isSuccess)
                        sellSuccessCounter++;
                }
            }
            allBuySignal += buySignalCounter;
            allSellSignal += sellSignalCounter;
            allBuySignalSuccess += buySuccessCounter;
            allSellSignalSuccess += sellSuccessCounter;
        }
        log.info("Buy result: " + allBuySignalSuccess + "/" + allBuySignal);
        log.info("Sell result: " + allSellSignalSuccess + "/" + allSellSignal);
    }
}
