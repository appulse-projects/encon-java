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
import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.SO_RCVBUF;
import static io.netty.channel.ChannelOption.SO_REUSEADDR;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.WRITE_BUFFER_WATER_MARK;
import static io.netty.handler.logging.LogLevel.DEBUG;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;

import io.appulse.encon.connection.handshake.HandshakeServerInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.logging.LoggingHandler;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.2.0
 * @author Artem Labazin
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ModuleServer implements Closeable {

  int port;

  Node node;

  ModuleConnection moduleConnection;

  ModuleServer (@NonNull Node node, @NonNull ModuleConnection moduleConnection, int port) {
    this.node = node;
    this.moduleConnection = moduleConnection;
    this.port = port;
    start();
  }

  @Override
  public void close () {
    log.debug("Closing sever module");
    moduleConnection.close();
    log.debug("Server module closed");
  }

  @SneakyThrows
  private void start () {
    log.debug("Starting server on port {}", port);

    new ServerBootstrap()
        .group(moduleConnection.getBossGroup(),
               moduleConnection.getWorkerGroup())
        .channel(moduleConnection.getServerChannelClass())
        .handler(new LoggingHandler(DEBUG))
        .childHandler(HandshakeServerInitializer.builder()
            .node(node)
            .consumer(moduleConnection::add)
            .channelCloseAction(remote -> {
              log.debug("Closing connection to {}", remote);
              node.moduleLookup.remove(remote);
              node.moduleConnection.remove(remote);
            })
            .build())
        .option(SO_BACKLOG, 1024)
        .option(SO_REUSEADDR, true)
        .option(CONNECT_TIMEOUT_MILLIS, 5000)
        .option(ALLOCATOR, moduleConnection.getAllocator())
        .option(SINGLE_EVENTEXECUTOR_PER_GROUP, true)
        .childOption(SO_REUSEADDR, true)
        .childOption(SO_KEEPALIVE, true)
        .childOption(TCP_NODELAY, true)
        .childOption(SO_RCVBUF, 128 * 1024)
        .childOption(WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(64 * 1024, 128 * 1024))
        .childOption(ALLOCATOR, moduleConnection.getAllocator())
        .bind(port);
  }
}
