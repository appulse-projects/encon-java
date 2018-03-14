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

package io.appulse.encon.java.module.connection;

import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.connection.regular.ClientRegularHandler;
import io.appulse.encon.java.module.connection.regular.Message;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class Connection implements Closeable {

  @NonNull
  @Getter
  RemoteNode remote;

  @NonNull
  ClientRegularHandler handler;

  public void send (@NonNull Message message) {
    handler.send(message);
  }

  @Override
  public void close () {
    log.debug("Closing connection to {}", remote);
    handler.close();
    log.debug("Connection to {} was closed", remote);
  }
}
