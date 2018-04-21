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

import io.appulse.utils.test.TestMethodNamePrinter;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class NodeDescriptorTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void from () {
    NodeDescriptor descriptor = NodeDescriptor.from("popa@localhost");
    assertThat(descriptor).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(descriptor.getShortName())
          .isNotNull()
          .isEqualTo("popa");

      softly.assertThat(descriptor.getFullName())
          .isNotNull()
          .isEqualTo("popa@localhost");

      softly.assertThat(descriptor.getAddress())
          .isNotNull();
    });
  }
}
