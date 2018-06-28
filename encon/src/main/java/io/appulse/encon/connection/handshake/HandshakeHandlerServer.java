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

package io.appulse.encon.connection.handshake;

import static io.appulse.encon.connection.handshake.message.StatusMessage.Status.OK;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.Connection;
import io.appulse.encon.connection.handshake.exception.HandshakeException;
import io.appulse.encon.connection.handshake.message.ChallengeAcknowledgeMessage;
import io.appulse.encon.connection.handshake.message.ChallengeMessage;
import io.appulse.encon.connection.handshake.message.ChallengeReplyMessage;
import io.appulse.encon.connection.handshake.message.Message;
import io.appulse.encon.connection.handshake.message.NameMessage;
import io.appulse.encon.connection.handshake.message.StatusMessage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
class HandshakeHandlerServer extends AbstractHandshakeHandler {

  @NonFinal
  int ourChallenge;

  @Builder
  HandshakeHandlerServer (Node node, CompletableFuture<Connection> future, Consumer<RemoteNode> channelCloseAction) {
    super(node, future, channelCloseAction);
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    val message = (Message) obj;
    log.debug("Received message\nfrom {}\n  {}\n",
              context.channel().remoteAddress(), message);

    switch (message.getType()) {
    case NAME:
      handle((NameMessage) message, context);
      break;
    case CHALLENGE_REPLY:
      handle((ChallengeReplyMessage) message, context);
      break;
    default:
      log.error("Unexpected message type: {}", message.getType());
      throw new IllegalArgumentException("Unexpected message type: " + message.getType());
    }
  }

  private void handle (NameMessage message, ChannelHandlerContext context) {
    remote = node.lookup(message.getFullNodeName());
    if (remote == null) {
      throw new HandshakeException();
    }

    val statusMessage = StatusMessage.builder()
        .status(OK)
        .build();
    context.write(statusMessage);
    log.debug("Sending status message\n  {}\n", statusMessage);

    ourChallenge = ThreadLocalRandom.current().nextInt();
    val challengeMessage = ChallengeMessage.builder()
        .distribution(node.getMeta().getLow())
        .flags(node.getMeta().getFlags())
        .challenge(ourChallenge)
        .fullName(node.getDescriptor().getFullName())
        .build();
    context.writeAndFlush(challengeMessage);
    log.debug("Sending challenge message\n  {}\n", challengeMessage);
  }

  private void handle (ChallengeReplyMessage message, ChannelHandlerContext context) {
    val peerDigest = message.getDigest();
    val myDigest = HandshakeUtils.generateDigest(ourChallenge, node.getCookie());
    if (!Arrays.equals(peerDigest, myDigest)) {
      log.error("Remote and own digest are not equal");
      throw new HandshakeException("Remote and own digest are not equal");
    }

    val remoteChallenge = message.getChallenge();
    val ourDigest = HandshakeUtils.generateDigest(remoteChallenge, node.getCookie());

    val acknowledgeMessage = ChallengeAcknowledgeMessage.builder()
        .digest(ourDigest)
        .build();

    log.debug("Sending challenge acknowledge message\n  {}\n", acknowledgeMessage);
    context.writeAndFlush(acknowledgeMessage)
        .addListener((ChannelFuture channelFuture) -> {
          val channel = channelFuture.channel();
          successHandshake(channel);
        });
  }
}
