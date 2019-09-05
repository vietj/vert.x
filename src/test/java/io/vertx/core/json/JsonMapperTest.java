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

package io.vertx.core.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.impl.DatabindCodec;
import io.vertx.core.json.impl.JacksonCodec;
import io.vertx.core.json.mappers.BooleanPojo;
import io.vertx.core.json.mappers.DoublePojo;
import io.vertx.core.json.mappers.FloatPojo;
import io.vertx.core.json.mappers.IntegerPojo;
import io.vertx.core.json.mappers.JsonArrayPojo;
import io.vertx.core.json.mappers.JsonObjectPojo;
import io.vertx.core.json.mappers.LongPojo;
import io.vertx.core.json.mappers.ShortPojo;
import io.vertx.core.json.mappers.Pojo;
import io.vertx.core.spi.json.JsonCodec;
import io.vertx.core.spi.json.JsonMapper;
import io.vertx.test.core.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.junit.Assert.*;

/**
 * @author <a href="https://github.com/lukehutch">Luke Hutchison</a>
 */
@RunWith(Parameterized.class)
public class JsonMapperTest {

  @Parameterized.Parameters
  public static Collection<Object[]> mappers() {
    return Arrays.asList(new Object[][] {
      { new DatabindCodec() }, { new JacksonCodec() }
    });
  }

  public static class MyType {
    public int a;
    public String b;
    public HashMap<String, Object> c = new HashMap<>();
    public List<MyType> d = new ArrayList<>();
    public List<Integer> e = new ArrayList<>();
  }

  public static class MyTypeMapper implements JsonMapper<MyType, JsonObject> {

    @Override
    public MyType deserialize(JsonObject json) throws IllegalArgumentException {
      MyType value = new MyType();
      value.a = json.getInteger("a", 0);
      value.b = json.getString("b");
      value.c = new HashMap<>(json.getJsonObject("c").getMap());
      JsonArray d = json.getJsonArray("d");
      if (d != null) {
        for (Object elt : d) {
          value.d.add(deserialize((JsonObject) elt));
        }
      }
      JsonArray e = json.getJsonArray("e");
      if (e != null) {
        for (Object elt : e) {
          value.e.add((Integer) elt);
        }
      }
      return value;
    }

    @Override
    public JsonObject serialize(MyType value) throws IllegalArgumentException {
      JsonObject o = new JsonObject();
      o.put("a", value.a);
      o.put("b", value.b);
      o.put("c", value.c);
      JsonArray d = new JsonArray();
      value.d.forEach(elt -> d.add(serialize(elt)));
      o.put("d", d);
      JsonArray e = new JsonArray();
      value.e.forEach(e::add);
      o.put("e", e);
      return o;
    }

    @Override
    public Class<MyType> getTargetClass() {
      return MyType.class;
    }
  }

  private JacksonCodec codec;

  public JsonMapperTest(JacksonCodec codec) {
    this.codec = codec;
  }

  @Test
  public void testSerialization() {
    MyType myObj0 = new MyType();
    myObj0.a = -1;
    myObj0.b = "obj0";
    myObj0.c.put("z", Arrays.asList(7, 8));
    myObj0.e.add(9);
    MyType myObj1 = new MyType();
    myObj1.a = 5;
    myObj1.b = "obj1";
    myObj1.c.put("x", "1");
    myObj1.c.put("y", 2);
    myObj1.d.add(myObj0);
    myObj1.e.add(3);

    JsonObject jsonObject1 = codec.toValue(myObj1, JsonObject.class);
    String jsonStr1 = jsonObject1.encode();
    assertEquals("{\"a\":5,\"b\":\"obj1\",\"c\":{\"x\":\"1\",\"y\":2},\"d\":["
        +"{\"a\":-1,\"b\":\"obj0\",\"c\":{\"z\":[7,8]},\"d\":[],\"e\":[9]}"
        + "],\"e\":[3]}", jsonStr1);

    MyType myObj1Roundtrip = JsonCodec.INSTANCE.fromValue(jsonObject1.getMap(), MyType.class);
    assertEquals(myObj1Roundtrip.a, 5);
    assertEquals(myObj1Roundtrip.b, "obj1");
    assertEquals(myObj1Roundtrip.c.get("x"), "1");
    assertEquals(myObj1Roundtrip.c.get("y"), new Integer(2));
    assertEquals(myObj1Roundtrip.e, Arrays.asList(3));
    MyType myObj0Roundtrip = myObj1Roundtrip.d.get(0);
    assertEquals(myObj0Roundtrip.a, -1);
    assertEquals(myObj0Roundtrip.b, "obj0");
    assertEquals(myObj0Roundtrip.c.get("z"), Arrays.asList(7, 8));
    assertEquals(myObj0Roundtrip.e, Arrays.asList(9));

    boolean caughtCycle = false;
    try {
      myObj0.d.add(myObj0);
      codec.toValue(myObj0, JsonObject.class);
    } catch (Throwable e) {
      caughtCycle = true;
    }
    if (!caughtCycle) {
      fail();
    }
  }

