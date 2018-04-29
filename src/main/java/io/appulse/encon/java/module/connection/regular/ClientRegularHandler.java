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

package io.appulse.encon.java.module.connection.regular;

import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.control.ControlMessage;
import io.appulse.encon.java.module.connection.control.Exit;
import io.appulse.encon.java.module.connection.control.Exit2;
import io.appulse.encon.java.module.connection.control.Link;
import io.appulse.encon.java.module.connection.control.Send;
import io.appulse.encon.java.module.connection.control.SendToRegisteredProcess;
import io.appulse.encon.java.module.connection.control.Unlink;
import io.appulse.encon.java.module.mailbox.Mailbox;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.Closeable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class ClientRegularHandler extends ChannelInboundHandlerAdapter implements Closeable {

  @NonNull
  NodeInternalApi internal;

  @NonNull
  RemoteNode remote;

  @NonFinal
  Channel channel;

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(),
              cause);

    context.fireExceptionCaught(cause);
    context.close();
    close();
  }

  @Override
  public void handlerAdded (ChannelHandlerContext context) throws Exception {
    super.handlerAdded(context);
    channel = context.channel();
    log.debug("Regular handler for channel {} was added with remote node {}",
              channel.remoteAddress(), remote);
  }

  @Override
  public void channelInactive (ChannelHandlerContext context) throws Exception {
    super.channelInactive(context);
    log.debug("Regular handler for channel {} became inactive. Remote is {}",
              channel.remoteAddress(), remote);
    close();
  }

  public void send (Message message) {
    log.debug("Sending message\nto {}\n  {}\n",
              remote, message);
    if (!channel.isWritable()) {
      log.error("Channel for {} is not writable. Remote node is {}",
                channel.remoteAddress(), remote);
      throw new RuntimeException("Channel is not writable");
    }
    channel.writeAndFlush(message);
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object obj) throws Exception {
    val message = (Message) obj;
    log.debug("Received message\nfrom {}\n  {}\n", remote, message);
    ControlMessage header = message.getHeader();

    val mailbox = findMailbox(header);
    if (mailbox != null) {
      mailbox.deliver(message);
    } else {
      log.warn("There is no mailbox for message\n  {}\n", message);
    }
  }

  @Override
  public void close () {
    log.debug("Closing regular handler for channel {} and remote node {}",
              channel.remoteAddress(), remote);

    if (channel.isOpen()) {
      channel.close();
    }
    internal.clearCachesFor(remote);
    log.debug("Client handler for {} was closed", channel.remoteAddress());
  }

  private Mailbox findMailbox (@NonNull ControlMessage header) {
    switch (header.getTag()) {
    case SEND:
      return handle((Send) header);
    case REG_SEND:
      return handle((SendToRegisteredProcess) header);
    case LINK:
      return handle((Link) header);
    case UNLINK:
      return handle((Unlink) header);
    case EXIT:
      return handle((Exit) header);
    case EXIT2:
      return handle((Exit2) header);
    default:
      return null;
    }
  }

  private Mailbox handle (@NonNull Send header) {
    val destination = header.getTo();
    val mailboxes = internal.mailboxes();
    return destination.isAtom()
           ? mailboxes.mailboxUnsafe(destination.asText())
           : mailboxes.mailboxUnsafe(destination.asPid());
  }

  private Mailbox handle (@NonNull SendToRegisteredProcess header) {
    val toPid = header.getTo();
    val mailboxName = toPid.asText();
    return internal.mailboxes()
        .mailboxUnsafe(mailboxName);
  }

  private Mailbox handle (@NonNull Link header) {
    val toPid = header.getTo();
    return internal.mailboxes()
        .mailboxUnsafe(toPid);
  }

  private Mailbox handle (@NonNull Unlink header) {
    val toPid = header.getTo();
    return internal.mailboxes()
        .mailboxUnsafe(toPid);
  }

  private Mailbox handle (@NonNull Exit header) {
    val toPid = header.getTo();
    return internal.mailboxes()
        .mailboxUnsafe(toPid);
  }

  private Mailbox handle (@NonNull Exit2 header) {
    val toPid = header.getTo();
    return internal.mailboxes()
        .mailboxUnsafe(toPid);
  }
}
