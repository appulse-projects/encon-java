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

package io.appulse.encon.connection.handshake;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.security.MessageDigest;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.handshake.exception.HandshakeException;
import io.appulse.epmd.java.core.model.Version;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public final class HandshakeUtils {

  public static Version findHighestCommonVerion (@NonNull Node node, @NonNull RemoteNode peer) {
    val meta = node.getMeta();

    if (peer.getProtocol() != meta.getProtocol() ||
        meta.getHigh().getCode() < peer.getLow().getCode() ||
        meta.getLow().getCode() > peer.getHigh().getCode()) {
      throw new HandshakeException("No common protocol found - cannot accept connection");
    }
    return peer.getHigh().getCode() > meta.getHigh().getCode()
           ? meta.getHigh()
           : peer.getHigh();
  }

  @SneakyThrows
  public static byte[] generateDigest (int challenge, @NonNull String cookie) {
    long ch = challenge;

    if (challenge < 0) {
      ch = 1L << 31;
      ch |= challenge & 0x7FFFFFFF;
    }

    val messageDigest = MessageDigest.getInstance("MD5");
    messageDigest.update(cookie.getBytes(ISO_8859_1));
    messageDigest.update(Long.toString(ch).getBytes(ISO_8859_1));

    return messageDigest.digest();
  }

  private HandshakeUtils () {
  }
}
