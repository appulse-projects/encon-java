/*
 * Copyright 2019 the original author or authors.
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

package io.appulse.encon.connection.handshake2;

import static lombok.AccessLevel.PRIVATE;
import static io.appulse.encon.connection.handshake2.HandshakeMessage.Tag.CHALLENGE_ACKNOWLEDGE;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 2.0.0
 * @author Artem Labazin
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class HandshakeMessageChallengeAcknowledge extends HandshakeMessage {

  byte[] digest;

  HandshakeMessageChallengeAcknowledge (@NonNull ByteBuf buffer) {
    super(CHALLENGE_ACKNOWLEDGE);
    read(buffer);
  }

  public HandshakeMessageChallengeAcknowledge (@NonNull byte[] digest) {
    super(CHALLENGE_ACKNOWLEDGE);
    this.digest = digest;
  }

  @Override
  void write (@NonNull ByteBuf buffer) {
    buffer.writeBytes(digest);
  }

  @Override
  final void read (@NonNull ByteBuf buffer) {
    digest = readAllRestBytes(buffer);
  }
}
