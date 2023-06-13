package com.gachon.kafka.Dto;

import com.gachon.kafka.srcDB.model.Bitcoin;
import lombok.Data;

import java.io.Serializable;

/**

 A DTO for the {@link Bitcoin} entity
 */
@Data
public class BitcoinDTO {
    private final String timestamp;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;
}