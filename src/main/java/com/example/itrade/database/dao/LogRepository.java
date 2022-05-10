package com.example.itrade.database.dao;

import com.example.itrade.database.model.Log;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogRepository extends CrudRepository<Log, Long> {
    @Query(value = "SELECT * FROM Log order by Date", nativeQuery = true)
    public List<Log> findAll();
    @Query(value = "SELECT TOP 1 * FROM Log order by ID", nativeQuery = true)
    public Log findFirstLog();
}
