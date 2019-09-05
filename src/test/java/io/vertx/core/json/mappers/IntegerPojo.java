package io.vertx.core.json.mappers;

import io.vertx.core.spi.json.JsonMapper;

import java.util.Objects;

public class IntegerPojo {

  public static class IntegerPojoMapper implements JsonMapper<IntegerPojo, Number> {

    @Override
    public IntegerPojo deserialize(Number value) throws IllegalArgumentException {
      return new IntegerPojo().setValue(value.intValue());
    }

    @Override
    public Integer serialize(IntegerPojo value) throws IllegalArgumentException {
      return value.getValue();
    }

    @Override
    public Class<IntegerPojo> getTargetClass() {
      return IntegerPojo.class;
    }
  }

  int value;

  public int getValue() {
    return value;
  }

  public IntegerPojo setValue(int value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IntegerPojo that = (IntegerPojo) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
