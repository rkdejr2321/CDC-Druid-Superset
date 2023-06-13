package com.gachon.kafka.srcDB.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "bitcoin")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Bitcoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    public void update(int id){
        this.id = id;
    }
}

