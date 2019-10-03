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

package io.vertx.core.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

class FutureImpl<T> implements Promise<T>, Future<T> {

  private final ContextInternal context;
  private boolean failed;
  private boolean succeeded;
  private Handler<AsyncResult<T>> handler;
  private T result;
  private Throwable throwable;

  /**
   * Create a future that hasn't completed yet
   */
  FutureImpl() {
    this(null);
  }

  /**
   * Create a future that hasn't completed yet
   */
  FutureImpl(ContextInternal context) {
    this.context = context;
  }

  /**
   * The result of the operation. This will be null if the operation failed.
   */
  public synchronized T result() {
    return result;
  }

  /**
   * An exception describing failure. This will be null if the operation succeeded.
   */
  public synchronized Throwable cause() {
    return throwable;
  }

  /**
   * Did it succeeed?
   */
  public synchronized boolean succeeded() {
    return succeeded;
  }

  /**
   * Did it fail?
   */
  public synchronized boolean failed() {
    return failed;
  }

  /**
   * Has it completed?
   */
  public synchronized boolean isComplete() {
    return failed || succeeded;
  }

  /**
   * Set a handler for the result. It will get called when it's complete
   */
  public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
    boolean callHandler;
    synchronized (this) {
      callHandler = isComplete();
      if (!callHandler) {
        this.handler = handler;
      }
    }
    if (callHandler) {
      callHandler(handler);
    }
    return this;
  }

  private void callHandler(Handler<AsyncResult<T>> handler) {
    if (context != null && Vertx.currentContext() != context) {
      context.runOnContext(this, handler);
    } else {
      handler.handle(this);
    }
  }

  @Override
  public synchronized Handler<AsyncResult<T>> getHandler() {
    return handler;
  }

  @Override
  public void complete(Object result) {
    if (!tryComplete(result)) {
      throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
    }
  }

  @Override
  public void succeed(T result) {
    if (!trySucceed(result)) {
      throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
    }
  }

  @Override
  public void fail(Throwable cause) {
    if (!tryFail(cause)) {
      throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
    }
  }

  @Override
  public void fail(String failureMessage) {
    if (!tryFail(failureMessage)) {
      throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
    }
  }
  @Override
  public boolean trySucceed(T result) {
    Handler<AsyncResult<T>> h;
    synchronized (this) {
      if (succeeded || failed) {
        return false;
      }
      this.result = result;
      succeeded = true;
      h = handler;
      handler = null;
    }
    if (h != null) {
      callHandler(h);
    }
    return true;
  }

  public void handle(Future<T> ar) {
    if (ar.succeeded()) {
      succeed(ar.result());
    } else {
      fail(ar.cause());
    }
  }

  @Override
  public void handle(AsyncResult<T> asyncResult) {
    if (asyncResult.succeeded()) {
      succeed(asyncResult.result());
    } else {
      fail(asyncResult.cause());
    }
  }

  @Override
  public boolean tryFail(Throwable cause) {
    Handler<AsyncResult<T>> h;
    synchronized (this) {
      if (succeeded || failed) {
        return false;
      }
      this.throwable = cause != null ? cause : new NoStackTraceThrowable(null);
      failed = true;
      h = handler;
      handler = null;
    }
    if (h != null) {
      h.handle(this);
    }
    return true;
  }

  @Override
  public boolean tryFail(String failureMessage) {
    return tryFail(new NoStackTraceThrowable(failureMessage));
  }

  @Override
  public Future<T> future() {
    return this;
  }

  @Override
  public String toString() {
    synchronized (this) {
      if (succeeded) {
        return "Future{result=" + result + "}";
      }
      if (failed) {
        return "Future{cause=" + throwable.getMessage() + "}";
      }
      return "Future{unresolved}";
    }
  }
}
