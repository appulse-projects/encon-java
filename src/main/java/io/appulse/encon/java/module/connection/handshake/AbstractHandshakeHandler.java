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

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.connection.Pipeline;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 20.02.2018
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public abstract class AbstractHandshakeHandler extends ChannelInboundHandlerAdapter {

  @NonNull
  Pipeline pipeline;

  public abstract RemoteNode getRemoteNode ();

  public void successHandshake (@NonNull ChannelHandlerContext context) {
    log.debug("Replacing pipline to regular for {}", context.channel().remoteAddress());
    pipeline.setupPipeline(context.pipeline());
  }

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    val message = String.format("Error during channel connection with %s",
                                context.channel().remoteAddress().toString());

    log.error(message, cause);
    context.close();
  }
}
