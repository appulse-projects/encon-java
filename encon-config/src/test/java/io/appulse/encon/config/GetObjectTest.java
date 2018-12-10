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

import java.net.URL;
import java.util.List;
import java.util.Map;

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
@DisplayName("Extracting object options from config")
class GetObjectTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("checks object options")
  void test () {
    URL url = ResourceUtils.getResourceUrls("", "object.yml").get(0);
    Config config = Config.load(url);

    assertThat(config.get("one"))
        .isPresent()
        .get()
        .isInstanceOf(Number.class);
    assertThat(config.get("two"))
        .isPresent()
        .get()
        .isInstanceOf(String.class);
    assertThat(config.get("three"))
        .isPresent()
        .get()
        .isInstanceOf(Boolean.class);
    assertThat(config.get("four"))
        .isPresent()
        .get()
        .isInstanceOf(List.class);
    assertThat(config.get("five"))
        .isPresent()
        .get()
        .isInstanceOf(Map.class);
  }
}
