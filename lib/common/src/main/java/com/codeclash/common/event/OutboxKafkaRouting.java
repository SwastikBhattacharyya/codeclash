package com.codeclash.common.event;

import io.namastack.outbox.handler.OutboxRecordMetadata;
import io.namastack.outbox.kafka.KafkaOutboxRouting;
import io.namastack.outbox.routing.selector.OutboxPayloadSelector;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public final class OutboxKafkaRouting {

  public static final String TOPIC_CONTEXT_KEY = "kafka.topic";

  private OutboxKafkaRouting() {}

  public static KafkaOutboxRouting create() {
    BiPredicate<Object, OutboxRecordMetadata> hasTopic =
        (payload, metadata) ->
            metadata.getContext().containsKey(OutboxKafkaRouting.TOPIC_CONTEXT_KEY);
    BiFunction<Object, OutboxRecordMetadata, String> topic =
        (payload, metadata) -> metadata.getContext().get(OutboxKafkaRouting.TOPIC_CONTEXT_KEY);
    BiFunction<Object, OutboxRecordMetadata, String> key = (payload, metadata) -> metadata.getKey();
    return KafkaOutboxRouting.builder()
        .route(
            OutboxPayloadSelector.predicate(hasTopic),
            route -> {
              route.target(topic);
              route.key(key);
            })
        .build();
  }
}
