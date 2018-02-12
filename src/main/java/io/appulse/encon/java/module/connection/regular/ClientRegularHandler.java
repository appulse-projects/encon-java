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

package io.appulse.encon.java.module.connection.regular;

import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.module.connection.control.Send;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ClientRegularHandler extends ChannelInboundHandlerAdapter {

  @NonNull
  NodeInternalApi internal;

  @NonFinal
  Channel channel;

  @Override
  public void handlerAdded (ChannelHandlerContext context) throws Exception {
    log.debug("Handler added");
    channel = context.channel();
  }

  public boolean wasAdded () {
    return channel != null;
  }

  public void send (Container container) {
    channel.writeAndFlush(container);
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    val container = (Container) obj;
    log.debug("Received message: {}", container);
    val controlMessage = container.getControlMessage();

    switch (controlMessage.getTag()) {
    case SEND:
      val destination = ((Send) controlMessage).getTo();
      val mailboxes = internal.mailboxes();
      Optional<Mailbox> optional;
      if (destination.isAtom()) {
        optional = mailboxes.getMailbox(destination.asText());
      } else {
        optional = mailboxes.getMailbox(destination.asPid());
      }
      optional.ifPresent(it -> it.inbox(container.getPayload()));
      break;
    default:
      throw new RuntimeException();
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
