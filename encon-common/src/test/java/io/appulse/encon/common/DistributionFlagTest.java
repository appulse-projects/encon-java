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

package io.appulse.encon.common;

import static io.appulse.encon.common.DistributionFlag.ATOM_CACHE;
import static io.appulse.encon.common.DistributionFlag.BIG_CREATION;
import static io.appulse.encon.common.DistributionFlag.PUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import io.appulse.utils.test.TestMethodNamePrinter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class DistributionFlagTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void parse () {
    Stream.of(DistributionFlag.values()).forEach(it -> {
      assertThat(DistributionFlag.parse(it.getCode()))
          .hasSize(1)
          .first()
          .isEqualTo(it);
    });

    assertThat(DistributionFlag.parse(262_147))
        .hasSize(3)
        .contains(PUBLISHED, ATOM_CACHE, BIG_CREATION);
  }

  @Test
  public void bitwiseOr () {
    assertThat(DistributionFlag.bitwiseOr(PUBLISHED, ATOM_CACHE, BIG_CREATION))
        .isEqualTo(262_147);
  }
}
