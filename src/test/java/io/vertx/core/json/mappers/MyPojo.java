package io.vertx.core.json.mappers;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.json.JsonMapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

@DataObject
public class MyPojo {

  public static class MyPojoJsonMapper implements JsonMapper<MyPojo, JsonObject> {

    public static final MyPojoJsonMapper INSTANCE = new MyPojoJsonMapper();

    @Override
    public MyPojo deserialize(JsonObject json) throws IllegalArgumentException {
      return new MyPojo(json);
    }

    @Override
    public JsonObject serialize(MyPojo value) throws IllegalArgumentException {
      return value.toJson();
    }

    @Override
    public Class<MyPojo> getTargetClass() {
      return MyPojo.class;
    }
  }

  String value;
  Instant instant;
  byte[] bytes;

  public MyPojo() {
  }

  public MyPojo(JsonObject object) {
    if (object.containsKey("value")) {
      this.value = object.getString("value");
    }
    if (object.containsKey("instant")) {
      this.instant = object.getInstant("instant");
    }
    if (object.containsKey("bytes")) {
      this.bytes = object.getBinary("bytes");
    }
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    if (value != null) {
      json.put("value", value);
    }
    if (instant != null) {
      json.put("instant", instant);
    }
    if (bytes != null) {
      json.put("bytes", bytes);
    }
    return json;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Instant getInstant() {
    return instant;
  }

  public void setInstant(Instant instant) {
    this.instant = instant;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MyPojo pojo = (MyPojo) o;
    return Objects.equals(value, pojo.value) &&
      Objects.equals(instant, pojo.instant) &&
      Arrays.equals(bytes, pojo.bytes);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(value, instant);
    result = 31 * result + Arrays.hashCode(bytes);
    return result;
  }
}
