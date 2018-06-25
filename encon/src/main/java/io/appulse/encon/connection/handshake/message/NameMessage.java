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

import static io.appulse.encon.connection.handshake.message.MessageType.NAME;
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
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class NameMessage extends Message {

  Version distribution;

  Set<DistributionFlag> flags;

  String fullNodeName;

  public NameMessage () {
    super(NAME);
  }

  @Builder
  private NameMessage (@NonNull Version distribution, @Singular Set<DistributionFlag> flags,
                       @NonNull String fullNodeName) {
    this();
    this.distribution = distribution;
    this.flags = flags;
    this.fullNodeName = fullNodeName;
  }

  @Override
  void write (ByteBuf buffer) {
    buffer.writeShort(distribution.getCode());
    buffer.writeInt(DistributionFlag.bitwiseOr(flags));
    buffer.writeCharSequence(fullNodeName, ISO_8859_1);
  }

  @Override
  void read (ByteBuf buffer) {
    distribution = Version.of(buffer.readShort());
    flags = DistributionFlag.parse(buffer.readInt());
    fullNodeName = buffer.readCharSequence(buffer.readableBytes(), ISO_8859_1).toString();
  }
}
