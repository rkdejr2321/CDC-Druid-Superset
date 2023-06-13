package com.gachon.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gachon.kafka.srcDB.model.Bitcoin;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.apache.kafka.common.serialization.Serdes.Long;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collections;
import io.debezium.serde.DebeziumSerdes;

@Component
public class BitcoinNumProcessor {
    final Serde<String> STRING_SERDE = Serdes.String();
    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        Serde<Bitcoin> BitcoinSerde = DebeziumSerdes.payloadJson(Bitcoin.class);
        BitcoinSerde.configure(Collections.singletonMap("from.field", "after"), false);

        KStream<String, Bitcoin> stream = streamsBuilder.stream(
                "debezium-connector.src_db.bitcoin",
                Consumed.with(Serdes.String(), BitcoinSerde));

        KStream<String, Bitcoin> filter = stream.filter(new Predicate<String, Bitcoin>() {
            @Override
            public boolean test(String key, Bitcoin value) { //16부터 19학번
                return value.getOpen() > 1000 && value.getOpen() < 10000000;
            }
        });

        filter.peek((key, value) -> System.out.println("Inside filter " + value.getId()));
        filter.to("user-sn-filter");
    }
}