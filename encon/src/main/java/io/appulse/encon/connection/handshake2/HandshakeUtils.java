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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 *
 * @since 2.0.0
 * @author Artem Labazin
 */
public final class HandshakeUtils {

  @SneakyThrows
  public static byte[] generateDigest (int challenge, @NonNull String cookie) {
    long ch = challenge;

    if (challenge < 0) {
      ch = 1L << 31;
      ch |= challenge & 0x7FFFFFFF;
    }

    val messageDigest = MessageDigest.getInstance("MD5");
    messageDigest.update(cookie.getBytes(UTF_8));
    messageDigest.update(Long.toString(ch).getBytes(UTF_8));

    return messageDigest.digest();
  }

  private HandshakeUtils () {
    throw new UnsupportedOperationException();
  }
}
