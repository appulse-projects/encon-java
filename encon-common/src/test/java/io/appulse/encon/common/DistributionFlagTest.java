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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@DisplayName("Check distribution flags enum")
class DistributionFlagTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("check if distribution flag's code could be converted to its enum value")
  void couldConvert () {
    Stream.of(DistributionFlag.values()).forEach(it -> {
      assertThat(DistributionFlag.parse(it.getCode()))
          .hasSize(1)
          .first()
          .isEqualTo(it);
    });
  }

  @Test
  @DisplayName("unfold integer to set of distribution flags")
  void unfold () {
    assertThat(DistributionFlag.parse(262_147))
        .hasSize(3)
        .contains(PUBLISHED, ATOM_CACHE, BIG_CREATION);
  }

  @Test
  @DisplayName("fold enum values to integer")
  void fold () {
    assertThat(DistributionFlag.bitwiseOr(PUBLISHED, ATOM_CACHE, BIG_CREATION))
        .isEqualTo(262_147);
  }
}
