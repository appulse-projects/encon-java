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

package io.appulse.encon.java;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import io.appulse.encon.java.protocol.type.Port;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
public class GeneratorPortTest {

  @Test
  public void generate () {
    GeneratorPort generator = new GeneratorPort("popa", 1);

    Port port = generator.generatePort();
    assertThat(port).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(port.getNode())
          .isNotNull()
          .isEqualTo("popa");

      softly.assertThat(port.getId())
          .isEqualTo(1);

      softly.assertThat(port.getCreation())
          .isEqualTo(1);
    });
  }
}
