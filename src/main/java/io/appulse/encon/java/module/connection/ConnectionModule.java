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
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ConnectionModule implements ConnectionModuleApi, Closeable {

  @NonNull
  NodeInternalApi internal;

  Map<RemoteNode, CompletableFuture<Connection>> cache;

  EventLoopGroup workerGroup;

  public ConnectionModule (@NonNull NodeInternalApi internal) {
    this.internal = internal;
    cache = new ConcurrentHashMap<>();

    workerGroup = new NioEventLoopGroup(internal.config().getClientThreads());
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

  public CompletionStage<Connection> connectAsync (@NonNull RemoteNode remote) {
    return getOrCreateConnection(remote);
  }

  @SneakyThrows
  public Connection connect (@NonNull RemoteNode remote) {
    return connect(remote, 10, SECONDS);
  }

  @SneakyThrows
  public Connection connect (@NonNull RemoteNode remote, long timeout, @NonNull TimeUnit unit) {
    return getOrCreateConnection(remote).get(timeout, unit);
  }

  private CompletableFuture<Connection> getOrCreateConnection (RemoteNode remote) {
    return cache.compute(remote, (key, value) -> {
      return value == null
             ? createConnection(key)
             : value;
    });
  }

  public boolean isAvailable (@NonNull RemoteNode remote) {
    try {
      return connect(remote) != null;
    } catch (Throwable ex) {
      return false;
    }
  }

  public void remove (@NonNull RemoteNode remote) {
    cache.remove(remote);
  }

  private CompletableFuture<Connection> createConnection (@NonNull RemoteNode remote) {
    CompletableFuture<Connection> future = new CompletableFuture<>();
    val pipeline = Pipeline.builder()
        .internal(internal)
        .future(future)
        .build();

    new Bootstrap()
        .group(workerGroup)
        .channel(NioSocketChannel.class)
        .option(SO_KEEPALIVE, true)
        .option(CONNECT_TIMEOUT_MILLIS, 5000)
        .handler(new ChannelInitializer<SocketChannel>() {

          @Override
          public void initChannel (SocketChannel channel) throws Exception {
            pipeline.setupClientHandshake(channel.pipeline(), remote);
          }
        })
        .connect(remote.getDescriptor().getAddress(), remote.getPort());

    return future;
  }
}
