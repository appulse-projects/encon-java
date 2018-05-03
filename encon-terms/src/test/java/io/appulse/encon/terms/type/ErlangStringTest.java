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

package io.appulse.encon.terms.type;

import static io.appulse.encon.terms.TermType.STRING;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;
import io.appulse.utils.test.TestMethodNamePrinter;

import erlang.OtpErlangString;
import erlang.OtpInputStream;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class ErlangStringTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void encode () {
    assertThat(new ErlangString("").toBytes())
        .isEqualTo(bytes(""));

    assertThat(new ErlangString("popa").toBytes())
        .isEqualTo(bytes("popa"));

    val string1 = repeat("a", 65536);
    assertThat(new ErlangString(string1).toBytes())
        .isEqualTo(bytes(string1));

    val string2 = repeat("я", 65536);
    assertThat(new ErlangString(string2).toBytes())
        .isEqualTo(bytes(string2));
  }

  @Test
  public void decode () throws Exception {
    val bytes = Bytes.allocate()
        .put1B(STRING.getCode())
        .put2B("popa".length())
        .put("popa", ISO_8859_1)
        .array();

    try (val input = new OtpInputStream(bytes)) {
      ErlangString string = ErlangTerm.newInstance(wrappedBuffer(bytes));
      assertThat(string.asText())
          .isEqualTo(input.read_string());
    }
  }

  private String repeat (String string, int times) {
    StringBuilder sb = new StringBuilder(string.length() * times);
    IntStream.range(0, times).forEach(it -> sb.append(string));
    return sb.toString();
  }

  @SneakyThrows
  private byte[] bytes (String value) {
    OtpErlangString string = new OtpErlangString(value);
    try (OtpOutputStream output = new OtpOutputStream()) {
      string.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
