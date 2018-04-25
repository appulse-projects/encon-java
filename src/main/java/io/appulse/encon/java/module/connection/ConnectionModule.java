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

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.handshake.HandshakeClientInitializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class ConnectionModule implements ConnectionModuleApi, Closeable {

  @NonNull
  NodeInternalApi internal;

  Map<RemoteNode, CompletableFuture<Connection>> cache;

  EventLoopGroup workerGroup;

  public ConnectionModule (@NonNull NodeInternalApi internal) {
    this.internal = internal;
    cache = new ConcurrentHashMap<>();

    workerGroup = new NioEventLoopGroup(
        internal.config().getClientThreads(),
        new DefaultThreadFactory(new StringBuilder()
            .append("client-")
            .append(internal.node().getDescriptor().getShortName())
            .toString()
        )
    );
  }

  @Override
  public void close () {
    log.debug("Closing connection module of {}",
              internal.node().getDescriptor().getFullName());

    cache.values().stream()
        .filter(CompletableFuture::isDone)
        .map(CompletableFuture::join)
        .forEach(Connection::close);

    cache.clear();
    log.debug("Connection module's cache of {} was cleared",
              internal.node().getDescriptor().getFullName());

    workerGroup.shutdownGracefully();

    log.debug("Connection module of {} was closed",
              internal.node().getDescriptor().getFullName());
  }

  public void addConnection (@NonNull CompletableFuture<Connection> future) {
    future.thenAccept(it -> {
      log.debug("Connection to {} was added", it.getRemote());
      cache.put(it.getRemote(), future);
    });
  }

  @Override
  public CompletableFuture<Connection> connectAsync (@NonNull RemoteNode remote) {
    return cache.computeIfAbsent(remote, this::createConnection);
  }

  @Override
  public Connection connect (@NonNull RemoteNode remote) {
    return connect(remote, 10, SECONDS);
  }

  @Override
  @SneakyThrows
  public Connection connect (@NonNull RemoteNode remote, long timeout, @NonNull TimeUnit unit) {
    return connectAsync(remote).get(timeout, unit);
  }

  @Override
  public boolean isAvailable (@NonNull RemoteNode remote) {
    try {
      return connect(remote) != null;
    } catch (Exception ex) {
      return false;
    }
  }

  @SneakyThrows
  public void remove (@NonNull RemoteNode remoteNode) {
    val future = cache.remove(remoteNode);
    log.debug("Clear connections cache for {} (existed: {})",
              remoteNode, future != null);

    if (log.isDebugEnabled() && future == null) {
      val keys = cache.keySet()
          .stream()
          .map(RemoteNode::toString)
          .map("  - "::concat)
          .collect(joining("\n"));

      log.debug("Cache keys:\n{}", keys);
    }

    if (future != null && future.isDone()) {
      future.get().close();
    }
  }

  private CompletableFuture<Connection> createConnection (@NonNull RemoteNode remote) {
    CompletableFuture<Connection> future = new CompletableFuture<>();

    new Bootstrap()
        .group(workerGroup)
        .channel(NioSocketChannel.class)
        .option(SO_KEEPALIVE, true)
        .option(CONNECT_TIMEOUT_MILLIS, 5000)
        .handler(HandshakeClientInitializer.builder()
            .internal(internal)
            .future(future)
            .remote(remote)
            .build()
        )
        .connect(remote.getDescriptor().getAddress(), remote.getPort());

    return future.whenComplete((connection, throwable) -> {
      if (throwable == null) {
        val result = CompletableFuture.completedFuture(connection);
        cache.put(remote, result);
      } else {
        remove(remote);
      }
    });
  }
}
