package io.vertx.core.json.mappers;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.JacksonCodec;
import io.vertx.core.spi.json.JsonMapper;

public class PojoMapper implements JsonMapper<Pojo, JsonObject> {

  @Override
  public Pojo deserialize(JsonObject json) throws IllegalArgumentException {
    Pojo value = new Pojo();
    value.value = json.getString("value");
    String instant = json.getString("instant");
    if (instant != null) {
      value.instant = JacksonCodec.instantFromString(instant);
    }
    String bytes = json.getString("bytes");
    if (bytes != null) {
      value.bytes = JacksonCodec.byteArrayFromString(bytes);
    }
    return value;
  }

  @Override
  public JsonObject serialize(Pojo value) throws IllegalArgumentException {
    JsonObject json = new JsonObject();
    if (value.value != null) {
      json.put("value", value.value);
    }
    if (value.instant != null) {
      json.put("instant", JacksonCodec.instantToString(value.instant));
    }
    if (value.bytes != null) {
      json.put("bytes", JacksonCodec.byteArrayToString(value.bytes));
    }
    return json;
  }

  @Override
  public Class<Pojo> getTargetClass() {
    return Pojo.class;
  }
}
