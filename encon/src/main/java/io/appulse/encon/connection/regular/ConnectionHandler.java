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

package io.appulse.encon.connection.regular;

import static io.appulse.encon.connection.regular.Message.PASS_THROUGH_TAG;
import static io.appulse.encon.connection.regular.Message.VERSION_TAG;
import static lombok.AccessLevel.PRIVATE;
// import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
// import static io.netty.util.internal.StringUtil.NEWLINE;

import java.io.Closeable;
import java.util.List;
import java.util.function.Consumer;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.control.ControlMessage;
import io.appulse.encon.connection.control.Exit;
import io.appulse.encon.connection.control.Exit2;
import io.appulse.encon.connection.control.Link;
import io.appulse.encon.connection.control.Send;
import io.appulse.encon.connection.control.SendToRegisteredProcess;
import io.appulse.encon.connection.control.Unlink;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
@Slf4j
@Builder
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class ConnectionHandler extends ByteToMessageDecoder implements Closeable {

  private static final ByteBuf TICK_TOCK = Unpooled.wrappedBuffer(new byte[] { 0, 0, 0, 0 });

  private static ErlangTerm readTerm (ByteBuf buffer) {
    val versionByte = buffer.readUnsignedByte();
    if (versionByte != VERSION_TAG) {
      throw new IllegalArgumentException("Wrong version byte. Expected 0x83 (131), but was: " + versionByte);
    }
    return ErlangTerm.newInstance(buffer);
  }

  // private static String formatByteBuf (ChannelHandlerContext ctx, String eventName, ByteBuf msg) {
  //   String chStr = ctx.channel().toString();
  //   int length = msg.readableBytes();
  //   if (length == 0) {
  //       StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 4);
  //       buf.append(chStr).append(' ').append(eventName).append(": 0B");
  //       return buf.toString();
  //   } else {
  //       int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
  //       StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + 10 + 1 + 2 + rows * 80);

  //       buf.append(chStr).append(' ').append(eventName).append(": ").append(length).append('B').append(NEWLINE);
  //       appendPrettyHexDump(buf, msg);

  //       return buf.toString();
  //   }
  // }

  @NonNull
  Node node;

  @Getter
  @NonNull
  RemoteNode remote;

  @NonNull
  Consumer<RemoteNode> channelCloseAction;

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

    val out = channel.alloc().buffer();
    message.writeTo(out);

    val messageLength = channel.alloc()
        .buffer(Integer.BYTES)
        .writeInt(out.readableBytes());

    val popa = channel.alloc().compositeBuffer(2)
        .addComponents(true, messageLength, out);

    channel.writeAndFlush(popa);
  }

  @Override
  protected void decode (ChannelHandlerContext context, ByteBuf buffer, List<Object> out) {
    if (!buffer.isReadable(4)) {
      // log.debug("not enough bytes #1: {}", buffer.readableBytes());
      return;
    }
    int index = buffer.readerIndex();

    int length = buffer.readInt();
    // log.debug("message length is: {}", length);
    if (length == 0) {
      // log.debug("TICK-TOCK message detected, sending response");
      TICK_TOCK.retain();
      context.writeAndFlush(TICK_TOCK.duplicate());
      if (buffer.isReadable()) {
        // log.debug("There is no more bytes in message, stop pipelining");
        return;
      }

      if (!buffer.isReadable(Integer.BYTES)) {
        // log.debug("not enough bytes #2: {}", buffer.readableBytes());
        return;
      }
      index = buffer.readerIndex();
      length = buffer.readInt();
      // log.debug("new message length is: {}", length);
    }

    if (!buffer.isReadable(length)) {
      buffer.readerIndex(index);
      // log.debug("not enough bytes #3: {} vs {}", buffer.readableBytes(), length + 4);
      return;
    }

    // MessageDecoder
    val passThrough = buffer.readByte();
    if (passThrough != PASS_THROUGH_TAG) {
      buffer.readerIndex(index);
      // log.error("\n{}", formatByteBuf(context, "POPA", buffer));
      throw new IllegalArgumentException("Wrong pass through marker. Expected 0x70 (112), but was: " + passThrough +
                                        " at index: " + buffer.readerIndex());
    }

    val header = readTerm(buffer);
    ControlMessage controlMessage = ControlMessage.parse(header);

    ErlangTerm body = null;
    if (buffer.isReadable()) {
      body = readTerm(buffer);
    }

    // ConnectionHandler
    val message = new Message(controlMessage, body);
    log.debug("Received message\nfrom {}\n  {}\n", remote, message);

    val mailbox = findMailbox(controlMessage);
    if (mailbox == null) {
      log.warn("There is no mailbox for message\n  {}\n  {}", message, node.mailboxes().keySet());
    } else {
      mailbox.deliver(message);
    }
  }

  @Override
  public void close () {
    log.debug("Closing regular handler for channel {} and remote node {}",
              channel.remoteAddress(), remote);

    if (channel.isOpen()) {
      channel.close();
    }
    channelCloseAction.accept(remote);

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
    return destination.isAtom()
           ? node.mailbox(destination.asText())
           : node.mailbox(destination.asPid());
  }

  private Mailbox handle (@NonNull SendToRegisteredProcess header) {
    val atom = header.getTo();
    val mailboxName = atom.asText();
    return node.mailbox(mailboxName);
  }

  private Mailbox handle (@NonNull Link header) {
    val toPid = header.getTo();
    return node.mailbox(toPid);
  }

  private Mailbox handle (@NonNull Unlink header) {
    val toPid = header.getTo();
    return node.mailbox(toPid);
  }

  private Mailbox handle (@NonNull Exit header) {
    val toPid = header.getTo();
    return node.mailbox(toPid);
  }

  private Mailbox handle (@NonNull Exit2 header) {
    val toPid = header.getTo();
    return node.mailbox(toPid);
  }
}
