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
@DisplayName("Extracting integer options from config")
class GetIntegerTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("checks integer options")
  void test () {
    URL url = ResourceUtils.getResourceUrls("", "int.yml").get(0);
    Config config = Config.load(url);

    assertThat(config.getInteger("one"))
        .isPresent().hasValue(0);
    assertThat(config.getInteger("two"))
        .isPresent().hasValue(-2147483648);
    assertThat(config.getInteger("three"))
        .isPresent().hasValue(2147483647);

    assertThatThrownBy(() -> config.getInteger("four"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("For input string: \"2147483648\"");

    assertThatThrownBy(() -> config.getInteger("five"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("For input string: \"-2147483649\"");
  }
}
