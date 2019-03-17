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
import static io.appulse.encon.connection.handshake2.HandshakeMessage.Tag.CHALLENGE_RESPONSE;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
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
public class HandshakeMessageChallengeResponse extends HandshakeMessage {

  int challenge;

  byte[] digest;

  HandshakeMessageChallengeResponse (@NonNull ByteBuf buffer) {
    super(CHALLENGE_RESPONSE);
    read(buffer);
  }

  @Builder
  private HandshakeMessageChallengeResponse (@NonNull Integer challenge,
                                             @NonNull byte[] digest
  ) {
    super(CHALLENGE_RESPONSE);

    this.challenge = challenge;
    this.digest = digest;
  }

  @Override
  void write (@NonNull ByteBuf buffer) {
    buffer.writeInt(challenge);
    buffer.writeBytes(digest);
  }

  @Override
  final void read (@NonNull ByteBuf buffer) {
    challenge = buffer.readInt();
    digest = readAllRestBytes(buffer);
  }
}
