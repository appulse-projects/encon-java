/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appulse.encon.config;

import java.util.concurrent.atomic.AtomicInteger;

import io.appulse.utils.SocketUtils;

import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
final class ServerPortGenerator {

  private static final AtomicInteger UPPER_BOUND = new AtomicInteger(65535);

  static int nextPort () {
    while (true) {
      val currentValue = UPPER_BOUND.get();
      val port = SocketUtils.findFreePort(1024, currentValue - 1).get();
      if (UPPER_BOUND.compareAndSet(currentValue, port)) {
        return port;
      }
    }
  }

  private ServerPortGenerator () {
  }
}
