/*
 * Copyright 2018 Appulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appulse.encon.java;

import io.appulse.encon.java.config.Config;
import io.appulse.encon.java.config.Defaults;
import io.appulse.encon.java.config.NodeConfig;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 04.03.2018
 */
@Slf4j
public class Erts {

  public static Erts start (@NonNull Config config) {
    return null;
  }

  public static Node node (@NonNull String name) {
    val nodeConfig = NodeConfig.builder().build();
    return node(name, nodeConfig);
  }

  public static Node node (@NonNull String name, @NonNull NodeConfig nodeConfig) {
    nodeConfig.initDefaults(Defaults.INSTANCE);
    return Node.newInstance(name, nodeConfig);
  }
}
