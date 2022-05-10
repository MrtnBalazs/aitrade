package com.example.itrade.business.calculate;

import com.example.itrade.database.Repository;
import com.example.itrade.database.model.*;
import com.example.itrade.service.CandleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class PredictionMaker {
    private static final Logger log = LoggerFactory.getLogger(CandleService.class);
    @Autowired
    Repository repository;

    public List<Prediction> makeNewPredictionsForStock(Stock stock){
        List<Prediction> predictions = new ArrayList<>();
        // The prediction making process uses couple days of data, so we need old predictions so that we can make consistent new predictions.
        // We need to get the last couple of predictions by number not date, because some stocks may miss candle data here and there and
        // because we calculate indicators based on candles, and we make prediction based on indicators, we might miss some predictions.
        List<Prediction> lastPredictions = repository.getLast20PredictionByStockID(stock.getID());
        if(lastPredictions == null || lastPredictions.size() < 20)
            return predictions;
        List<Indicator> indicatorList = repository.getIndicatorsForStockFrom(stock.getID(), lastPredictions.get(0).getDate());
        List<Pattern> patternList = repository.getPatternsForStockFrom(stock.getID(), lastPredictions.get(lastPredictions.size() - 1).getDate().plus(1,ChronoUnit.DAYS));
        if(indicatorList == null || patternList == null || indicatorList.isEmpty())
            return predictions;
        var candleList = repository.getCandlesForStockFrom(stock.getID(), indicatorList.get(0).getDate());
        if(candleList == null || candleList.isEmpty() || candleList.size() != indicatorList.size())
            return predictions;
        log.info("Calculating new predictions for stock: " + stock.getSymbol() + ", from: " + lastPredictions.get(lastPredictions.size() - 1).getDate().plus(1,ChronoUnit.DAYS));

        predictions =  makePredictions(indicatorList, candleList, patternList, 65, 75, 35, 25, 2, 2);
        return predictions.subList(20, predictions.size());
    }

    public List<Prediction> initPredictionsForStock(Stock stock){
        log.info("Initializing predictions for stocks...");
        List<Prediction> predictions = new ArrayList<>();
        List<Indicator> indicatorList = repository.getAllIndicatorsForStock(stock.getID());
        List<Pattern> patternList = repository.getAllPatternsForStock(stock.getID());
        if(indicatorList == null || patternList == null || indicatorList.isEmpty())
            return predictions;
        var candleList = repository.getCandlesForStockFrom(stock.getID(), indicatorList.get(0).getDate());
        if(candleList == null || candleList.isEmpty() || candleList.size() != indicatorList.size())
            return predictions;

        return makePredictions(indicatorList, candleList, patternList, 65, 75, 35, 25, 2, 2);
    }

    // The prediction is negative people should sell, if the prediction is positive people should buy the stock,
    // the absolute value of the prediction dictates, how good is that prediction (the bigger the better)
    private List<Prediction> makePredictions(List<Indicator> indicatorList,
                                          List<Candle> candleList,
                                          List<Pattern> patternList,
                                          int rsiFirstUpperLimit,
                                          int rsiSecondUpperLimit,
                                          int rsiFirstLowerLimit,
                                          int rsiSecondLowerLimit,
                                          int macdIncreaseLimit,
                                          int macdDecreaseLimit
    ){
        List<Prediction> predictionList = new ArrayList<>();
        for(Indicator indicator: indicatorList) {
            predictionList.add(new Prediction(0, indicator.getDate(), indicator.getStockID()));
        }
        predictionList = predictFromMacd(predictionList, indicatorList, candleList, macdIncreaseLimit, macdDecreaseLimit);
        predictionList = predictFromRsi(predictionList, indicatorList, rsiFirstUpperLimit, rsiSecondUpperLimit, rsiFirstLowerLimit, rsiSecondLowerLimit);
        predictionList = predictFromPattern(predictionList, patternList);
        return predictionList;
    }

    // Predicting using MACD:
    // The idea is if the MACD is above the signal line and rises for a couple of days and then falls,
    // and the price of the stock is above the 100 day EMA (which indicates the long term trend of the stock),
    // the price is likely to go down.
    // If the MACD is under the signal line for a couple of days and then rises, and the price of the stock is under the
    // 100 day EMA, the price of the stock is likely to go up.
    private List<Prediction> predictFromMacd(List<Prediction> predictionList, List<Indicator> indicatorList, List<Candle> candleList, int macdIncreaseLimit, int macdDecreaseLimit) {
        int macdDecreaseCounter = 0;
        int macdIncreaseCounter = 0;
        for (int i = 1; i < indicatorList.size(); i++) {
            double oldHistogram = indicatorList.get(i - 1).getMACD() - indicatorList.get(i - 1).getSignal();
            if (macdIncreaseCounter > macdIncreaseLimit && indicatorList.get(i - 1).getMACD() > indicatorList.get(i).getMACD()) {
                predictionList.get(i).setStrength(predictionList.get(i).getStrength() - 2);
            }
            if (macdDecreaseCounter > macdDecreaseLimit && indicatorList.get(i - 1).getMACD() < indicatorList.get(i).getMACD() && candleList.get(i).getClose() < indicatorList.get(i).getEMA100()) {
                predictionList.get(i).setStrength(predictionList.get(i).getStrength() + 2);
            }

            if (oldHistogram > 0 && indicatorList.get(i - 1).getMACD() < indicatorList.get(i).getMACD()) {
                macdIncreaseCounter++;
                macdDecreaseCounter = 0;
            } else {
                macdIncreaseCounter = 0;
            }
            if (oldHistogram < 0 && indicatorList.get(i - 1).getMACD() > indicatorList.get(i).getMACD()) {
                macdDecreaseCounter++;
                macdIncreaseCounter = 0;
            } else {
                macdDecreaseCounter = 0;
            }
        }
        return predictionList;
    }

    // Predicting using RSI:
    // If the RSI is above the second upper limit that means the stock is very overvalued so people should sell that stock.
    // If the RSI is above the first upper limit that means the stock is slightly overvalued.
    private List<Prediction> predictFromRsi(List<Prediction> predictionList, List<Indicator> indicatorList, int rsiFirstUpperLimit, int rsiSecondUpperLimit, int rsiFirstLowerLimit, int rsiSecondLowerLimit) {
        for(int i = 0; i < indicatorList.size(); i++){
            if (indicatorList.get(i).getRSI() <= rsiSecondLowerLimit)
                predictionList.get(i).setStrength(predictionList.get(i).getStrength() + 2);
            else if (indicatorList.get(i).getRSI() <= rsiFirstLowerLimit)
                predictionList.get(i).setStrength(predictionList.get(i).getStrength() + 1);

            if (indicatorList.get(i).getRSI() >= rsiSecondUpperLimit)
                predictionList.get(i).setStrength(predictionList.get(i).getStrength() - 2);
            else if (indicatorList.get(i).getRSI() >= rsiFirstUpperLimit)
                predictionList.get(i).setStrength(predictionList.get(i).getStrength() - 1);
        }
        return predictionList;
    }

    private List<Prediction> predictFromPattern(List<Prediction> predictionList, List<Pattern> patternList){
        for(Pattern pattern: patternList){
            innerloop:
            {
                for (Prediction prediction : predictionList) {

                    if (pattern.getDate().equals(prediction.getDate())) {
                        prediction.setStrength(prediction.getStrength() + pattern.getStrength());
                        break innerloop;
                    }
                }
            }
        }
        return predictionList;
    }
}
