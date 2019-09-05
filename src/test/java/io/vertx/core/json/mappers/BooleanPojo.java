package io.vertx.core.json.mappers;

import io.vertx.core.spi.json.JsonMapper;

import java.util.Objects;

public class BooleanPojo {

  public static class BooleanPojoMapper implements JsonMapper<BooleanPojo, Boolean> {

    @Override
    public BooleanPojo deserialize(Boolean value) throws IllegalArgumentException {
      return new BooleanPojo().setValue(value);
    }

    @Override
    public Boolean serialize(BooleanPojo value) throws IllegalArgumentException {
      return value.isValue();
    }

    @Override
    public Class<BooleanPojo> getTargetClass() {
      return BooleanPojo.class;
    }
  }

  boolean value;

  public boolean isValue() {
    return value;
  }

  public BooleanPojo setValue(boolean value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BooleanPojo that = (BooleanPojo) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
