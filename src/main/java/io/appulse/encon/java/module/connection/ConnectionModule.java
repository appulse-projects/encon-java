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

package io.appulse.encon.java.module.connection;

import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.connection.handshake.HandshakeStrategy;
import io.appulse.encon.java.module.NodeInternalApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ConnectionModule implements ConnectionModuleApi, Closeable {

  NodeInternalApi internal;

  Map<RemoteNode, Connection> cache = new ConcurrentHashMap<>();

  HandshakeStrategy handshakeStrategy = new HandshakeStrategy();

  @Override
  public void close() {
    cache.clear();
  }

  public Connection connect (@NonNull RemoteNode remote) {
    return cache.compute(remote, (key, value) -> {
      if (value != null) {
        return value;
      }
      val socket = handshakeStrategy.handshake(internal.node(), key);
      return new Connection(internal.node(), key, socket);
    });
  }

  public boolean isAvailable (@NonNull RemoteNode remote) {
    try {
      return connect(remote) != null;
    } catch (RuntimeException ex) {
      return false;
    }
  }

  public void remove (@NonNull RemoteNode remote) {
    cache.remove(remote);
  }
}
