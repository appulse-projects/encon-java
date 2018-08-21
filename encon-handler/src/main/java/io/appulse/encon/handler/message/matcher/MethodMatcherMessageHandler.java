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

package io.appulse.encon.handler.message.matcher;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.appulse.encon.connection.control.ControlMessage;
import io.appulse.encon.handler.message.MessageHandler;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * {@link MessageHandler} implementation which allows to register methods of different classes as
 * incoming messages handlers.
 *
 * @since 1.4.0
 * @author alabazin
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MethodMatcherMessageHandler implements MessageHandler {

  /**
   * Start building new {@link MethodMatcherMessageHandler} instance.
   *
   * @return {@link MethodMatcherMessageHandlerBuilder} builder
   */
  public static MethodMatcherMessageHandlerBuilder builder () {
    return new MethodMatcherMessageHandlerBuilder();
  }

  @NonNull
  Map<Integer, List<MethodDescriptor>> map;

  @Override
  public void handle (Mailbox self, ControlMessage header, ErlangTerm body) {
    log.debug("new message to {}\nterm: {}", self, body);

    val list = ofNullable(body.size())
        .map(map::get)
        .filter(Objects::nonNull)
        .orElse(null);

    if (list == null) {
      val message = new StringBuilder()
          .append("There is no handler for ").append(body.size()).append(" arguments, only ")
          .append(map.keySet())
          .toString();

      throw new IllegalArgumentException(message);
    }

    val optional = list.stream()
        .filter(it -> it.matches(body))
        .findFirst();

    if (optional.isPresent()) {
      log.debug("handler found");
      optional.get().invoke(body);
    } else {
      log.warn("there is no matched handler for {}", body);
    }
  }
}
