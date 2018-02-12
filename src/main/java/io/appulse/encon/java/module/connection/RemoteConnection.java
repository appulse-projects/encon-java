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

package io.appulse.encon.java.module.connection;

import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.Node;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.handshake.ClientHandshakeDecoder;
import io.appulse.encon.java.module.connection.handshake.ClientHandshakeEncoder;
import io.appulse.encon.java.module.connection.handshake.ClientHandshakeHandler;
import io.appulse.encon.java.module.connection.regular.ClientRegularDecoder;
import io.appulse.encon.java.module.connection.regular.ClientRegularEncoder;
import io.appulse.encon.java.module.connection.regular.ClientRegularHandler;
import io.appulse.encon.java.module.connection.regular.Container;
import io.appulse.encon.java.protocol.control.ControlMessage;
import io.appulse.encon.java.protocol.control.RegistrationSendControlMessage;
import io.appulse.encon.java.protocol.control.SendControlMessage;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.Atom;
import io.appulse.encon.java.protocol.type.Pid;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RemoteConnection implements Closeable {

  private static final ChannelOutboundHandler HANDSHAKE_ENCODER;

  private static final ChannelOutboundHandler REGULAR_ENCODER;

  static {
    HANDSHAKE_ENCODER = new ClientHandshakeEncoder();

    REGULAR_ENCODER = new ClientRegularEncoder();
  }

  @NonNull
  Node node;

  @NonNull
  RemoteNode remote;

  @NonNull
  EventLoopGroup workerGroup;

  @NonFinal
  Bootstrap bootstrap;

  @NonFinal
  ClientRegularHandler handler;

  public void start (@NonNull NodeInternalApi internal) {
    handler = new ClientRegularHandler(internal);
    bootstrap = new Bootstrap()
        .group(workerGroup)
        .channel(NioSocketChannel.class)
        .option(SO_KEEPALIVE, true)
        .handler(new ChannelInitializer<SocketChannel>() {

          @Override
          public void initChannel (SocketChannel channel) throws Exception {
            channel.pipeline()
                .addLast("decoder", new ClientHandshakeDecoder())
                .addLast("encoder", HANDSHAKE_ENCODER)
                .addLast("handler", ClientHandshakeHandler.builder()
                         .node(node)
                         .remote(remote)
                         .replace("decoder", new ClientRegularDecoder())
                         .replace("encoder", REGULAR_ENCODER)
                         .replace("handler", handler)
                         .build());
          }
        });

    bootstrap.connect(remote.getDescriptor().getAddress(), remote.getPort());
  }

  public void sendReg (@NonNull Pid from, @NonNull String mailbox, @NonNull ErlangTerm message) {
    sendReg(from, new Atom(mailbox), message);
  }

  public void sendReg (@NonNull Pid from, @NonNull Atom mailbox, @NonNull ErlangTerm message) {
    send(new RegistrationSendControlMessage(from, mailbox), message);
  }

  public void send (@NonNull Pid to, @NonNull ErlangTerm message) {
    send(new SendControlMessage(to), message);
  }

  @SneakyThrows
  private void send (ControlMessage control, ErlangTerm payload) {
    while (!handler.wasAdded()) {
      log.debug("Waiting {} connection to send message", remote);
      TimeUnit.SECONDS.sleep(1);
    }

    // if (handler.wasAdded()) {
      handler.send(new Container(control, payload));
    // } else {
      // log.warn("Handler was not added");
    // }
  }

  @Override
  public void close () {
    workerGroup.shutdownGracefully();
  }
}
