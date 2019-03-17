/*
 * Copyright 2019 the original author or authors.
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

package io.appulse.encon.connection.outbound;

import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.handshake.HandshakeUtils;
import io.appulse.encon.connection.handshake.exception.HandshakeException;
import io.appulse.encon.connection.handshake2.HandshakeMessageChallengeAcknowledge;
import io.appulse.encon.connection.handshake2.HandshakeMessageChallengeRequest;
import io.appulse.encon.connection.handshake2.HandshakeMessageChallengeResponse;
import io.appulse.encon.connection.handshake2.HandshakeMessageNameRequest;
import io.appulse.encon.connection.handshake2.HandshakeMessageStatusResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 2.0.0
 * @author Artem Labazin
 */
@Slf4j
@FieldDefaults(level = PRIVATE)
class HandshakeHandler extends ChannelInboundHandlerAdapter {

  final Node node;

  final Consumer<RemoteNode> onSuccess;

  State state = State.SEND_NAME;

  RemoteNode remote;

  int myChallenge;

  @Builder
  private HandshakeHandler (@NonNull Node node, @NonNull Consumer<RemoteNode> onSuccess) {
    this.node = node;
    this.onSuccess = onSuccess;
  }

  @Override
  public void channelActive (ChannelHandlerContext context) throws Exception {
    super.channelActive(context);
    val nameRequest = HandshakeMessageNameRequest.builder()
        .nodeName(node.getDescriptor().getFullName())
        .version(node.getMeta().getLow())
        .flags(node.getMeta().getFlags())
        .build();

    log.debug("{}, sending {}", state, nameRequest);
    context.writeAndFlush(nameRequest);

    state = State.RECEIVE_STATUS;
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    switch (state) {
    case RECEIVE_STATUS:
      val statusResponse = (HandshakeMessageStatusResponse) obj;
      log.debug("'{}', received {}", state, statusResponse);
      handle(statusResponse, context);
      break;
    case RECEIVE_CHALLENGE:
      val challengeRequest = (HandshakeMessageChallengeRequest) obj;
      log.debug("'{}', received {}", state, challengeRequest);
      handle(challengeRequest, context);
      break;
    case RECEIVE_CHALLENGE_ACKNOWLEDGE:
      val challengeAcknowledge = (HandshakeMessageChallengeAcknowledge) obj;
      log.debug("'{}', received {}", state, challengeAcknowledge);
      handle(challengeAcknowledge, context);
      break;
    case DONE:
      log.warn("It is final handshake state, but messages keep coming");
      break;
    default:
      log.error("Unsupported handshake state '{}'", state);
    }
  }

  private void handle (HandshakeMessageStatusResponse statusResponse, ChannelHandlerContext context) {
    switch (statusResponse.getStatus()) {
    case OK:
      state = State.RECEIVE_CHALLENGE;
      return;
    case OK_SIMULTANEOUS:
    case NOK:
    case NOT_ALLOWED:
    case ALIVE:
    default:
      log.error("Unsupported status value {}, close connection",
                statusResponse.getStatus());
      context.close();
    }
  }

  private void handle (HandshakeMessageChallengeRequest challengeRequest, ChannelHandlerContext context) {
    val remoteChallenge = challengeRequest.getChallenge();
    val digest = HandshakeUtils.generateDigest(remoteChallenge, node.getCookie());
    myChallenge = ThreadLocalRandom.current().nextInt();
    val challengeResponse = HandshakeMessageChallengeResponse.builder()
        .challenge(myChallenge)
        .digest(digest)
        .build();

    log.debug("{}, sending {}", state, challengeResponse);
    context.writeAndFlush(challengeResponse);

    state = State.RECEIVE_CHALLENGE_ACKNOWLEDGE;
  }

  private void handle (HandshakeMessageChallengeAcknowledge challengeAcknowledge, ChannelHandlerContext context) {
    val peerDigest = challengeAcknowledge.getDigest();
    val myDigest = HandshakeUtils.generateDigest(myChallenge, node.getCookie());
    if (!Arrays.equals(peerDigest, myDigest)) {
      log.error("Remote and own digest are not equal");
      throw new HandshakeException("Remote and own digest are not equal");
    }

    onSuccess.accept(remote);
    state = State.DONE;
  }

  enum State {

    SEND_NAME,
    RECEIVE_STATUS,
    RECEIVE_CHALLENGE,
    RECEIVE_CHALLENGE_ACKNOWLEDGE,
    DONE;
  }
}
