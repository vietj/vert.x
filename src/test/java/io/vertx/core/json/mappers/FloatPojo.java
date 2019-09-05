package io.vertx.core.json.mappers;

import io.vertx.core.spi.json.JsonMapper;

import java.util.Objects;

public class FloatPojo {

  public static class FloatPojoMapper implements JsonMapper<FloatPojo, Number> {

    @Override
    public FloatPojo deserialize(Number value) throws IllegalArgumentException {
      return new FloatPojo().setValue(value.floatValue());
    }

    @Override
    public Float serialize(FloatPojo value) throws IllegalArgumentException {
      return value.getValue();
    }

    @Override
    public Class<FloatPojo> getTargetClass() {
      return FloatPojo.class;
    }
  }

  float value;

  public float getValue() {
    return value;
  }

  public FloatPojo setValue(float value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FloatPojo that = (FloatPojo) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
