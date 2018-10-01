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
public class GetFloatTest {

  @Test
  public void  test () {
    URL url = ResourceUtils.getResourceUrls("", "float.yml").get(0);
    Config config = Config.load(url);

    assertThat(config.getFloat("one"))
        .isPresent().hasValue(0F);
    assertThat(config.getFloat("two"))
        .isPresent().hasValue(0x0.000002P-126F);
    assertThat(config.getFloat("three"))
        .isPresent().hasValue(0x1.fffffeP+127F);

    assertThatThrownBy(() -> config.getFloat("four"))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("For input string: \"popa\"");
  }
}
