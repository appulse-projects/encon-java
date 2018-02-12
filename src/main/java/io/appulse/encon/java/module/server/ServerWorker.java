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

package io.appulse.encon.java.module.server;

import static lombok.AccessLevel.PRIVATE;

import java.net.Socket;

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.utils.SocketUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ServerWorker implements Runnable {

  @NonNull
  Socket socket;

  @NonNull
  NodeInternalApi internal;

  @Override
  public void run () {
    log.debug("Start server worker");

    val length = SocketUtils.readBytes(socket, Integer.BYTES).getInt();
    log.debug("Incoming message length is: {}", length);

    val body = SocketUtils.read(socket, length);
    log.debug("Readed message body ({} bytes)", body.length);
  }
}