  @Test
  public void testInstantFromPOJO() {
    Pojo object = new Pojo();
    object.instant = Instant.now();
    JsonObject json = codec.toValue(object, JsonObject.class);
    // attempt to deserialize back to a instant, asserting for not null
    // already means that there was an attempt to parse a string to instant
    // and that the parsing succeeded (the object is of type instant and not null)
    assertNotNull(json.getInstant("instant"));
  }

  @Test
  public void testInstantToPOJO() {
    Pojo obj = codec.fromValue(new JsonObject().put("instant", Instant.EPOCH), Pojo.class);
    assertEquals(Instant.EPOCH, obj.instant);
  }

  @Test
  public void testInvalidInstantToPOJO() {
    testInvalidValueToPOJO("instant");
  }

  @Test
  public void testBase64FromPOJO() {
    Pojo object = new Pojo();
    object.bytes = "Hello World!".getBytes();
    JsonObject json = codec.toValue(object, JsonObject.class);
    // attempt to deserialize back to a byte[], asserting for not null
    // already means that there was an attempt to parse a string to byte[]
    // and that the parsing succeeded (the object is of type byte[] and not null)
    assertNotNull(json.getBinary("bytes"));
  }

  @Test
  public void testBase64ToPOJO() {
    Pojo obj = codec.fromValue(new JsonObject().put("bytes", "Hello World!".getBytes()), Pojo.class);
    assertArrayEquals("Hello World!".getBytes(), obj.bytes);
  }

  @Test
  public void testInvalidBase64ToPOJO() {
    testInvalidValueToPOJO("bytes");
  }

  private void testInvalidValueToPOJO(String key) {
    try {
      new JsonObject().put(key, "1").mapTo(Pojo.class);
      fail();
    } catch (DecodeException e) {
//      assertThat(e.getCause(), is(instanceOf(InvalidFormatException.class)));
//      InvalidFormatException ife = (InvalidFormatException) e.getCause();
//      assertEquals("1", ife.getValue());
    }
  }

  @Test
  public void testNullPOJO() {
    assertNull(JsonObject.mapFrom(null));
  }

  @Test
  public void testInstantDecoding() {
    Pojo original = new Pojo();
    original.instant = Instant.from(ISO_INSTANT.parse("2018-06-20T07:25:38.397Z"));
    Pojo decoded = codec.fromString("{\"instant\":\"2018-06-20T07:25:38.397Z\"}", Pojo.class);
    assertEquals(original.instant, decoded.instant);
  }

  @Test
  public void testNullInstantDecoding() {
    Pojo original = new Pojo();
    Pojo decoded = codec.fromString("{\"instant\":null}", Pojo.class);
    assertEquals(original.instant, decoded.instant);
  }

  @Test
  public void testBytesDecoding() {
    Pojo original = new Pojo();
    original.bytes = TestUtils.randomByteArray(12);
    Pojo decoded = codec.fromString("{\"bytes\":\"" + Base64.getEncoder().encodeToString(original.bytes) + "\"}", Pojo.class);
    assertArrayEquals(original.bytes, decoded.bytes);
  }

  @Test
  public void testNullBytesDecoding() {
    Pojo original = new Pojo();
    Pojo decoded = codec.fromString("{\"bytes\":null}", Pojo.class);
    assertEquals(original.bytes, decoded.bytes);
  }

  @Test
  public void booleanMapperTest() {
    BooleanPojo pojo = new BooleanPojo();
    pojo.setValue(true);
    assertEquals(Buffer.buffer("true"), codec.toBuffer(pojo));
    assertEquals(pojo, codec.fromBuffer(Buffer.buffer("true"), BooleanPojo.class));
  }

