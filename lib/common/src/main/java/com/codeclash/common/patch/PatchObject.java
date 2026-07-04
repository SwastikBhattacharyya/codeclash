package com.codeclash.common.patch;

import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

@RequiredArgsConstructor
public class PatchObject<T> {

  private final T value;
  @Getter private final boolean present;

  public static <T> PatchObject<T> from(JsonNullable<T> jsonNullable) {
    if (jsonNullable == null || !jsonNullable.isPresent()) return new PatchObject<>(null, false);
    else return new PatchObject<>(jsonNullable.get(), true);
  }

  public static <T> PatchObject<T> of(T value) {
    return new PatchObject<>(value, true);
  }

  public void ifPresent(Consumer<T> consumer) {
    if (!this.present) return;
    consumer.accept(this.value);
  }

  public <R> PatchObject<R> map(Function<T, R> mapper) {
    if (!this.present) return new PatchObject<>(null, false);
    return new PatchObject<>(mapper.apply(this.value), true);
  }

  public T orElse(T other) {
    return this.present ? this.value : other;
  }
}
