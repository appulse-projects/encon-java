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

package io.appulse.encon.java.module.server;

import io.appulse.encon.java.module.connection.handshake.message.Message;
import io.appulse.encon.java.module.connection.handshake.message.NameMessage;
import io.appulse.encon.java.module.connection.handshake.message.StatusMessage;
import io.appulse.encon.java.module.connection.regular.ClientDecoder;
import io.appulse.encon.java.module.connection.regular.ClientEncoder;
import io.appulse.encon.java.module.connection.regular.ClientRegularHandler;

import static io.appulse.encon.java.module.connection.handshake.message.StatusMessage.Status.OK;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.handshake.HandshakeUtils;
import io.appulse.encon.java.module.connection.handshake.exception.HandshakeException;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeAcknowledgeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeReplyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class HandshakeHandler extends ChannelInboundHandlerAdapter {

  private static final ChannelOutboundHandler CLIENT_ENCODER;

  static {
    CLIENT_ENCODER = new ClientEncoder();
  }

  @NonNull
  NodeInternalApi internal;

  @NonFinal
  int ourChallenge;

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    val message = (Message) obj;
    log.debug("Received message: {}", message);

    switch (message.getType()) {
    case NAME:
      handle((NameMessage) message, context);
      break;
    case CHALLENGE_REPLY:
      handle((ChallengeReplyMessage) message, context);
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

  private void handle (NameMessage message, ChannelHandlerContext context) {
    context.write(StatusMessage.builder()
          .status(OK)
          .build());

    ourChallenge = ThreadLocalRandom.current().nextInt();

    context.writeAndFlush(ChallengeMessage.builder()
        .distribution(internal.node().getMeta().getLow())
        .flags(internal.node().getMeta().getFlags())
        .challenge(ourChallenge)
        .fullName(internal.node().getDescriptor().getFullName())
        .build());
  }

  private void handle (ChallengeReplyMessage message, ChannelHandlerContext context) {
    val peerDigest = message.getDigest();
    val myDigest = HandshakeUtils.generateDigest(ourChallenge, internal.node().getCookie());
    if (!Arrays.equals(peerDigest, myDigest)) {
      log.error("Remote and own digest are not equal");
      throw new HandshakeException("Remote and own digest are not equal");
    }
    val remoteChallenge = message.getChallenge();
    val ourDigest = HandshakeUtils.generateDigest(remoteChallenge, internal.node().getCookie());
    context.writeAndFlush(ChallengeAcknowledgeMessage.builder()
        .digest(ourDigest)
        .build());

    context.pipeline().replace("decoder", "decoder", new ClientDecoder());
    context.pipeline().replace("encoder", "encoder", CLIENT_ENCODER);
    val handler = new ClientRegularHandler(internal);
    context.pipeline().replace("handler", "handler", handler);
  }
}