  @Test(expected = DecodeException.class)
  public void booleanMapperWrongTypeTest() {
    codec.fromBuffer(Buffer.buffer("aaaa"), BooleanPojo.class);
  }

  @Test
  public void doubleMapperTest() {
    DoublePojo pojo = new DoublePojo();
    pojo.setValue(1.2d);
    assertEquals(Buffer.buffer("1.2"), codec.toBuffer(pojo));
    assertEquals(pojo, codec.fromBuffer(Buffer.buffer("1.2"), DoublePojo.class));
  }

  @Test(expected = DecodeException.class)
  public void doubleMapperWrongTypeTest() {
    codec.fromBuffer(Buffer.buffer(""), DoublePojo.class);
  }

  @Test
  public void floatMapperTest() {
    FloatPojo pojo = new FloatPojo();
    pojo.setValue(1.2f);
    assertEquals(Buffer.buffer("1.2"), codec.toBuffer(pojo));
    assertEquals(pojo, codec.fromBuffer(Buffer.buffer("1.2"), FloatPojo.class));
  }

  @Test(expected = DecodeException.class)
  public void floatMapperWrongTypeTest() {
    codec.fromBuffer(Buffer.buffer(""), FloatPojo.class);
  }

  @Test
  public void intMapperTest() {
    IntegerPojo pojo = new IntegerPojo();
    pojo.setValue(1);
    assertEquals(Buffer.buffer("1"), codec.toBuffer(pojo));
    assertEquals(pojo, codec.fromBuffer(Buffer.buffer("1"), IntegerPojo.class));
  }

  @Test(expected = DecodeException.class)
  public void intMapperWrongTypeTest() {
    codec.fromBuffer(Buffer.buffer(""), IntegerPojo.class);
  }

  @Test
  public void longMapperTest() {
    LongPojo pojo = new LongPojo();
    pojo.setValue(1L);
    assertEquals(Buffer.buffer("1"), codec.toBuffer(pojo));
    assertEquals(pojo, codec.fromBuffer(Buffer.buffer("1"), LongPojo.class));
  }

  @Test(expected = DecodeException.class)
  public void longMapperWrongTypeTest() {
    codec.fromBuffer(Buffer.buffer(""), LongPojo.class);
  }

  @Test
  public void shortMapperTest() {
    ShortPojo pojo = new ShortPojo();
    pojo.setValue((short)1);
    assertEquals(Buffer.buffer("1"), codec.toBuffer(pojo));
    assertEquals(pojo, codec.fromBuffer(Buffer.buffer("1"), ShortPojo.class));
  }

  @Test(expected = DecodeException.class)
  public void shortCodecWrongTypeTest() {
    codec.fromBuffer(Buffer.buffer(""), ShortPojo.class);
  }

  @Test
  public void jsonArrayMapperTest() {
    JsonArrayPojo pojo = new JsonArrayPojo();
    JsonArray array = new JsonArray().add(1).add(2).add(3);
    pojo.setValue(array);
    assertEquals(Buffer.buffer("[1,2,3]"), codec.toBuffer(pojo));
    assertEquals(pojo, codec.fromBuffer(Buffer.buffer("[1,2,3]"), JsonArrayPojo.class));
  }

  @Test(expected = DecodeException.class)
  public void jsonArrayMapperWrongTypeTest() {
    codec.fromBuffer(Buffer.buffer("2"), JsonArrayPojo.class);
  }

  @Test
  public void jsonObjectMapperTest() {
    JsonObjectPojo pojo = new JsonObjectPojo();
    JsonObject obj = new JsonObject().put("a", 1).put("b", "c");
    pojo.setValue(obj);
    assertEquals(Buffer.buffer("{\"a\":1,\"b\":\"c\"}"), codec.toBuffer(pojo));
    assertEquals(pojo, codec.fromBuffer(Buffer.buffer("{\"a\":1,\"b\":\"c\"}"), JsonObjectPojo.class));
  }

  @Test(expected = DecodeException.class)
  public void jsonObjectMapperWrongTypeTest() {
    codec.fromBuffer(Buffer.buffer("2"), JsonObjectPojo.class);
  }
}
