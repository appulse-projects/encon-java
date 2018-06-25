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

import static io.appulse.encon.connection.handshake.message.MessageType.CHALLENGE_REPLY;
import static lombok.AccessLevel.PRIVATE;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ChallengeReplyMessage extends Message {

  int challenge;

  byte[] digest;

  public ChallengeReplyMessage () {
    super(CHALLENGE_REPLY);
  }

  @Builder
  private ChallengeReplyMessage (int challenge, @NonNull byte[] digest) {
    this();
    this.challenge = challenge;
    this.digest = digest.clone();
  }

  @Override
  void write (ByteBuf buffer) {
    buffer.writeInt(challenge);
    buffer.writeBytes(digest);
  }

  @Override
  void read (ByteBuf buffer) {
    challenge = buffer.readInt();
    digest = readAllRestBytes(buffer);
  }
}
