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
import static java.util.Arrays.copyOfRange;

import io.appulse.utils.BytesUtils;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.ByteArrayInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 09.04.2018
 */
@Slf4j
@Sharable
public class CompressionDecoder extends ChannelInboundHandlerAdapter {

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    val message = String.format("Error during channel connection with %s",
                                context.channel().remoteAddress().toString());

    log.error(message, cause);
    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object msg) throws Exception {
    byte[] bytes = (byte[]) msg;
    if (bytes[0] == COMPRESSED.getCode()) {
      bytes = decompress(bytes);
    }
    context.fireChannelRead(bytes);
  }

  @SneakyThrows
  private byte[] decompress (@NonNull byte[] bytes) {
    val uncompressedSize = BytesUtils.asInteger(copyOfRange(bytes, 1, 5));
    val result = new byte[uncompressedSize];
    val byteArrayInputStream = new ByteArrayInputStream(copyOfRange(bytes, 5, bytes.length));
    val inflaterInputStream = new InflaterInputStream(byteArrayInputStream, new Inflater(), uncompressedSize);

    int cursorPosition = 0;
    while (cursorPosition < uncompressedSize) {
      val readed = inflaterInputStream.read(result, cursorPosition, uncompressedSize - cursorPosition);
      if (readed == -1) {
        break;
      }
      cursorPosition += readed;
    }

    return result;
  }
}
