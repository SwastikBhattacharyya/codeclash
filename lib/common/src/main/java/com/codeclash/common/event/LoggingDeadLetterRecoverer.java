package com.codeclash.common.event;

import com.codeclash.common.exception.CodedException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConsumerAwareRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

@Slf4j
public class LoggingDeadLetterRecoverer implements ConsumerAwareRecordRecoverer {

  private final DeadLetterPublishingRecoverer delegate;

  public LoggingDeadLetterRecoverer(DeadLetterPublishingRecoverer delegate) {
    this.delegate = delegate;
  }

  private static Optional<CodedException> codedCause(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause())
      if (cause instanceof CodedException coded) return Optional.of(coded);
    return Optional.empty();
  }

  private static void logRejected(ConsumerRecord<?, ?> record, CodedException exception) {
    LoggingDeadLetterRecoverer.log
        .atWarn()
        .setMessage("record rejected")
        .addKeyValue("messaging.topic", record.topic())
        .addKeyValue("messaging.key", record.key())
        .addKeyValue("error.code", exception.getErrorCode().name())
        .log();
  }

  private static void logFailed(ConsumerRecord<?, ?> record, Exception exception) {
    LoggingDeadLetterRecoverer.log
        .atError()
        .setMessage("record failed")
        .addKeyValue("messaging.topic", record.topic())
        .addKeyValue("messaging.key", record.key())
        .setCause(exception)
        .log();
  }

  @Override
  public void accept(ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, Exception exception) {
    this.delegate.accept(record, consumer, exception);
    LoggingDeadLetterRecoverer.codedCause(exception)
        .ifPresentOrElse(
            coded -> LoggingDeadLetterRecoverer.logRejected(record, coded),
            () -> LoggingDeadLetterRecoverer.logFailed(record, exception));
  }
}
