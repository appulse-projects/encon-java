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

import io.appulse.encon.java.Node;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.connection.handshake.exception.HandshakeException;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeAcknowledgeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeReplyMessage;
import io.appulse.encon.java.module.connection.handshake.message.Message;
import io.appulse.encon.java.module.connection.handshake.message.NameMessage;
import io.appulse.encon.java.module.connection.handshake.message.StatusMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
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

  @NonNull
  @Singular
  Map<String, ChannelHandler> replaces;

  @NonNull
  Node node;

  @NonNull
  RemoteNode remote;

  @NonFinal
  int myChallenge;

  @Override
  public void channelActive (ChannelHandlerContext context) throws Exception {
    super.channelActive(context);
    val message = NameMessage.builder()
        .fullNodeName(node.getDescriptor().getFullName())
        .distribution(HandshakeUtils.findHighestCommonVerion(node, remote))
        .flags(node.getMeta().getFlags())
        .build();

    context.writeAndFlush(message);
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    Message message = (Message) obj;
    log.debug("Received message: {}", message);

    switch (message.getType()) {
    case STATUS:
      val status = ((StatusMessage) message).getStatus();
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
      break;
    case CHALLENGE:
      val remoteChallenge = ((ChallengeMessage) message).getChallenge();
      val digest = HandshakeUtils.generateDigest(remoteChallenge, node.getCookie());
      myChallenge = ThreadLocalRandom.current().nextInt();
      context.writeAndFlush(ChallengeReplyMessage.builder()
          .challenge(myChallenge)
          .digest(digest)
          .build());
      break;
    case CHALLENGE_ACKNOWLEDGE:
      val peerDigest = ((ChallengeAcknowledgeMessage) message).getDigest();
      val myDigest = HandshakeUtils.generateDigest(myChallenge, node.getCookie());
      if (!Arrays.equals(peerDigest, myDigest)) {
        log.error("Remote and own digest are not equal");
        throw new HandshakeException("Remote and own digest are not equal");
      }
      log.debug("Sucessfull handshake from {} to {}",
                node.getDescriptor().getFullName(),
                remote.getDescriptor().getFullName());

      log.debug("Replacing pipline to regular for {}", context.channel().remoteAddress());

      replaces.entrySet().forEach(it -> {
        context.pipeline().replace(it.getKey(), it.getKey(), it.getValue());
      });
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
}
