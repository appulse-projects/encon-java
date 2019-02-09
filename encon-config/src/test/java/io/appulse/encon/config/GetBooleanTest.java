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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

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
@DisplayName("Extracting boolean options from config")
class GetBooleanTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("checks boolean options")
  void test () {
    URL url = ResourceUtils.getResourceUrls("", "boolean.yml").get(0);
    Config config = Config.load(url);

    assertThat(config.getBoolean("one"))
        .isPresent().hasValue(TRUE);
    assertThat(config.getBoolean("two"))
        .isPresent().hasValue(FALSE);

    assertThat(config.getBoolean("three"))
        .isPresent().hasValue(TRUE);
    assertThat(config.getBoolean("four"))
        .isPresent().hasValue(FALSE);

    assertThat(config.getBoolean("five"))
        .isPresent().hasValue(TRUE);
    assertThat(config.getBoolean("six"))
        .isPresent().hasValue(FALSE);

    assertThat(config.getBoolean("seven"))
        .isPresent().hasValue(TRUE);
    assertThat(config.getBoolean("eight"))
        .isPresent().hasValue(TRUE);

    assertThat(config.getBoolean("nine"))
        .isPresent().hasValue(FALSE);
    assertThat(config.getBoolean("ten"))
        .isPresent().hasValue(FALSE);
  }
}
