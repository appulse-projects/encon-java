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

package io.appulse.encon.connection.handshake.message;

import java.util.stream.Stream;

import lombok.Getter;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
public enum MessageType {

  NAME('n', NameMessage.class),
  STATUS('s', StatusMessage.class),
  CHALLENGE('n', ChallengeMessage.class),
  CHALLENGE_REPLY('r', ChallengeReplyMessage.class),
  CHALLENGE_ACKNOWLEDGE('a', ChallengeAcknowledgeMessage.class),
  UNDEFINED('0', Message.class);

  private final byte tag;

  private final Class<? extends Message> type;

  MessageType (char tag, Class<? extends Message> type) {
    this.tag = (byte) tag;
    this.type = type;
  }

  public static boolean check (byte tag, Class<? extends Message> type) {
    return Stream.of(values())
        .filter(it -> it.getTag() == tag)
        .filter(it -> it.getType() == type)
        .findAny()
        .isPresent();
  }
}
