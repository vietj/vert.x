/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.impl.DatabindCodec;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class JacksonDatabindTest extends VertxTestBase {

  public static class Pojo {
    @JsonProperty
    public String value;
    @JsonProperty
    public Instant instant;
    @JsonProperty
    public byte[] bytes;
  }

  private DatabindCodec codec = new DatabindCodec();

  @Test
  public void testGetSetMapper() {
    ObjectMapper mapper = DatabindCodec.mapper;
    assertNotNull(mapper);
    ObjectMapper newMapper = new ObjectMapper();
    DatabindCodec.mapper = newMapper;
    assertSame(newMapper, DatabindCodec.mapper);
    DatabindCodec.mapper = mapper;
  }

  @Test
  public void testGetSetPrettyMapper() {
    ObjectMapper mapper = DatabindCodec.prettyMapper;
    assertNotNull(mapper);
    ObjectMapper newMapper = new ObjectMapper();
    DatabindCodec.prettyMapper = newMapper;
    assertSame(newMapper, DatabindCodec.prettyMapper);
    DatabindCodec.prettyMapper = mapper;
  }

  @Test
  public void testGenericDecoding() {
    Pojo original = new Pojo();
    original.value = "test";

    String json = Json.encode(Collections.singletonList(original));
    List<Pojo> correct;

    correct = codec.fromString(json, new TypeReference<List<Pojo>>() {});
    assertTrue(((List)correct).get(0) instanceof Pojo);
    assertEquals(original.value, correct.get(0).value);

    // same must apply if instead of string we use a buffer
    correct = codec.fromBuffer(Buffer.buffer(json, "UTF8"), new TypeReference<List<Pojo>>() {});
    assertTrue(((List)correct).get(0) instanceof Pojo);
    assertEquals(original.value, correct.get(0).value);

    List incorrect = Json.decodeValue(json, List.class);
    assertFalse(incorrect.get(0) instanceof Pojo);
    assertTrue(incorrect.get(0) instanceof Map);
    assertEquals(original.value, ((Map)(incorrect.get(0))).get("value"));
  }
}
