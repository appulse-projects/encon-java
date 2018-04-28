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

import static io.appulse.encon.java.protocol.TermType.COMPRESSED;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.utils.Bytes;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 09.04.2018
 */
@Slf4j
@Sharable
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class CompressionEncoder extends ChannelOutboundHandlerAdapter {

  int level;

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  public void write (ChannelHandlerContext context, Object msg, ChannelPromise promise) throws Exception {
    log.debug("Compressing");
    byte[] bytes = (byte[]) msg;
    val compressed = compress(bytes);
    log.debug("Sending message after compression:\n  {}\n", bytes);
    context.write(compressed, promise);
  }

  @SneakyThrows
  private byte[] compress (@NonNull byte[] bytes) {
    val deflater = new Deflater(level);
    val byteArrayOutputStream = new ByteArrayOutputStream();
    try (val deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater)) {
      deflaterOutputStream.write(bytes);
      deflaterOutputStream.flush();
    }
    deflater.end();

    return Bytes.allocate()
        .put1B(COMPRESSED.getCode())
        .put4B(bytes.length)
        .put(byteArrayOutputStream.toByteArray())
        .array();
  }
}
