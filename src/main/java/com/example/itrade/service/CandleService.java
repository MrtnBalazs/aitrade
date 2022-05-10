package com.example.itrade.service;
import com.example.itrade.database.model.Candle;
import com.example.itrade.database.model.Stock;
import com.example.itrade.service.wrapper.CandleJsonWrapper;
import com.example.itrade.service.wrapper.CandleServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CandleService {
    private static final Logger log = LoggerFactory.getLogger(CandleService.class);

    public List<Candle> getCandlesForStockFromUntil(Stock stock, LocalDate fromDate, LocalDate untilDate){

        // We do not work with intraday data, so we only need data for the days when the market has already been closed.
        // If the time and date now is before the close time and date on until date, then the market is still open on that date so
        // we change untildate for the day before.
        LocalDateTime timeNow = LocalDateTime.now(ZoneId.of("US/Eastern"));
        LocalDateTime closeTime = LocalDateTime.of(untilDate, LocalTime.of(16,0));
        if(timeNow.isBefore(closeTime)){
            log.info("The market hasn't closed yet today, or the date is not yet " + untilDate);
            untilDate = LocalDate.now().minus(1,ChronoUnit.DAYS);
            log.info("Requesting until the day before: (" + untilDate + ")");
        }

        List<Candle> candleList = new ArrayList<>();
        var serviceResult = getCandles(stock, fromDate, untilDate);
        // If we get too many requests status code we wait for 25 seconds and try again (max 3 times)
        var requestTryCounter = 1;
        while(serviceResult.result.equals(Result.TOO_MANY_REQUESTS) && requestTryCounter <= 3){
            log.info("Too many requests, waiting 25 seconds and then retrying..");
            try {
                TimeUnit.SECONDS.sleep(25);
            }catch (Exception e){
                e.printStackTrace();
            }
            serviceResult = getCandles(stock, fromDate, untilDate);
            requestTryCounter++;
        }
        if(serviceResult.result.equals(Result.SUCCESS))
            candleList.addAll(serviceResult.candleList);
        else
            log.info("Failed to get candle info for stock: (" + stock.getSymbol() + ")");
        return candleList;
    }

    private CandleServiceResult getCandles(Stock stock, LocalDate fromDate, LocalDate untilDate){
        log.info("Getting candles from finnhub api for stock: " + stock.getSymbol() + " from: " + fromDate + " until: " + untilDate);
        // The api only gives back 1 year of data in the past so if the from date is older, then a year we change it to a year.
        if(fromDate.isBefore(LocalDate.now().minus(1, ChronoUnit.YEARS)))
            fromDate = LocalDate.now().minus(1, ChronoUnit.YEARS);
        // If the from date is before the until date, the request is invalid.
        if(untilDate.isBefore(fromDate))
            return new CandleServiceResult(new ArrayList<>(), Result.FAIL);

        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();
        ZoneOffset zone = ZoneId.of("UTC").getRules().getOffset(LocalDateTime.now(ZoneId.of("UTC")));
        // Time is set to 21:00 on the from date and 23:00 on the until date, because on the weekends if the time is set
        // before 20:02 on the from date, the api gives back friday s data for some reason.
        var fromEpochDate = fromDate.atTime(21,0).toEpochSecond(zone);
        var untilEpochDate = untilDate.atTime(23,0).toEpochSecond(zone);
        String url = "https://finnhub.io/api/v1/stock/candle?symbol=" + stock.getSymbol() + "&resolution=D&from=" + fromEpochDate + "&to=" + untilEpochDate + "&token=sandbox_c9t9nuiad3i1pjtuh8gg";
        log.info("Request to: " + url);
        try {
            var response = restTemplate.getForEntity(url, CandleJsonWrapper.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                var candleWrapper = response.getBody();
                if(candleWrapper == null)
                    return new CandleServiceResult(new ArrayList<>(), Result.FAIL);
                var candleList = candleWrapper.makeCandleList(stock.getID());
                if(candleList.isEmpty())
                    return new CandleServiceResult(new ArrayList<>(), Result.FAIL);
                else
                    return new CandleServiceResult(candleList, Result.SUCCESS);
            } else {
                return new CandleServiceResult(new ArrayList<>(), Result.FAIL);
            }
        }catch (HttpClientErrorException e){
            if(e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS))
                return new CandleServiceResult(new ArrayList<>(), Result.TOO_MANY_REQUESTS);
            else
                return new CandleServiceResult(new ArrayList<>(), Result.FAIL);
        }catch (Exception e){
            log.info("ERROR Something went wrong! Returning empty list. (Stock: " + stock.getName() + ")");
            return new CandleServiceResult(new ArrayList<>(), Result.FAIL);
        }
    }
}
