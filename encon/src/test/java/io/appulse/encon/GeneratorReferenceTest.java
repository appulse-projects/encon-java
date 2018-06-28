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

package io.appulse.encon;

import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.common.NodeDescriptor;
import io.appulse.utils.test.TestMethodNamePrinter;

import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class GeneratorReferenceTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void generateReference () {
    val descriptor = NodeDescriptor.from("popa");
    val creation = 1;

    val generator = new GeneratorReference(descriptor.getFullName(), creation);

    val reference = generator.generate();
    assertThat(reference).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(reference.getDescriptor().getFullName())
          .isNotNull()
          .isEqualTo(descriptor.getFullName());

      softly.assertThat(reference.getIds())
          .isNotNull()
          .isEqualTo(new long[] { 1, 0, 0 });

      softly.assertThat(reference.getCreation())
          .isEqualTo(creation);
    });
  }
}
