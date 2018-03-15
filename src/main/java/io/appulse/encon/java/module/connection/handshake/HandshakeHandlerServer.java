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

package io.appulse.encon.java.module.connection.handshake;

import static io.appulse.encon.java.module.connection.handshake.message.StatusMessage.Status.OK;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.Pipeline;
import io.appulse.encon.java.module.connection.handshake.exception.HandshakeException;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeAcknowledgeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeReplyMessage;
import io.appulse.encon.java.module.connection.handshake.message.Message;
import io.appulse.encon.java.module.connection.handshake.message.NameMessage;
import io.appulse.encon.java.module.connection.handshake.message.StatusMessage;

import io.netty.channel.ChannelHandlerContext;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class HandshakeHandlerServer extends AbstractHandshakeHandler {

  NodeInternalApi internal;

  @NonFinal
  RemoteNode remote;

  @NonFinal
  int ourChallenge;

  public HandshakeHandlerServer (Pipeline pipeline, @NonNull NodeInternalApi internal) {
    super(pipeline);
    this.internal = internal;
  }

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
      throw new IllegalArgumentException("Unexpected message type: " + message.getType());
    }
  }

  @Override
  public RemoteNode getRemoteNode () {
    return remote;
  }

  private void handle (NameMessage message, ChannelHandlerContext context) {
    remote = internal.node()
        .lookup(message.getFullNodeName())
        .orElseThrow(HandshakeException::new);

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

    successHandshake(context);
  }
}
