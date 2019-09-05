package io.vertx.core.json.mappers;

import io.vertx.core.spi.json.JsonMapper;

import java.util.Objects;

public class DoublePojo {

  public static class DoublePojoMapper implements JsonMapper<DoublePojo, Number> {

    @Override
    public DoublePojo deserialize(Number value) throws IllegalArgumentException {
      return new DoublePojo().setValue(value.doubleValue());
    }

    @Override
    public Double serialize(DoublePojo value) throws IllegalArgumentException {
      return value.getValue();
    }

    @Override
    public Class<DoublePojo> getTargetClass() {
      return DoublePojo.class;
    }
  }

  double value;

  public double getValue() {
    return value;
  }

  public DoublePojo setValue(double value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DoublePojo that = (DoublePojo) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
