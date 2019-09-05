package io.vertx.core.json;

import io.vertx.core.json.mappers.*;
import org.junit.Test;

import static io.vertx.core.json.Json.encodeToBuffer;
import static org.junit.Assert.assertEquals;

public class JsonCodecLoaderTest {

  @Test
  public void booleanCodecTest() {
    BooleanPojo pojo = new BooleanPojo();
    pojo.setValue(true);
    assertEquals(encodeToBuffer(true), JsonCodecMapper.encodeBuffer(pojo));
    assertEquals(pojo, JsonCodecMapper.decodeBuffer(encodeToBuffer(true), BooleanPojo.class));
  }

  @Test(expected = ClassCastException.class)
  public void booleanCodecWrongTypeTest() {
    JsonCodecMapper.decodeBuffer(encodeToBuffer("aaa"), BooleanPojo.class);
  }

  @Test
  public void doubleCodecTest() {
    DoublePojo pojo = new DoublePojo();
    pojo.setValue(1.2d);
    assertEquals(encodeToBuffer(1.2d), JsonCodecMapper.encodeBuffer(pojo));
    assertEquals(pojo, JsonCodecMapper.decodeBuffer(encodeToBuffer(1.2d), DoublePojo.class));
  }

  @Test(expected = ClassCastException.class)
  public void doubleCodecWrongTypeTest() {
    JsonCodecMapper.decodeBuffer(encodeToBuffer(""), DoublePojo.class);
  }

  @Test
  public void floatCodecTest() {
    FloatPojo pojo = new FloatPojo();
    pojo.setValue(1.2f);
    assertEquals(encodeToBuffer(1.2f), JsonCodecMapper.encodeBuffer(pojo));
    assertEquals(pojo, JsonCodecMapper.decodeBuffer(encodeToBuffer(1.2f), FloatPojo.class));
  }

  @Test(expected = ClassCastException.class)
  public void floatCodecWrongTypeTest() {
    JsonCodecMapper.decodeBuffer(encodeToBuffer(""), FloatPojo.class);
  }

  @Test
  public void intCodecTest() {
    IntegerPojo pojo = new IntegerPojo();
    pojo.setValue(1);
    assertEquals(encodeToBuffer((int)1), JsonCodecMapper.encodeBuffer(pojo));
    assertEquals(pojo, JsonCodecMapper.decodeBuffer(encodeToBuffer((int)1), IntegerPojo.class));
  }

  @Test(expected = ClassCastException.class)
  public void intCodecWrongTypeTest() {
    JsonCodecMapper.decodeBuffer(encodeToBuffer(""), IntegerPojo.class);
  }

  @Test
  public void longCodecTest() {
    LongPojo pojo = new LongPojo();
    pojo.setValue(1L);
    assertEquals(encodeToBuffer(1L), JsonCodecMapper.encodeBuffer(pojo));
    assertEquals(pojo, JsonCodecMapper.decodeBuffer(encodeToBuffer(1L), LongPojo.class));
  }

  @Test(expected = ClassCastException.class)
  public void longCodecWrongTypeTest() {
    JsonCodecMapper.decodeBuffer(encodeToBuffer(""), LongPojo.class);
  }

  @Test
  public void shortCodecTest() {
    ShortPojo pojo = new ShortPojo();
    pojo.setValue((short)1);
    assertEquals(encodeToBuffer((short)1), JsonCodecMapper.encodeBuffer(pojo));
    assertEquals(pojo, JsonCodecMapper.decodeBuffer(encodeToBuffer((short)1), ShortPojo.class));
  }

  @Test(expected = ClassCastException.class)
  public void shortCodecWrongTypeTest() {
    JsonCodecMapper.decodeBuffer(encodeToBuffer(""), ShortPojo.class);
  }

  @Test
  public void jsonArrayCodecTest() {
    JsonArrayPojo pojo = new JsonArrayPojo();
    JsonArray array = new JsonArray().add(1).add(2).add(3);
    pojo.setValue(array);
    assertEquals(array.toBuffer(), JsonCodecMapper.encodeBuffer(pojo));
    assertEquals(pojo, JsonCodecMapper.decodeBuffer(array.toBuffer(), JsonArrayPojo.class));
  }

  @Test(expected = ClassCastException.class)
  public void jsonArrayCodecWrongTypeTest() {
    JsonCodecMapper.decodeBuffer(encodeToBuffer(2), JsonArrayPojo.class);
  }

  @Test
  public void jsonObjectCodecTest() {
    JsonObjectPojo pojo = new JsonObjectPojo();
    JsonObject obj = new JsonObject().put("a", 1).put("b", "c");
    pojo.setValue(obj);
    assertEquals(obj.toBuffer(), JsonCodecMapper.encodeBuffer(pojo));
    assertEquals(pojo, JsonCodecMapper.decodeBuffer(obj.toBuffer(), JsonObjectPojo.class));
  }

  @Test(expected = ClassCastException.class)
  public void jsonObjectCodecWrongTypeTest() {
    JsonCodecMapper.decodeBuffer(encodeToBuffer(2), JsonObjectPojo.class);
  }
  
}
