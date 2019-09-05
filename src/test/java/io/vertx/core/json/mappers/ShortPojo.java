package io.vertx.core.json.mappers;

import io.vertx.core.spi.json.JsonMapper;

import java.util.Objects;

public class ShortPojo {

  public static class MyShortPojoJsonMapper implements JsonMapper<ShortPojo, Number> {

    @Override
    public ShortPojo deserialize(Number value) throws IllegalArgumentException {
      return new ShortPojo().setValue(value.shortValue());
    }

    @Override
    public Short serialize(ShortPojo value) throws IllegalArgumentException {
      return value.getValue();
    }

    @Override
    public Class<ShortPojo> getTargetClass() {
      return ShortPojo.class;
    }
  }

  short value;

  public short getValue() {
    return value;
  }

  public ShortPojo setValue(short value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ShortPojo that = (ShortPojo) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
