package com.example.itrade.database.dao;

import com.example.itrade.database.model.Stock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends CrudRepository<Stock, Long>{
    public List<Stock> findAll();
    public Optional<Stock> findByID(Long ID);
    public Stock findBySymbol(String symbol);
}

