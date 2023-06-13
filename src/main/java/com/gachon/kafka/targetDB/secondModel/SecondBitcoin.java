package com.gachon.kafka.targetDB.secondModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bitcoin")
@Entity

public class SecondBitcoin {

    @Id
    @Column(name="id")
    private int id;

    @Column(name="timestamp")
    private String timestamp;

    @Column(name="open")
    private double open;

    @Column(name="high")
    private double high;

    @Column(name="low")
    private double low;

    @Column(name="close")
    private double close;

    @Column(name="volume")
    private double volume;



    @Builder
    public  SecondBitcoin(String timestamp, double open, double high, double low, double close, double volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
}
