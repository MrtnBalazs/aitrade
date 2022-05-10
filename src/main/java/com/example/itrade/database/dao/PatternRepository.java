package com.example.itrade.database.dao;

import com.example.itrade.database.model.Pattern;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatternRepository extends CrudRepository<Pattern, Long> {
    @Query(value = "SELECT * FROM Pattern WHERE StockID = :stockID order by Date", nativeQuery = true)
    public List<Pattern> findAllByStockID(Long stockID);
    @Query(value = "SELECT * FROM Pattern WHERE StockID = :stockID AND Date >= :date order by Date", nativeQuery = true)
    public List<Pattern> findFromDateAndStockID(LocalDate date, Long stockID);
}
