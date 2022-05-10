package com.example.itrade.database.dao;

import com.example.itrade.database.model.Indicator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IndicatorRepository extends CrudRepository<Indicator, Long> {
    @Query(value = "SELECT * FROM Indicator WHERE StockID = :stockID order by Date", nativeQuery = true)
    public List<Indicator> findAllByStockID(Long stockID);
    @Query(value = "SELECT TOP 1 * FROM Indicator WHERE StockID = :stockID AND Date = :date order by Date", nativeQuery = true)
    public Indicator findByDateAndStockID(LocalDate date, Long stockID);
    @Query(value = "SELECT * FROM Indicator WHERE StockID = :stockID AND Date >= :date order by Date", nativeQuery = true)
    public List<Indicator> findFromDateAndStockID(LocalDate date, Long stockID);
    @Query(value = "SELECT TOP 1 * FROM Indicator WHERE StockID = :stockID order by Date desc", nativeQuery = true)
    public Indicator findLastByStockID(Long stockID);
}
