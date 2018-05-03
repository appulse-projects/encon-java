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

package io.appulse.encon.config;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.config.exception.NoSuchMailboxHandlerException;
import io.appulse.encon.module.mailbox.MailboxHandler;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
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

    ofNullable(map.get("handler"))
        .map(Object::toString)
        .map(it -> {
          try {
            return Class.forName(it);
          } catch (ClassNotFoundException ex) {
            throw new NoSuchMailboxHandlerException(ex, it);
          }
        })
        .map(it -> it.asSubclass(MailboxHandler.class))
        .ifPresent(builder::handler);

    return builder.build();
  }

  String name;

  Class<? extends MailboxHandler> handler;

  /**
   * Method for setting up default values.
   *
   * @param defaults MailboxConfig with default values for node
   *
   * @return reference to this object (for chain calls)
   */
  public MailboxConfig withDefaultsFrom (@NonNull MailboxConfig defaults) {
    if (handler == null) {
      handler = defaults.getHandler();
    }
    return this;
  }
}
