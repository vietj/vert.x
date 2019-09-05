/*
 * Copyright (c) 2011-2017 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.json.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DatabindCodec extends JacksonCodec {

  public static ObjectMapper mapper = new ObjectMapper();
  public static ObjectMapper prettyMapper = new ObjectMapper();

  static {
    initialize();
  }

  private static void initialize() {
    // Non-standard JSON but we allow C style comments in our JSON
    mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

    prettyMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    prettyMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

    SimpleModule module = new SimpleModule();
    // custom types
    module.addSerializer(JsonObject.class, new JsonObjectSerializer());
    module.addSerializer(JsonArray.class, new JsonArraySerializer());
    // he have 2 extensions: RFC-7493
    module.addSerializer(Instant.class, new InstantSerializer());
    module.addDeserializer(Instant.class, new InstantDeserializer());
    module.addSerializer(byte[].class, new ByteArraySerializer());
    module.addDeserializer(byte[].class, new ByteArrayDeserializer());

    JacksonCodec.mappers.values().forEach(m -> {
      Class clazz = m.getTargetClass();
      module.addSerializer(clazz, new StdSerializer(clazz) {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
          Object val = m.serialize(value);
          encodeJson(val, gen);
        }
      });
      module.addDeserializer(clazz, new StdDeserializer(clazz) {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
          Object o = parseAny(p, Object.class);
          return m.deserialize(o);
        }
      });
    });

    mapper.registerModule(module);
    prettyMapper.registerModule(module);
  }

  @Override
  public <T> T fromValue(Object json, Class<T> clazz) {
    if (clazz == JsonObject.class) {
      Map<String, Object> value = (Map<String, Object>) DatabindCodec.mapper.convertValue(json, Map.class);
      return clazz.cast(new JsonObject(value));
    }
    if (clazz == JsonArray.class) {
      List<Object> value = (List<Object>) DatabindCodec.mapper.convertValue(json, List.class);
      return clazz.cast(new JsonArray(value));
    }
    T value;
    try {
      value = DatabindCodec.mapper.convertValue(json, clazz);
    } catch (Exception e) {
      if (e instanceof DecodeException) {
        throw e;
      }
      throw new DecodeException(e.getMessage(), e.getCause());
    }
    if (clazz == Object.class) {
      value = (T) adapt(value);
    }
    return value;
  }

  public <T> T fromValue(Object json, TypeReference<T> type) {
    T value = DatabindCodec.mapper.convertValue(json, type);
    if (type.getType() == Object.class) {
      value = (T) adapt(value);
    }
    return value;
  }

  @Override
  public <T> T toValue(Object object, Class<T> toJsonType) {
    if (toJsonType == JsonObject.class) {
      return toJsonType.cast(new JsonObject(DatabindCodec.mapper.convertValue(object, Map.class)));
    } else if (toJsonType == JsonArray.class) {
      return toJsonType.cast(new JsonArray(DatabindCodec.mapper.convertValue(object, List.class)));
    }
    return DatabindCodec.mapper.convertValue(object, toJsonType);
  }

  @Override
  public <T> T fromString(String str, Class<T> clazz) throws DecodeException {
    return fromParser(createParser(str), clazz);
  }

  public <T> T fromString(String str, TypeReference<T> typeRef) throws DecodeException {
    return fromParser(createParser(str), typeRef);
  }

  @Override
  public <T> T fromBuffer(Buffer buf, Class<T> clazz) throws DecodeException {
    return fromParser(createParser(buf), clazz);
  }

  public <T> T fromBuffer(Buffer buf, TypeReference<T> typeRef) throws DecodeException {
    return fromParser(createParser(buf), typeRef);
  }

  private static JsonParser createParser(Buffer buf) {
    try {
      return DatabindCodec.mapper.getFactory().createParser((InputStream) new ByteBufInputStream(buf.getByteBuf()));
    } catch (IOException e) {
      throw new DecodeException("Failed to decode:" + e.getMessage(), e);
    }
  }

  private static JsonParser createParser(String str) {
    try {
      return DatabindCodec.mapper.getFactory().createParser(str);
    } catch (IOException e) {
      throw new DecodeException("Failed to decode:" + e.getMessage(), e);
    }
  }

  public static <T> T fromParser(JsonParser parser, Class<T> type) throws DecodeException {
    T value;
    try {
      value = DatabindCodec.mapper.readValue(parser, type);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode:" + e.getMessage(), e);
    } finally {
      close(parser);
    }
    if (type == Object.class) {
      value = (T) adapt(value);
    }
    return value;
  }

  private static <T> T fromParser(JsonParser parser, TypeReference<T> type) throws DecodeException {
    T value;
    try {
      value = DatabindCodec.mapper.readValue(parser, type);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode:" + e.getMessage(), e);
    } finally {
      close(parser);
    }
    if (type.getType() == Object.class) {
      value = (T) adapt(value);
    }
    return value;
  }

  @Override
  public String toString(Object object, boolean pretty) throws EncodeException {
    try {
      ObjectMapper mapper = pretty ? DatabindCodec.prettyMapper : DatabindCodec.mapper;
      return mapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }

  @Override
  public Buffer toBuffer(Object object, boolean pretty) throws EncodeException {
    try {
      ObjectMapper mapper = pretty ? DatabindCodec.prettyMapper : DatabindCodec.mapper;
      return Buffer.buffer(mapper.writeValueAsBytes(object));
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }

  private static Object adapt(Object o) {
    try {
      if (o instanceof List) {
        List list = (List) o;
        return new JsonArray(list);
      } else if (o instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) o;
        return new JsonObject(map);
      }
      return o;
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage());
    }
  }
}
