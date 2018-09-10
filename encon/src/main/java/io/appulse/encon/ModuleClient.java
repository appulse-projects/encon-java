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

package io.appulse.encon;

import static io.netty.channel.ChannelOption.ALLOCATOR;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static io.netty.channel.ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.Connection;
import io.appulse.encon.connection.handshake.HandshakeClientInitializer;

import io.netty.bootstrap.Bootstrap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.2.0
 * @author Artem Labazin
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ModuleClient implements Closeable {

  @NonNull
  Node node;

  @NonNull
  ModuleConnection moduleConnection;

  boolean shortNamedNode;

  @Override
  public void close () {
    log.debug("Closing sever module");
    moduleConnection.close();
    log.debug("Server module closed");
  }

  CompletableFuture<Connection> connectAsync (@NonNull RemoteNode remote) {
    if (shortNamedNode) {
      if (remote.getDescriptor().isLongName()) {
        val msg = String.format("Short-named node '%s' couldn't be connected to long-named node '%s'",
                                node.getDescriptor().getFullName(),
                                remote.getDescriptor().getFullName());

        throw new IllegalArgumentException(msg);
      }
    } else if (remote.getDescriptor().isShortName()) {
      val msg = String.format("Long-named node '%s' couldn't be connected to short-named node '%s'",
                              node.getDescriptor().getFullName(),
                              remote.getDescriptor().getFullName());

      throw new IllegalArgumentException(msg);
    }
    return moduleConnection.compute(remote, this::createConnection);
  }

  Connection connect (@NonNull RemoteNode remote) {
    return connect(remote, 5, SECONDS);
  }

  @SneakyThrows
  Connection connect (@NonNull RemoteNode remote, long timeout, @NonNull TimeUnit unit) {
    return connectAsync(remote).get(timeout, unit);
  }

  boolean isAvailable (@NonNull RemoteNode remote) {
    try {
      return connect(remote) != null;
    } catch (Exception ex) {
      return false;
    }
  }

  private CompletableFuture<Connection> createConnection (@NonNull RemoteNode remote) {
    log.debug("Creating new client's connection\nto {}", remote);
    CompletableFuture<Connection> future = new CompletableFuture<>();

    new Bootstrap()
        .group(moduleConnection.getWorkerGroup())
        .channel(moduleConnection.getClientChannelClass())
        .option(SO_KEEPALIVE, true)
        .option(TCP_NODELAY, true)
        .option(CONNECT_TIMEOUT_MILLIS, 5000)
        .option(ALLOCATOR, moduleConnection.getAllocator())
        .option(SINGLE_EVENTEXECUTOR_PER_GROUP, true)
        .handler(HandshakeClientInitializer.builder()
            .node(node)
            .future(future)
            .remote(remote)
            .channelCloseAction(remoteNode -> {
              log.debug("Closing connection to {}", remoteNode);
              node.moduleLookup.remove(remoteNode);
              node.moduleConnection.remove(remoteNode);
            })
            .build()
        )
        .connect(remote.getDescriptor().getAddress(),
                 remote.getPort());

    moduleConnection.add(future);
    return future;
  }
}
