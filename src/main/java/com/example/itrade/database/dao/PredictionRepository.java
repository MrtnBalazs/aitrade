package com.example.itrade.database.dao;

import com.example.itrade.database.model.Prediction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionRepository extends CrudRepository<Prediction, Long> {
    @Query(value = "SELECT * FROM Prediction WHERE StockID = :stockID order by Date", nativeQuery = true)
    public List<Prediction> findAllByStockID(Long stockID);
    @Query(value = "SELECT TOP 20 * FROM Prediction WHERE StockID = :stockID order by Date desc", nativeQuery = true)
    public List<Prediction> findLast20ByStockID(Long stockID);
}
