package com.example.itrade.database.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ID;
    @Column(name="Date")
    LocalDateTime date;
    @Column(name="Message")
    String message;

    public Log(LocalDateTime date, String message){
        this.date = date;
        this.message = message;
    }
    public Log(){}

    public Long getID() {
        return ID;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }
}
