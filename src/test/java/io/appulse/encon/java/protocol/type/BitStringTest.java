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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static io.appulse.encon.java.protocol.TermType.BIT_BINNARY;

import io.appulse.encon.java.protocol.exception.ErlangTermValidationException;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import lombok.val;

public class BitStringTest {

  @Test
  public void erlangTermValidationException () {
    assertThatThrownBy(() -> new BitString(new byte[] { 1 }, -1))
        .isInstanceOf(ErlangTermValidationException.class)
        .hasMessage("Padding must be in range 0..7");

    assertThatThrownBy(() -> new BitString(new byte[0], 1))
        .isInstanceOf(ErlangTermValidationException.class)
        .hasMessage("Padding on zero length BitString");
  }

  @Test
  public void newInstance () {
    val value = new byte[] { 1, 2, 3 };
    val pad = 3;

    val bytes = Bytes.allocate()
        .put1B(BIT_BINNARY.getCode())
        .put4B(value.length)
        .put1B(8 - pad)
        .put(value)
        .array();

    BitString bitString = ErlangTerm.newInstance(bytes);
    assertThat(bitString).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(bitString.getBits())
          .isEqualTo(new byte[] { 1, 2, 0 });

      softly.assertThat(bitString.getPad())
          .isEqualTo(3);
    });
  }

  @Test
  public void toBytes () {
    val bytes = new byte[] { 1, 2, 3 };
    val pad = 3;

    val expected = Bytes.allocate()
        .put1B(BIT_BINNARY.getCode())
        .put4B(bytes.length)
        .put1B(8 - pad)
        .put(new byte[] { 1, 2, 0 })
        .array();

    assertThat(new BitString(bytes, pad).toBytes())
        .isEqualTo(expected);
  }
}
