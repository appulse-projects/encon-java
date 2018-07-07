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

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.val;
import org.yaml.snakeyaml.Yaml;

/**
 * Main nodes conigs aggregator.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Data
@NoArgsConstructor
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
public final class Config {

  /**
   * Loads configuration YAML file byt its path.
   *
   * @param fileName configuration file path
   *
   * @return parsed {@link Config} instance
   */
  public static Config load (@NonNull String fileName) {
    val file = new File(fileName);
    return load(file);
  }

  /**
   * Loads configuration YAML file byt its path.
   *
   * @param path configuration file path
   *
   * @return parsed {@link Config} instance
   */
  public static Config load (@NonNull Path path) {
    val file = path.toFile();
    return load(file);
  }

  /**
   * Loads configuration YAML file.
   *
   * @param file configuration file
   *
   * @return parsed {@link Config} instance
   */
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
    try (val inputStream = Files.newInputStream(file.toPath())) {
      return (Map<String, Map<String, Object>>) yaml.load(inputStream);
    }
  }

  Defaults defaults;

  Map<String, NodeConfig> nodes;

  /**
   * Copy constructor.
   *
   * @param config config to copy
   */
  public Config (Config config) {
    defaults = ofNullable(config.getDefaults())
        .map(Defaults::new)
        .orElse(null);
    nodes = ofNullable(config.getNodes())
        .map(it -> it.entrySet()
            .stream()
            .map(entry -> new SimpleEntry<>(entry.getKey(), new NodeConfig(entry.getValue())))
            .collect(toMap(Entry::getKey, Entry::getValue))
        )
        .orElse(null);
  }

  @Builder
  private Config (Defaults defaults, @Singular Map<String, NodeConfig> nodes) {
    this.defaults = ofNullable(defaults)
        .orElse(Defaults.INSTANCE);

    this.nodes = nodes.entrySet()
        .stream()
        .peek(it -> it.getValue().withDefaultsFrom(this.defaults))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }
}
