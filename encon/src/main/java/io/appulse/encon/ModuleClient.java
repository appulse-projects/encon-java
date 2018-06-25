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

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.Connection;
import io.appulse.encon.connection.handshake.HandshakeClientInitializer;
import io.appulse.encon.connection.regular.ConnectionHandler;

import io.netty.bootstrap.Bootstrap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

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

  @Override
  public void close () {
    log.debug("Closing sever module");
    moduleConnection.close();
    log.debug("Server module closed");
  }

  CompletableFuture<Connection> connectAsync (@NonNull RemoteNode remote) {
    return moduleConnection.compute(remote, this::createConnection);
  }

  Connection connect (@NonNull RemoteNode remote) {
    return connect(remote, 60, SECONDS);
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
        .option(CONNECT_TIMEOUT_MILLIS, 5000)
        .handler(HandshakeClientInitializer.builder()
            .node(node)
            .future(future)
            .remote(remote)
            .channelCloseListener(f -> {
              ConnectionHandler connectionHandler = f.channel()
                  .pipeline()
                  .get(ConnectionHandler.class);

              if (connectionHandler == null) {
                return;
              }

              RemoteNode remoteNode = connectionHandler.getRemote();
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
