package com.gachon.kafka.consumer;

//import com.gachon.kafka.oracleDB.model.OracleUser;
//import com.gachon.kafka.oracleDB.repository.OracleUserRepository;

import com.gachon.kafka.srcDB.model.Bitcoin;
import com.gachon.kafka.targetDB.secondRepository.SecondBitcoinRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
//@AllArgsConstructor
//@NoArgsConstructor
public class BitcoinConsumer {

    private final ObjectMapper mapper;
    private final SecondBitcoinRepository secondBitcoinRepository;


    @KafkaListener(topics = "debezium-connector.src_db.bitcoin")
    public void consumerBitcoin(ConsumerRecord<String, String> record) throws JsonProcessingException {
        String consumedValue = record.value();

        var jsonNode = mapper.readTree(consumedValue);
        JsonNode payload = jsonNode.path("payload");
        String op = payload.get("op").toString().substring(1,2);
        System.out.println(payload);


        switch (op) {
            case "c": {
                JsonNode after = payload.path("after");
                Bitcoin mysqlBitcoin = Bitcoin.builder()
                        .timestamp(after.get("timestamp").toString())
                        .open(after.get("open").doubleValue())
                        .high(after.get("high").doubleValue())
                        .low(after.get("low").doubleValue())
                        .close(after.get("close").doubleValue())
                        .volume(after.get("volume").doubleValue())
                        .build();

                secondBitcoinRepository.save(mysqlBitcoin);

                break;
            }
            case "u": {
                JsonNode after = payload.path("after");
                Bitcoin mysqlBitcoin = secondBitcoinRepository.findById(Integer.parseInt(after.get("id").toString()))
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id입니다. " + after.get("id").toString()));


                mysqlBitcoin.update(Integer.parseInt(after.get("id").toString()));


                secondBitcoinRepository.save(mysqlBitcoin);

                break;
            }
            case "d": {
                JsonNode before = payload.path("before");
                System.out.println(before.get("id").toString());

                Bitcoin mysqlBitcoin = secondBitcoinRepository.findById(Integer.parseInt(before.get("id").toString()))
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id입니다. " + before.get("id").toString()));
                System.out.println(mysqlBitcoin.toString());

                System.out.println(mysqlBitcoin.getId());


                secondBitcoinRepository.deleteById(mysqlBitcoin.getId());

                break;
            }
        }
    }
}
