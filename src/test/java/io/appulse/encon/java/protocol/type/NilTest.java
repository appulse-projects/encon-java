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
import static io.appulse.encon.java.protocol.TermType.NIL;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import org.junit.Test;
import lombok.val;

public class NilTest {

  @Test
  public void newInstance () {
    val bytes = Bytes.allocate()
        .put1B(NIL.getCode())
        .array();

    Nil nil = ErlangTerm.newInstance(bytes);
    assertThat(nil).isNotNull();
    assertThat(nil.getType())
        .isEqualTo(NIL);
  }

  @Test
  public void toBytes () {
    val expected = Bytes.allocate()
        .put1B(NIL.getCode())
        .array();

    assertThat(new Nil().toBytes())
        .isEqualTo(expected);
  }
}
