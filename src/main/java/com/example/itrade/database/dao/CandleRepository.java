package com.example.itrade.database.dao;

import com.example.itrade.database.model.Candle;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CandleRepository extends CrudRepository<Candle, Long> {
    @Query(value = "SELECT * FROM Candle order by Date", nativeQuery = true)
    public List<Candle> findAll();
    @Query(value = "SELECT * FROM Candle WHERE StockID = :stockID order by Date", nativeQuery = true)
    public List<Candle> findAllByStockID(Long stockID);
    @Query(value = "SELECT * FROM Candle WHERE StockID = :stockID AND Date >= :from order by Date", nativeQuery = true)
    public List<Candle> findFromDateAndStockID(@Param("from") LocalDate from, @Param("stockID") Long stockID);
    @Query(value = "SELECT TOP 1 * FROM Candle WHERE StockID = :stockID order by Date desc", nativeQuery = true)
    public Candle findLastByStockID(Long stockID);
    @Query(value = "SELECT TOP 20 * FROM Candle WHERE StockID = :stockID order by Date desc", nativeQuery = true)
    public List<Candle> findLast20ByStockID(Long stockID);
}

