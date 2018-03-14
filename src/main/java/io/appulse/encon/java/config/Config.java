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

package io.appulse.encon.java.config;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.val;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public final class Config {

  public static Config load (@NonNull String fileName) {
    val file = new File(fileName);
    return load(file);
  }

  public static Config load (@NonNull Path path) {
    val file = path.toFile();
    return load(file);
  }

  @SuppressWarnings("unchecked")
  public static Config load (@NonNull File file) {
    val map = parseYaml(file);

    ConfigBuilder builder = Config.builder();

    ofNullable(map)
        .map(it -> it.get("defaults"))
        .filter(Objects::nonNull)
        .map(Defaults::newInstance)
        .ifPresent(builder::defaults);

    ofNullable(map)
        .map(it -> it.get("nodes"))
        .filter(Objects::nonNull)
        .map(subMap -> subMap.entrySet()
            .stream()
            .filter(it -> it.getValue() instanceof Map)
            .collect(toMap(Entry::getKey, it -> NodeConfig.newInstance((Map<String, Object>) it.getValue())))
        )
        .ifPresent(builder::nodes);

    return builder.build();
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static Map<String, Map<String, Object>> parseYaml (@NonNull File file) {
    val yaml = new Yaml();
    try (val inputStream = new FileInputStream(file)) {
      return (Map<String, Map<String, Object>>) yaml.load(inputStream);
    }
  }

  Defaults defaults;

  Map<String, NodeConfig> nodes;

  @Builder
  private Config (Defaults defaults, @Singular Map<String, NodeConfig> nodes) {
    this.defaults = ofNullable(defaults)
        .orElse(Defaults.builder().build());

    this.nodes = nodes.entrySet()
        .stream()
        .peek(it -> it.getValue().withDefaultsFrom(this.defaults))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }
}
