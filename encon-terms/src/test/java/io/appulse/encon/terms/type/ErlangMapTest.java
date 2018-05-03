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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.test.TestMethodNamePrinter;

import erlang.OtpErlangMap;
import erlang.OtpErlangObject;
import erlang.OtpErlangString;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class ErlangMapTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void encode () {
    LinkedHashMap<String, String> value = new LinkedHashMap<>(3);
    value.put("one", "1");
    value.put("two", "2");
    value.put("three", "3");

    LinkedHashMap<ErlangTerm, ErlangTerm> map = new LinkedHashMap<>(3);
    value.forEach((k, v) -> {
      map.put(new ErlangString(k), new ErlangString(v));
    });

    assertThat(new ErlangMap(map).toBytes())
        .isEqualTo(bytes(value));
  }


  @SneakyThrows
  private byte[] bytes (LinkedHashMap<String, String> value) {
    List<OtpErlangObject> keys = new ArrayList<>();
    List<OtpErlangObject> values = new ArrayList<>();

    value.entrySet().forEach(it -> {
      keys.add(new OtpErlangString(it.getKey()));
      values.add(new OtpErlangString(it.getValue()));
    });

    OtpErlangMap map = new OtpErlangMap(
        keys.toArray(new OtpErlangObject[keys.size()]),
        values.toArray(new OtpErlangObject[values.size()])
    );
    try (OtpOutputStream output = new OtpOutputStream()) {
      map.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
