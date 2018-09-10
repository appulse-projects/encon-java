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

import static io.netty.handler.logging.LogLevel.DEBUG;

import java.util.function.Consumer;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
public final class RegularPipeline {

  private static final ChannelDuplexHandler LOGGING_HANDLER;

  static {
    LOGGING_HANDLER = new LoggingHandler(DEBUG);
  }

  public static ConnectionHandler setup (@NonNull ChannelPipeline pipeline,
                                         @NonNull Node node,
                                         @NonNull RemoteNode remoteNode,
                                         @NonNull Consumer<RemoteNode> channelCloseAction
  ) {
    ConnectionHandler handler = ConnectionHandler.builder()
        .node(node)
        .remote(remoteNode)
        .channelCloseAction(channelCloseAction)
        .build();

    if (log.isDebugEnabled()) {
      pipeline.addLast(LOGGING_HANDLER);
    }
    pipeline.addLast(handler);

    return handler;
  }

  private RegularPipeline () {
  }
}
