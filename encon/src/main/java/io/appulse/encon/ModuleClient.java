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
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.connection.Connection;
import io.appulse.encon.connection.handshake.HandshakeClientInitializer;
import io.appulse.epmd.java.core.model.Protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Builder;
import lombok.NonNull;
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
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ModuleClient implements Closeable {

  Node node;

  ModuleConnection moduleConnection;

  boolean shortNamedNode;

  UnaryOperator<Bootstrap> settingUpBootstrap;

  @Builder
  ModuleClient (@NonNull Node node,
                @NonNull ModuleConnection moduleConnection,
                @NonNull NodeConfig nodeConfig
  ) {
    this.node = node;
    this.moduleConnection = moduleConnection;
    this.shortNamedNode = nodeConfig.getShortNamed();
    settingUpBootstrap = createSettingUpBootstrap(nodeConfig.getProtocol());
  }

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

    Bootstrap bootstrap = new Bootstrap()
        .group(moduleConnection.getWorkerGroup())
        .option(CONNECT_TIMEOUT_MILLIS, 5000)
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
        );

    settingUpBootstrap
        .apply(bootstrap)
        .connect(remote.getDescriptor().getAddress(),
                 remote.getPort());

    moduleConnection.add(future);
    return future;
  }

  private UnaryOperator<Bootstrap> createSettingUpBootstrap (Protocol protocol) {
    Class<? extends Channel> clientChannelClass;
    switch (protocol) {
    case TCP:
      clientChannelClass = Epoll.isAvailable()
                           ? EpollSocketChannel.class
                           : NioSocketChannel.class;

      return bootstrap -> bootstrap
          .channel(clientChannelClass)
          .option(SO_KEEPALIVE, true)
          .option(TCP_NODELAY, true);
    case UDP:
      clientChannelClass = Epoll.isAvailable()
                           ? EpollDatagramChannel.class
                           : NioDatagramChannel.class;

      return bootstrap -> bootstrap
          .channel(clientChannelClass);
    case SCTP:
      return bootstrap -> bootstrap
          .channel(NioSctpChannel.class);
    default:
      throw new UnsupportedOperationException("Unsupported protocol: " + protocol);
    }
  }
}
