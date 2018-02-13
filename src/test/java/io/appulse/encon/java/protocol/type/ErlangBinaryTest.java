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

package io.appulse.encon.java.protocol.type;

import static org.assertj.core.api.Assertions.assertThat;
import static io.appulse.encon.java.protocol.TermType.BINARY;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import lombok.val;

public class ErlangBinaryTest {

  @Test
  public void instantiate () {
    val value = new byte[] { 1, 2, 3 };

    assertThat(new ErlangBinary(value).asBinary())
        .isEqualTo(value);
  }

  @Test
  public void newInstance () {
    val value = new byte[] { 1, 2, 3 };

    val bytes = Bytes.allocate()
        .put1B(BINARY.getCode())
        .put4B(value.length)
        .put(value)
        .array();

    ErlangBinary binary = ErlangTerm.newInstance(bytes);
    assertThat(binary).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(binary.isBinary())
          .isTrue();

      softly.assertThat(binary.asBinary())
          .isEqualTo(value);
    });
  }

  @Test
  public void toBytes () {
    val value = new byte[] { 1, 2, 3 };

    val expected = Bytes.allocate()
        .put1B(BINARY.getCode())
        .put4B(value.length)
        .put(value)
        .array();

    assertThat(new ErlangBinary(value).toBytes())
        .isEqualTo(expected);
  }
}
