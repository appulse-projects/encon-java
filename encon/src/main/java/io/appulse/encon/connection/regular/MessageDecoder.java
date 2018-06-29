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

import java.util.List;

import io.appulse.encon.connection.control.ControlMessage;
import io.appulse.encon.terms.ErlangTerm;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@Sharable
public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  protected void decode (ChannelHandlerContext context, ByteBuf msg, List<Object> out) throws Exception {
    log.debug("decoding");

    msg.skipBytes(Integer.BYTES); // skip size

    val passThrough = msg.readByte();
    if (passThrough != PASS_THROUGH_TAG) {
      throw new IllegalArgumentException("Wrong pass through marker. Expected 0x70 (112), but was: " + passThrough);
    }

    val header = readTerm(msg);
    val controlMessage = ControlMessage.parse(header);

    ErlangTerm body = null;
    if (msg.readerIndex() < msg.capacity()) {
      body = readTerm(msg);
    }

    out.add(new Message(controlMessage, body));
  }

  private ErlangTerm readTerm (ByteBuf buffer) {
    val versionByte = buffer.readUnsignedByte();
    if (versionByte != VERSION_TAG) {
      throw new IllegalArgumentException("Wrong version byte. Expected 0x83 (131), but was: " + versionByte);
    }
    return ErlangTerm.newInstance(buffer);
  }
}
