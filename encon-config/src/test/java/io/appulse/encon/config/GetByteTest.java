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
@DisplayName("Extracting byte options from config")
class GetByteTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("checks byte options")
  void test () {
    URL url = ResourceUtils.getResourceUrls("", "byte.yml").get(0);
    Config config = Config.load(url);

    assertThat(config.getByte("one"))
        .isPresent().hasValue((byte) 0);
    assertThat(config.getByte("two"))
        .isPresent().hasValue((byte) -128);
    assertThat(config.getByte("three"))
        .isPresent().hasValue((byte) 127);

    assertThatThrownBy(() -> config.getByte("four"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("Value out of range. Value:\"128\" Radix:10");

    assertThatThrownBy(() -> config.getByte("five"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("Value out of range. Value:\"-129\" Radix:10");
  }
}
