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

import io.netty.channel.ChannelHandlerContext;
import lombok.Builder;
import lombok.NonNull;
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
class HandshakeHandlerClient extends AbstractHandshakeHandler {

  @NonFinal
  int myChallenge;

  @Builder
  HandshakeHandlerClient (Node node,
                          CompletableFuture<Connection> future,
                          @NonNull RemoteNode remote,
                          Consumer<RemoteNode> channelCloseAction
  ) {
    super(node, future, channelCloseAction);
    this.remote = remote;
  }

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  public void channelActive (ChannelHandlerContext context) throws Exception {
    super.channelActive(context);
    val nameMessage = NameMessage.builder()
        .fullNodeName(node.getDescriptor().getFullName())
        .distribution(HandshakeUtils.findHighestCommonVerion(node, remote))
        .flags(node.getMeta().getFlags())
        .build();

    log.debug("Sending name message\n  {}\n", nameMessage);
    context.writeAndFlush(nameMessage);
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    val message = (Message) obj;
    log.debug("Received message from {}\n  {}\n",
              context.channel().remoteAddress(), message);

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
      throw new IllegalArgumentException("Unexpected message type: " + message.getType());
    }
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
      log.error("Invalid received status {} in context {}", status, context);
      throw new HandshakeException("Invalid received status: " + status);
    }
  }

  private void handle (ChallengeMessage message, ChannelHandlerContext context) {
    val remoteChallenge = message.getChallenge();
    val digest = HandshakeUtils.generateDigest(remoteChallenge, node.getCookie());
    myChallenge = ThreadLocalRandom.current().nextInt();

    val replyMessage = ChallengeReplyMessage.builder()
        .challenge(myChallenge)
        .digest(digest)
        .build();

    log.debug("Sending challenge reply message\n  {}\n", replyMessage);
    context.writeAndFlush(replyMessage);
  }

  private void handle (ChallengeAcknowledgeMessage message, ChannelHandlerContext context) {
    val peerDigest = message.getDigest();
    val myDigest = HandshakeUtils.generateDigest(myChallenge, node.getCookie());
    if (!Arrays.equals(peerDigest, myDigest)) {
      log.error("Remote and own digest are not equal");
      throw new HandshakeException("Remote and own digest are not equal");
    }
    log.debug("Sucessfull handshake from {} to {}",
              node.getDescriptor().getFullName(),
              remote);

    successHandshake(context.channel());
  }
}
