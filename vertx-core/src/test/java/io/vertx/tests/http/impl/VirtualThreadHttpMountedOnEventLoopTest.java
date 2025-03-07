/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.tests.http.impl;

import io.vertx.core.ThreadingModel;
import io.vertx.core.internal.ContextInternal;

public class VirtualThreadHttpMountedOnEventLoopTest extends VirtualThreadHttpTestBase {

  protected ContextInternal createVirtualThreadContext() {
    return vertx.createContext(ThreadingModel.VIRTUAL_THREAD_MOUNTED_ON_EVENT_LOOP);
  }
}
