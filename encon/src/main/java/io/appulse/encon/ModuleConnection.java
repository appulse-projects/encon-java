/*
 * Copyright 2018 Appulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appulse.encon;

import static io.netty.util.ResourceLeakDetector.Level.DISABLED;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.Connection;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 24.06.2018
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ModuleConnection implements Closeable {

  static {
    if (!SystemPropertyUtil.contains("io.netty.noResourceLeakDetection")) {
      ResourceLeakDetector.setLevel(DISABLED);
    }
  }

  @Getter
  EventLoopGroup bossGroup;

  @Getter
  EventLoopGroup workerGroup;

  @Getter
  Class<? extends Channel> clientChannelClass;

  @Getter
  Class<? extends ServerChannel> serverChannelClass;

  Map<RemoteNode, CompletableFuture<Connection>> cache;

  ModuleConnection (int bossThreads, int workerThreads) {
    cache = new ConcurrentHashMap<>();

    val bossThreadFactory = new DefaultThreadFactory("boss-group");
    val workerThreadFactory = new DefaultThreadFactory("worker-group");

    if (Epoll.isAvailable()) {
      bossGroup = new EpollEventLoopGroup(bossThreads, bossThreadFactory);
      workerGroup = new EpollEventLoopGroup(workerThreads, workerThreadFactory);
      clientChannelClass = EpollSocketChannel.class;
      serverChannelClass = EpollServerSocketChannel.class;
    } else {
      bossGroup = new NioEventLoopGroup(bossThreads, bossThreadFactory);
      workerGroup = new NioEventLoopGroup(workerThreads, workerThreadFactory);
      clientChannelClass = NioSocketChannel.class;
      serverChannelClass = NioServerSocketChannel.class;
    }
  }

  @Override
  public void close () {
    log.debug("Closing connection module");
    if (!bossGroup.isShuttingDown() && !bossGroup.isShutdown()) {
      log.debug("Shutting down boss threads group");
      bossGroup.shutdownGracefully();
    }
    if (!workerGroup.isShuttingDown() && !workerGroup.isShutdown()) {
      log.debug("Shutting down worker threads group");
      workerGroup.shutdownGracefully();
    }
    log.debug("Connection module closed");
  }

  void add (@NonNull CompletableFuture<Connection> future) {
    future.thenAccept(it -> {
      RemoteNode remote = it.getRemote();
      log.debug("Connection was added for\n  {}", remote);
      cache.putIfAbsent(remote, completedFuture(it));
    });
  }

  CompletableFuture<Connection> compute (@NonNull RemoteNode remote,
                                         @NonNull Function<RemoteNode, CompletableFuture<Connection>> function) {
    return cache.computeIfAbsent(remote, function);
  }

  void remove (@NonNull RemoteNode remote) {
    CompletableFuture<Connection> future = cache.remove(remote);
    if (future == null) {
      return;
    }

    if (future.isDone()) {
      future.join().close();
    } else {
      future.cancel(false);
    }
  }
}
