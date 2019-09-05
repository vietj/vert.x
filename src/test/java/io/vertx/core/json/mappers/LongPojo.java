package io.vertx.core.json.mappers;

import io.vertx.core.spi.json.JsonMapper;

import java.util.Objects;

public class LongPojo {

  public static class LongPojoMapper implements JsonMapper<LongPojo, Number> {

    @Override
    public LongPojo deserialize(Number value) throws IllegalArgumentException {
      return new LongPojo().setValue(value.longValue());
    }

    @Override
    public Long serialize(LongPojo value) throws IllegalArgumentException {
      return value.getValue();
    }

    @Override
    public Class<LongPojo> getTargetClass() {
      return LongPojo.class;
    }
  }

  long value;

  public long getValue() {
    return value;
  }

  public LongPojo setValue(long value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LongPojo that = (LongPojo) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
