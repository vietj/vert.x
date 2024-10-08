/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.eventbus;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryContext;
import io.vertx.core.eventbus.EventBus;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class EventBusInterceptorTest extends VertxTestBase {

  protected EventBus eb;

  @Test
  public void testOutboundInterceptorOnSend() {
    eb.addOutboundInterceptor(sc -> {
      assertEquals("armadillo", sc.message().body());
      assertSame(sc.body(), sc.message().body());
      assertTrue(sc.send());
      sc.next();
    });
    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      testComplete();
    });
    eb.send("some-address", "armadillo");
    await();
  }

  @Test
  public void testInterceptorsOnSend() {
    eb.addOutboundInterceptor(sc -> {
      assertEquals("armadillo", sc.message().body());
      assertTrue(sc.send());
      sc.next();
    }).addInboundInterceptor(dc -> {
      assertEquals("armadillo", dc.message().body());
      assertSame(dc.body(), dc.message().body());
      assertTrue(dc.send());
      dc.next();
    });
    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      testComplete();
    });
    eb.send("some-address", "armadillo");
    await();
  }

  @Test
  public void testOutboundInterceptorOnPublish() {
    eb.addOutboundInterceptor(sc -> {
      assertEquals("armadillo", sc.message().body());
      assertFalse(sc.send());
      sc.next();
    });
    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      testComplete();
    });
    eb.publish("some-address", "armadillo");
    await();
  }

  @Test
  public void testInterceptorsOnPublish() {
    eb.addOutboundInterceptor(sc -> {
      assertEquals("armadillo", sc.message().body());
      assertFalse(sc.send());
      sc.next();
    }).addInboundInterceptor(dc -> {
      assertEquals("armadillo", dc.message().body());
      assertFalse(dc.send());
      dc.next();
    });
    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      testComplete();
    });
    eb.publish("some-address", "armadillo");
    await();
  }

  @Test
  public void testOutboundInterceptorNoNext() {
    eb.addOutboundInterceptor(sc -> {
      assertEquals("armadillo", sc.message().body());
    });
    eb.consumer("some-address", msg -> {
      fail("Should not receive message");
    });
    eb.send("some-address", "armadillo");
    vertx.setTimer(200, tid -> testComplete());
    await();
  }

  @Test
  public void testInboundInterceptorNoNext() {
    eb.addInboundInterceptor(dc -> {
      assertEquals("armadillo", dc.message().body());
    });

    eb.consumer("some-address", msg -> {
      fail("Should not receive message");
    });
    eb.send("some-address", "armadillo");
    vertx.setTimer(200, tid -> testComplete());
    await();
  }

  @Test
  public void testMultipleOutboundInterceptors() {
    AtomicInteger cnt = new AtomicInteger();
    int interceptorNum = 10;
    for (int i = 0; i < interceptorNum; i++) {
      final int expectedCount = i;
      eb.addOutboundInterceptor(sc -> {
        assertEquals("armadillo", sc.message().body());
        int count = cnt.getAndIncrement();
        assertEquals(expectedCount, count);
        sc.next();
      });
    }
    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      assertEquals(interceptorNum, cnt.get());
      testComplete();
    });
    eb.send("some-address", "armadillo");
    await();
  }

  @Test
  public void testRemoveInterceptor() {

    AtomicInteger cnt1 = new AtomicInteger();
    AtomicInteger cnt2 = new AtomicInteger();
    AtomicInteger cnt3 = new AtomicInteger();

    Handler<DeliveryContext<Object>> eb1 = sc -> {
      cnt1.incrementAndGet();
      sc.next();
    };

    Handler<DeliveryContext<Object>> eb2 = sc -> {
      cnt2.incrementAndGet();
      sc.next();
    };

    Handler<DeliveryContext<Object>> eb3 = sc -> {
      cnt3.incrementAndGet();
      sc.next();
    };

    eb
      .addInboundInterceptor(eb1).addOutboundInterceptor(eb1)
      .addInboundInterceptor(eb2).addOutboundInterceptor(eb2)
      .addInboundInterceptor(eb3).addOutboundInterceptor(eb3);

    eb.consumer("some-address", msg -> {
      if (msg.body().equals("armadillo")) {
        assertEquals(2, cnt1.get());
        assertEquals(2, cnt2.get());
        assertEquals(2, cnt3.get());
        eb.removeInboundInterceptor(eb2).removeOutboundInterceptor(eb2);
        eb.send("some-address", "aardvark");
      } else if (msg.body().equals("aardvark")) {
        assertEquals(4, cnt1.get());
        assertEquals(2, cnt2.get());
        assertEquals(4, cnt3.get());
        eb.removeInboundInterceptor(eb3).removeOutboundInterceptor(eb3);
        eb.send("some-address", "anteater");
      } else if (msg.body().equals("anteater")) {
        assertEquals(6, cnt1.get());
        assertEquals(2, cnt2.get());
        assertEquals(4, cnt3.get());
        testComplete();
      } else {
        fail("wrong body");
      }
    });
    eb.send("some-address", "armadillo");
    await();
  }

  @Test
  public void testOutboundInterceptorOnReply() {
    AtomicInteger cnt = new AtomicInteger();
    eb.addOutboundInterceptor(sc -> {
      if (sc.message().body().equals("armadillo")) {
        assertEquals(0, cnt.get());
      } else if (sc.message().body().equals("echidna")) {
        assertEquals(1, cnt.get());
      } else {
        fail("wrong body");
      }
      cnt.incrementAndGet();
      sc.next();
    });
    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      assertEquals(1, cnt.get());
      msg.reply("echidna");
    });
    eb.request("some-address", "armadillo").onComplete(onSuccess(reply -> {
      assertEquals("echidna", reply.body());
      assertEquals(2, cnt.get());
      testComplete();
    }));
    await();
  }

  @Test
  public void testInboundInterceptorOnReply() {
    AtomicInteger cnt = new AtomicInteger();

    eb.addInboundInterceptor(dc -> {
      if (dc.message().body().equals("armadillo")) {
        assertEquals(0, cnt.get());
      } else if (dc.message().body().equals("echidna")) {
        assertEquals(1, cnt.get());
      } else {
        fail("wrong body");
      }
      cnt.incrementAndGet();
      dc.next();
    });

    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      assertEquals(1, cnt.get());
      msg.reply("echidna");
    });
    eb.request("some-address", "armadillo").onComplete(onSuccess(reply -> {
      assertEquals("echidna", reply.body());
      assertEquals(2, cnt.get());
      testComplete();
    }));
    await();
  }

  @Test
  public void testExceptionInOutboundInterceptor() {
    AtomicInteger cnt = new AtomicInteger();

    Handler<DeliveryContext<Object>> eb1 = sc -> {
      cnt.incrementAndGet();
      vertx.runOnContext(v -> sc.next());
      throw new RuntimeException("foo");
    };

    Handler<DeliveryContext<Object>> eb2 = sc -> {
      cnt.incrementAndGet();
      sc.next();
    };

    eb.addOutboundInterceptor(eb1).addOutboundInterceptor(eb2);

    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      assertEquals(2, cnt.get());
      testComplete();
    });
    eb.send("some-address", "armadillo");
    await();
  }

  @Test
  public void testExceptionInInboundInterceptor() {
    AtomicInteger cnt = new AtomicInteger();

    Handler<DeliveryContext<Object>> eb1 = dc -> {
      cnt.incrementAndGet();
      vertx.runOnContext(v -> dc.next());
      throw new RuntimeException("foo");
    };

    Handler<DeliveryContext<Object>> eb2 = dc -> {
      cnt.incrementAndGet();
      dc.next();
    };

    eb.addInboundInterceptor(eb1).addInboundInterceptor(eb2);

    eb.consumer("some-address", msg -> {
      assertEquals("armadillo", msg.body());
      assertEquals(2, cnt.get());
      testComplete();
    });
    eb.send("some-address", "armadillo");
    await();
  }

  @Test
  public void testInboundInterceptorContextOnSend() {
    testInboundInterceptorContext(false, false);
  }

  @Test
  public void testInboundInterceptorContextOnReply() {
    testInboundInterceptorContext(true, false);
  }

  @Test
  public void testInboundInterceptorContextOnReplyFailure() {
    testInboundInterceptorContext(true, true);
  }

  private void testInboundInterceptorContext(boolean reply, boolean failure) {
    waitFor(reply ? 2:1);
    AtomicReference<Context> msgCtx = new AtomicReference<>();
    AtomicReference<Context> replyCtx = new AtomicReference<>();
    eb.addInboundInterceptor(dc -> {
      if ("bar".equals(dc.body())) {
        msgCtx.set(Vertx.currentContext());
      } else {
        replyCtx.set(Vertx.currentContext());
      }
      dc.next();
    });
    eb.consumer("foo", msg -> {
      assertSame(msgCtx.get(), Vertx.currentContext());
      if (failure) {
        msg.fail(42, "fail");
      } else if (reply) {
        msg.reply("baz");
      }
      complete();
    });
    if (reply) {
      eb.request("foo", "bar").onComplete(ar -> {
        assertEquals(failure, ar.failed());
        assertSame(replyCtx.get(), Vertx.currentContext());
        complete();
      });
    } else {
      eb.send("foo", "bar");
    }
    await();
  }

  @Test
  public void testOutboundInterceptorFromNonVertxThreadDispatch() {
    AtomicReference<Thread> interceptorThread = new AtomicReference<>();
    eb.addOutboundInterceptor(sc -> {
      interceptorThread.set(Thread.currentThread());
    });
    eb.consumer("some-address", msg -> {
    });
    eb.send("some-address", "armadillo");
    assertSame(Thread.currentThread(), interceptorThread.get());
  }

  @Test
  public void testOutboundInterceptorFromNonVertxThreadFailure() {
    RuntimeException expected = new RuntimeException();
    eb.addOutboundInterceptor(sc -> {
      throw expected;
    });
    eb.consumer("some-address", msg -> {
    });
    AtomicReference<Throwable> caught = new AtomicReference<>();
    vertx.exceptionHandler(err -> caught.set(err));
    eb.send("some-address", "armadillo");
    assertSame(expected, caught.get());
  }

  @Test
  public void testInboundInterceptorFromNonVertxThreadDispatch() {
    AtomicReference<Thread> interceptorThread = new AtomicReference<>();
    AtomicReference<Thread> th = new AtomicReference<>();
    eb.addInboundInterceptor(sc -> {
      new Thread(() -> {
        th.set(Thread.currentThread());
        sc.next();
      }).start();
    });
    eb.addInboundInterceptor(sc -> {
      interceptorThread.set(Thread.currentThread());
    });
    eb.consumer("some-address", msg -> {
    });
    eb.send("some-address", "armadillo");
    waitUntil(() -> interceptorThread.get() != null);
    assertSame(th.get(), interceptorThread.get());
  }

  @Test
  public void testInboundInterceptorFromNonVertxThreadFailure() {
    RuntimeException expected = new RuntimeException();
    eb.addInboundInterceptor(sc -> {
      new Thread(() -> {
        sc.next();
      }).start();
    });
    eb.addInboundInterceptor(sc -> {
      throw expected;
    });
    eb.consumer("some-address", msg -> {
    });
    AtomicReference<Throwable> caught = new AtomicReference<>();
    vertx.exceptionHandler(err -> caught.set(err));
    eb.send("some-address", "armadillo");
    waitUntil(() -> caught.get() != null);
    assertSame(expected, caught.get());
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    eb = vertx.eventBus();
  }
}
