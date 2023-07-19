package com.meshtier.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class StreamsCountApp {

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG,"streams-count-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> textLines = builder.stream("word-count-input");

        KTable<String, Long> wordCounts
                = textLines
                .mapValues(
                        textLine -> textLine.toLowerCase())
                .flatMapValues(
                        textLine
                                -> Arrays.asList(
                                textLine.split("\\W+")))
                .selectKey((key, word) -> word)
                .groupByKey()
                .count(Named.as("Counts"));

        wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(),Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        while (true) {
            System.out.println(streams.toString());
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                break;
            }
        }

    }
}
