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

package io.appulse.encon.java.config;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.Map;

import io.appulse.encon.java.module.mailbox.MailboxHandler;
import io.appulse.encon.java.module.mailbox.MailboxType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 22.02.2018
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class MailboxConfig {

  static MailboxConfig newInstance (@NonNull Map<String, Object> map) {
    MailboxConfigBuilder builder = MailboxConfig.builder();

    ofNullable(map.get("name"))
        .map(Object::toString)
        .ifPresent(builder::name);

    ofNullable(map.get("type"))
        .map(Object::toString)
        .map(MailboxType::valueOf)
        .ifPresent(builder::receiverType);

    ofNullable(map.get("handler"))
        .map(Object::toString)
        .map(it -> {
          try {
            return Class.forName(it);
          } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
          }
        })
        .map(it -> it.asSubclass(MailboxHandler.class))
        .ifPresent(builder::handler);

    return builder.build();
  }

  String name;

  MailboxType receiverType;

  Class<? extends MailboxHandler> handler;

  MailboxConfig initDefaults (@NonNull MailboxConfig defaults) {
    receiverType = ofNullable(receiverType)
        .orElse(defaults.getReceiverType());

    if (handler == null) {
      handler = defaults.getHandler();
    }
    return this;
  }
}
