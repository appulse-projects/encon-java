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

package io.appulse.encon.module.server;

import static io.netty.channel.ChannelOption.ALLOCATOR;
import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.SO_REUSEADDR;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.WRITE_BUFFER_WATER_MARK;
import static io.netty.handler.logging.LogLevel.DEBUG;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

import io.appulse.encon.module.NodeInternalApi;
import io.appulse.encon.module.connection.handshake.HandshakeServerInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
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
public final class ServerModule implements ServerModuleApi, Closeable {

  int port;

  NodeInternalApi internal;

  EventLoopGroup bossGroup;

  EventLoopGroup workerGroup;

  AtomicBoolean closed = new AtomicBoolean(false);

  public ServerModule (@NonNull NodeInternalApi internal) {
    this.internal = internal;

    val serverConfig = internal.config().getServer();
    port = serverConfig.getPort();

    if (Epoll.isAvailable()) {
      bossGroup = new EpollEventLoopGroup(
          serverConfig.getBossThreads(),
          new DefaultThreadFactory(getThreadName("boss"))
      );
      workerGroup = new EpollEventLoopGroup(
          serverConfig.getWorkerThreads(),
          new DefaultThreadFactory(getThreadName("worker"))
      );
    } else {
      bossGroup = new NioEventLoopGroup(
          serverConfig.getBossThreads(),
          new DefaultThreadFactory(getThreadName("boss"))
      );
      workerGroup = new NioEventLoopGroup(
          serverConfig.getWorkerThreads(),
          new DefaultThreadFactory(getThreadName("worker"))
      );
    }

    start();
  }

  @Override
  @SneakyThrows
  public void close () {
    if (closed.get()) {
      log.debug("Server was already closed");
      return;
    }

    closed.set(true);
    log.debug("Closing server module of {}",
              internal.node().getDescriptor().getFullName());

    workerGroup.shutdownGracefully();
    bossGroup.shutdownGracefully();
    log.debug("Boss and workers groups are closed");

    log.debug("Server of {} was closed",
              internal.node().getDescriptor().getFullName());
  }

  @SneakyThrows
  private void start () {
    log.debug("Starting server on port {}", port);
    val bootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .handler(new LoggingHandler(DEBUG))
        .childHandler(HandshakeServerInitializer.builder()
            .internal(internal)
            .build())
        .option(SO_BACKLOG, 1024)
        .option(SO_REUSEADDR, true)
        .childOption(SO_REUSEADDR, true)
        .childOption(SO_KEEPALIVE, true)
        .childOption(TCP_NODELAY, true)
        .childOption(WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(64 * 1024, 128 * 1024))
        .childOption(ALLOCATOR, new PooledByteBufAllocator(false));

    if (Epoll.isAvailable()) {
      bootstrap.channel(EpollServerSocketChannel.class);
    } else {
      bootstrap.channel(NioServerSocketChannel.class);
    }
    bootstrap.bind(port);
  }

  private String getThreadName (@NonNull String suffix) {
    return new StringBuilder()
        .append("server-")
        .append(internal.node().getDescriptor().getShortName())
        .append('-')
        .append(suffix)
        .toString();
  }
}
