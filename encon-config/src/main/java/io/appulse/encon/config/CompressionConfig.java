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
import static lombok.AccessLevel.PRIVATE;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * Protocol compression settings.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class CompressionConfig {

  static CompressionConfig newInstance (@NonNull Map<String, Object> map) {
    CompressionConfigBuilder builder = CompressionConfig.builder();

    ofNullable(map.get("enabled"))
        .map(Object::toString)
        .map(Boolean::parseBoolean)
        .ifPresent(builder::enabled);

    ofNullable(map.get("level"))
        .map(Object::toString)
        .map(Integer::parseInt)
        .ifPresent(builder::level);

    return builder.build();
  }

  Boolean enabled;

  Integer level;

  /**
   * Copy constructor.
   *
   * @param compressionConfig config to copy
   */
  public CompressionConfig (CompressionConfig compressionConfig) {
    enabled = compressionConfig.getEnabled();
    level = compressionConfig.getLevel();
  }

  /**
   * Method for setting up the default values.
   *
   * @param defaults CompressionConfig with default values for compression
   *
   * @return reference to this object (for chain calls)
   */
  CompressionConfig withDefaultsFrom (@NonNull CompressionConfig defaults) {
    enabled = ofNullable(enabled)
        .orElse(defaults.getEnabled());

    level = ofNullable(level)
        .orElse(defaults.getLevel());

    return this;
  }
}
