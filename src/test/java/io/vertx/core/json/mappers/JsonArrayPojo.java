package io.vertx.core.json.mappers;

import io.vertx.core.spi.json.JsonMapper;
import io.vertx.core.json.JsonArray;

import java.util.Objects;

public class JsonArrayPojo {

  public static class JsonArrayPojoMapper implements JsonMapper<JsonArrayPojo, JsonArray> {

    @Override
    public JsonArrayPojo deserialize(JsonArray value) throws IllegalArgumentException {
      return new JsonArrayPojo().setValue(value);
    }

    @Override
    public JsonArray serialize(JsonArrayPojo value) throws IllegalArgumentException {
      return value.getValue();
    }

    @Override
    public Class<JsonArrayPojo> getTargetClass() {
      return JsonArrayPojo.class;
    }
  }

  JsonArray value;

  public JsonArray getValue() {
    return value;
  }

  public JsonArrayPojo setValue(JsonArray value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JsonArrayPojo that = (JsonArrayPojo) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
