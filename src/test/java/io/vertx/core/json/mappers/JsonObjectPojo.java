package io.vertx.core.json.mappers;

import io.vertx.core.spi.json.JsonMapper;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class JsonObjectPojo {

  public static class JsonObjectPojoMapper implements JsonMapper<JsonObjectPojo, JsonObject> {

    @Override
    public JsonObjectPojo deserialize(JsonObject value) throws IllegalArgumentException {
      return new JsonObjectPojo().setValue(value);
    }

    @Override
    public JsonObject serialize(JsonObjectPojo value) throws IllegalArgumentException {
      return value.getValue();
    }

    @Override
    public Class<JsonObjectPojo> getTargetClass() {
      return JsonObjectPojo.class;
    }
  }

  JsonObject value;

  public JsonObject getValue() {
    return value;
  }

  public JsonObjectPojo setValue(JsonObject value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JsonObjectPojo that = (JsonObjectPojo) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
