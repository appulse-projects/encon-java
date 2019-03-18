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

package io.appulse.encon.connection.inbound;

import static lombok.AccessLevel.PRIVATE;
import static java.util.concurrent.TimeUnit.SECONDS;

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
import lombok.SneakyThrows;
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

  State state = State.RECEIVE_NAME;

  RemoteNode remote;

  int ourChallenge;

  @Builder
  private HandshakeHandler (@NonNull Node node, @NonNull Consumer<RemoteNode> onSuccess) {
    this.node = node;
    this.onSuccess = onSuccess;
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    switch (state) {
    case RECEIVE_NAME:
      val nameRequest = (HandshakeMessageNameRequest) obj;
      log.debug("'{}', received {}", state, nameRequest);
      handle(nameRequest, context);
      break;
    case SEND_CHALLENGE_ACKNOWLEDGE:
      val challengeResponse = (HandshakeMessageChallengeResponse) obj;
      log.debug("'{}', received {}", state, challengeResponse);
      handle(challengeResponse, context);
      break;
    case DONE:
      log.warn("It is final handshake state, but messages keep coming");
      break;
    default:
      log.error("Unsupported handshake state ", state);
    }
  }

  @SneakyThrows
  private void handle (HandshakeMessageNameRequest nameRequest, ChannelHandlerContext context) {
    remote = node.discovery()
        .lookup(nameRequest.getNodeName())
        .get(2, SECONDS)
        .orElseThrow(HandshakeException::new);

    log.debug("'{}', sending OK status", state);
    context.write(HandshakeMessageStatusResponse.OK);

    ourChallenge = ThreadLocalRandom.current().nextInt();
    val challengeRequest = HandshakeMessageChallengeRequest.builder()
        .version(node.getMeta().getLow())
        .flags(node.getMeta().getFlags())
        .challenge(ourChallenge)
        .nodeName(node.getDescriptor().getFullName())
        .build();

    log.debug("'{}', sending {}", state, challengeRequest);
    context.writeAndFlush(challengeRequest);

    state = State.SEND_CHALLENGE_ACKNOWLEDGE;
  }

  private void handle (HandshakeMessageChallengeResponse challengeResponse, ChannelHandlerContext context) {
    val peerDigest = challengeResponse.getDigest();
    val myDigest = HandshakeUtils.generateDigest(ourChallenge, node.getCookie());
    if (!Arrays.equals(peerDigest, myDigest)) {
      log.error("Remote and own digest are not equal");
      throw new HandshakeException("Remote and own digest are not equal");
    }

    val remoteChallenge = challengeResponse.getChallenge();
    val ourDigest = HandshakeUtils.generateDigest(remoteChallenge, node.getCookie());

    val challengeAcknowledge = new HandshakeMessageChallengeAcknowledge(ourDigest);
    log.debug("'{}', sending {}", state, challengeAcknowledge);
    context.writeAndFlush(challengeAcknowledge).addListener(future -> {
      if (!future.isSuccess()) {
        log.error("Error during handshake ending", future.cause());
        return;
      }

      context.channel().config().setAutoRead(false);
      HandshakePipeline.clean(context.pipeline());
      onSuccess.accept(remote);
      context.channel().config().setAutoRead(true);
    });

    state = State.DONE;
  }

  enum State {

    RECEIVE_NAME,
    SEND_CHALLENGE_ACKNOWLEDGE,
    DONE;
  }
}
