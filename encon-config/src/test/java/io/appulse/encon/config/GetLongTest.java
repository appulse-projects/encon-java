/*
 * Copyright 2019 the original author or authors.
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

package io.appulse.encon.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URL;

import io.appulse.utils.ResourceUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@DisplayName("Extracting long options from config")
class GetLongTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("checks long options")
  void test () {
    URL url = ResourceUtils.getResourceUrls("", "long.yml").get(0);
    Config config = Config.load(url);

    assertThat(config.getLong("one"))
        .isPresent().hasValue(0L);
    assertThat(config.getLong("two"))
        .isPresent().hasValue(-9223372036854775808L);
    assertThat(config.getLong("three"))
        .isPresent().hasValue(9223372036854775807L);

    assertThatThrownBy(() -> config.getLong("four"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("For input string: \"9223372036854775808\"");

    assertThatThrownBy(() -> config.getLong("five"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("For input string: \"-9223372036854775809\"");
  }
}
