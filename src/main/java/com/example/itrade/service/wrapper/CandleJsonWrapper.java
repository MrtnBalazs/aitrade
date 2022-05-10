package com.example.itrade.service.wrapper;

import com.example.itrade.database.model.Candle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CandleJsonWrapper {
    private static final Logger log = LoggerFactory.getLogger(CandleJsonWrapper.class);
    @JsonProperty("o")
    List<Double> open;
    @JsonProperty("c")
    List<Double> close;
    @JsonProperty("h")
    List<Double> high;
    @JsonProperty("l")
    List<Double> low;
    @JsonProperty("t")
    List<Long> date;

    public List<Candle> makeCandleList(Long stockID){
        if(open == null || close == null || high == null || low == null || date == null){
            log.info("ERROR! CandleWrapper object is missing data -> cannot convert to Candle object! Giving back empty list. (StockID: " + stockID + ")");
            return new ArrayList<>();
        }
        List<Candle> result = new ArrayList<>();
        for(int i = 0; i < close.size(); i++){
            LocalDate date = Instant.ofEpochSecond(this.date.get(i)).atZone(ZoneId.systemDefault()).toLocalDate();
            result.add(new Candle(open.get(i), close.get(i), high.get(i), low.get(i), date, stockID));
        }
        return result;
    }
}
