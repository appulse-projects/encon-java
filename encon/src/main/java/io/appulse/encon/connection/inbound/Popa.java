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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.handshake.HandshakeUtils;
import io.appulse.encon.connection.handshake.exception.HandshakeException;
import io.appulse.encon.connection.handshake2.HandshakeMessage;
import io.appulse.encon.connection.handshake2.HandshakeMessageChallengeAcknowledge;
import io.appulse.encon.connection.handshake2.HandshakeMessageChallengeRequest;
import io.appulse.encon.connection.handshake2.HandshakeMessageChallengeResponse;
import io.appulse.encon.connection.handshake2.HandshakeMessageNameRequest;
import io.appulse.encon.connection.handshake2.HandshakeMessageStatusResponse;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
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
class Popa extends ByteToMessageDecoder implements AutoCloseable {

  private static final int HANDSHAKE_MESSAGE_LENGTH_FIELD_BYTES = 2;

  final Node node;

  final Consumer<RemoteNode> onSuccess;

  Channel channel;

  State state = State.RECEIVE_NAME;

  RemoteNode remote;

  int ourChallenge;

  @Builder
  private Popa (@NonNull Node node, @NonNull Consumer<RemoteNode> onSuccess) {
    this.node = node;
    this.onSuccess = onSuccess;
  }

  @Override
  public void handlerAdded (ChannelHandlerContext context) throws Exception {
    super.handlerAdded(context);
    channel = context.channel();
    log.debug("Regular handler for channel '{}' was added", channel.remoteAddress());
  }

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with '{}'",
              context.channel().remoteAddress(),
              cause);

    context.fireExceptionCaught(cause);
    context.close();
    close();
  }

  @Override
  public void channelInactive (ChannelHandlerContext context) throws Exception {
    super.channelInactive(context);
    log.debug("Regular handler for channel '{}' became inactive. Remote is '{}'",
              channel.remoteAddress(), remote);
    close();
  }

  @Override
  public void close () {
    log.debug("Closing regular handler for channel '{}' and remote node '{}'",
              channel.remoteAddress(), remote);

    if (channel.isOpen()) {
      channel.close();
    }
    log.debug("Client handler for '{}' was closed", channel.remoteAddress());
  }

  void initChannel (SocketChannel socketChannel) throws Exception {
    socketChannel.pipeline()
        .addLast("READ_TIMEOUT", new ReadTimeoutHandler(5))
        .addLast("HANDLER", this);
  }

  @Override
  protected void decode (ChannelHandlerContext context, ByteBuf buffer, List<Object> out) {
    if (!buffer.isReadable(HANDSHAKE_MESSAGE_LENGTH_FIELD_BYTES)) {
      return;
    }
    val index = buffer.readerIndex();

    val length = buffer.readShort();
    if (!buffer.isReadable(length)) {
      buffer.readerIndex(index);
      return;
    }

    switch (state) {
    case RECEIVE_NAME:
      HandshakeMessageNameRequest nameRequest = HandshakeMessage.from(buffer, false);
      log.debug("'{}', received {}", state, nameRequest);
      handle(nameRequest, context);
      break;
    case SEND_CHALLENGE_ACKNOWLEDGE:
      HandshakeMessageChallengeResponse challengeResponse = HandshakeMessage.from(buffer, false);
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

  private void handle (HandshakeMessageNameRequest nameRequest, ChannelHandlerContext context) {
    remote = node.lookup(nameRequest.getNodeName());
    if (remote == null) {
      throw new HandshakeException();
    }

    log.debug("'{}', sending OK status", state);
    context.write(HandshakeMessageStatusResponse.OK.toByteBuf());

    ourChallenge = ThreadLocalRandom.current().nextInt();
    val challengeRequest = HandshakeMessageChallengeRequest.builder()
        .version(node.getMeta().getLow())
        .flags(node.getMeta().getFlags())
        .challenge(ourChallenge)
        .nodeName(node.getDescriptor().getFullName())
        .build();

    log.debug("'{}', sending {}", state, challengeRequest);
    context.writeAndFlush(challengeRequest.toByteBuf());

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
    context.writeAndFlush(challengeAcknowledge.toByteBuf()).addListener(future -> {
      if (future.isSuccess()) {
        onSuccess.accept(remote);
      }
    });

    state = State.DONE;
  }

  enum State {

    RECEIVE_NAME,
    SEND_CHALLENGE_ACKNOWLEDGE,
    DONE;
  }
}
