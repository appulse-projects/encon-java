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

package io.appulse.encon.java.module.connection.handshake;

import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.Connection;
import io.appulse.encon.java.module.connection.handshake.exception.HandshakeException;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeAcknowledgeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeReplyMessage;
import io.appulse.encon.java.module.connection.handshake.message.Message;
import io.appulse.encon.java.module.connection.handshake.message.NameMessage;
import io.appulse.encon.java.module.connection.handshake.message.StatusMessage;
import io.appulse.encon.java.module.connection.regular.ClientEncoder;
import io.appulse.encon.java.module.connection.regular.ClientDecoder;
import io.appulse.encon.java.module.connection.regular.ClientRegularHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@Builder
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ClientHandshakeHandler extends ChannelInboundHandlerAdapter {

  private static final ChannelOutboundHandler CLIENT_ENCODER;

  static {
    CLIENT_ENCODER = new ClientEncoder();
  }

  @NonNull
  NodeInternalApi internal;

  @NonNull
  RemoteNode remote;

  @NonNull
  CompletableFuture<Connection> future;

  @NonFinal
  int myChallenge;

  @Override
  public void channelActive (ChannelHandlerContext context) throws Exception {
    super.channelActive(context);
    context.writeAndFlush(NameMessage.builder()
        .fullNodeName(internal.node().getDescriptor().getFullName())
        .distribution(HandshakeUtils.findHighestCommonVerion(internal.node(), remote))
        .flags(internal.node().getMeta().getFlags())
        .build());
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    val message = (Message) obj;
    log.debug("Received message: {}", message);

    switch (message.getType()) {
    case STATUS:
      handle((StatusMessage) message, context);
      break;
    case CHALLENGE:
      handle((ChallengeMessage) message, context);
      break;
    case CHALLENGE_ACKNOWLEDGE:
      handle((ChallengeAcknowledgeMessage) message, context);
      break;
    default:
      log.error("Unexpected message type: {}", message.getType());
      throw new RuntimeException("Unexpected message type: " + message.getType());
    }
  }

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    val message = String.format("Error during channel connection with %s",
                                context.channel().remoteAddress().toString());

    log.error(message, cause);
    context.close();
  }

  private void handle (StatusMessage message, ChannelHandlerContext context) {
    val status = message.getStatus();
    switch (status) {
    case OK:
      break;
    case OK_SIMULTANEOUS:
    case NOK:
    case NOT_ALLOWED:
    case ALIVE:
    default:
      log.error("Invalid received status: {}", status);
      throw new HandshakeException("Invalid received status: " + status);
    }
  }

  private void handle (ChallengeMessage message, ChannelHandlerContext context) {
    val remoteChallenge = message.getChallenge();
    val digest = HandshakeUtils.generateDigest(remoteChallenge, internal.node().getCookie());
    myChallenge = ThreadLocalRandom.current().nextInt();
    context.writeAndFlush(ChallengeReplyMessage.builder()
        .challenge(myChallenge)
        .digest(digest)
        .build());
  }

  private void handle (ChallengeAcknowledgeMessage message, ChannelHandlerContext context) {
    val peerDigest = message.getDigest();
    val myDigest = HandshakeUtils.generateDigest(myChallenge, internal.node().getCookie());
    if (!Arrays.equals(peerDigest, myDigest)) {
      log.error("Remote and own digest are not equal");
      throw new HandshakeException("Remote and own digest are not equal");
    }
    log.debug("Sucessfull handshake from {} to {}",
              internal.node().getDescriptor().getFullName(),
              remote.getDescriptor().getFullName());

    log.debug("Replacing pipline to regular for {}", context.channel().remoteAddress());

    context.pipeline().replace("decoder", "decoder", new ClientDecoder());
    context.pipeline().replace("encoder", "encoder", CLIENT_ENCODER);
    val handler = new ClientRegularHandler(internal);
    context.pipeline().replace("handler", "handler", handler);

    val connection = new Connection(remote, handler);
    future.complete(connection);
  }
}
