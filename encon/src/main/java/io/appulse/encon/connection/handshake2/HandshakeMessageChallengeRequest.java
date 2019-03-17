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

import static io.appulse.encon.connection.handshake2.HandshakeMessage.Tag.CHALLENGE_REQUEST;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static lombok.AccessLevel.PRIVATE;

import java.util.Set;

import io.appulse.encon.common.DistributionFlag;
import io.appulse.epmd.java.core.model.Version;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
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
public class HandshakeMessageChallengeRequest extends HandshakeMessage {

  String nodeName;

  Version version;

  Set<DistributionFlag> flags;

  int challenge;

  HandshakeMessageChallengeRequest (@NonNull ByteBuf buffer) {
    super(CHALLENGE_REQUEST);
    read(buffer);
  }

  @Builder
  private HandshakeMessageChallengeRequest (@NonNull String nodeName,
                                            @NonNull Version version,
                                            @Singular Set<DistributionFlag> flags,
                                            @NonNull Integer challenge
  ) {
    super(CHALLENGE_REQUEST);

    this.nodeName = nodeName;
    this.version = version;
    this.flags = flags;
    this.challenge = challenge;
  }

  @Override
  void write (@NonNull ByteBuf buffer) {
    buffer
        .writeShort(version.getCode())
        .writeInt(DistributionFlag.bitwiseOr(flags))
        .writeInt(challenge)
        .writeCharSequence(nodeName, ISO_8859_1);
  }

  @Override
  final void read (@NonNull ByteBuf buffer) {
    version = Version.of(buffer.readShort());
    flags = DistributionFlag.parse(buffer.readInt());
    challenge = buffer.readInt();
    nodeName = buffer.readCharSequence(buffer.readableBytes(), ISO_8859_1).toString();
  }
}
