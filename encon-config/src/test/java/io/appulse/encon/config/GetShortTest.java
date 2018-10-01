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

import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
public class GetShortTest {

  @Test
  public void test () {
    URL url = ResourceUtils.getResourceUrls("", "short.yml").get(0);
    Config config = Config.load(url);

    assertThat(config.getShort("one"))
        .isPresent().hasValue((short) 0);
    assertThat(config.getShort("two"))
        .isPresent().hasValue((short) -32768);
    assertThat(config.getShort("three"))
        .isPresent().hasValue((short) 32767);

    assertThatThrownBy(() -> config.getShort("four"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("Value out of range. Value:\"32768\" Radix:10");

    assertThatThrownBy(() -> config.getShort("five"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("Value out of range. Value:\"-32769\" Radix:10");
  }
}
