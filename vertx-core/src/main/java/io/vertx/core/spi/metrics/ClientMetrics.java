/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.spi.metrics;

/**
 * The client metrics SPI that Vert.x will use to call when client events occur.<p/>
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface ClientMetrics<M, Req, Resp> extends Metrics {

  /**
   * Called when a client request begins. Vert.x will invoke {@link #requestEnd} when the request
   * has ended or {@link #requestReset} if the request/response has failed before.
   *
   * <p>The request uri is an arbitrary URI that depends on the client, e.g an HTTP request uri,
   * a SQL query, etc...
   *
   * @param uri an arbitrary uri
   * @param request the request object
   * @return the request metric
   */
  default M requestBegin(String uri, Req request) {
    return null;
  }

  /**
   * Calls {@link #requestEnd(Object, long)} with {@code -1L}
   */
  default void requestEnd(M requestMetric) {
    requestEnd(requestMetric, -1L);
  }

  /**
   * Called when the client request ends.
   *
   * @param requestMetric the request metric
   * @param bytesWritten the number of bytes written or {@code -1} when it is not known
   */
  default void requestEnd(M requestMetric, long bytesWritten) {
  }

  /**
   * Called when the client response begins. Vert.x will invoke {@link #responseEnd} when the response has ended
   *  or {@link #requestReset} if the request/response has failed before.
   *
   * @param requestMetric the request metric
   * @param response the response object
   */
  default void responseBegin(M requestMetric, Resp response) {
  }


  /**
   * Called when the client request couldn't complete successfully, for instance the connection
   * was closed before the response was received.
   *
   * @param requestMetric the request metric
   */
  default void requestReset(M requestMetric) {
  }

  /**
   * Calls {@link #responseEnd(Object, long)} with {@code -1L}
   */
  default void responseEnd(M requestMetric) {
    responseEnd(requestMetric, -1L);
  }

  /**
   * Called when the client response has ended
   *
   * @param requestMetric the request metric
   * @param bytesRead the number of bytes read or {@code -1} when it is not known
   */
  default void responseEnd(M requestMetric, long bytesRead) {
  }
}
