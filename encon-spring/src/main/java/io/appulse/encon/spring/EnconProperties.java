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

package io.appulse.encon.spring;

import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import io.appulse.encon.config.Config;
import io.appulse.encon.config.Defaults;
import io.appulse.encon.config.NodeConfig;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
@Data
@Component
@Validated
@FieldDefaults(level = PRIVATE)
@ConfigurationProperties(prefix = "spring.encon")
public class EnconProperties {

  @Getter(value = PACKAGE, lazy = true)
  final Config config = createConfig();

  Boolean enabled;

  Defaults defaults;

  @NonNull
  Map<String, NodeConfig> nodes = new LinkedHashMap<>();

  /**
   * Post construct. Initialize with default values the fields.
   */
  @PostConstruct
  public void postConstruct () {
    if (enabled == null) {
      enabled = TRUE;
    }
    if (defaults == null) {
      defaults = Defaults.INSTANCE;
    }
  }

  private Config createConfig () {
    return Config.builder()
        .defaults(defaults)
        .nodes(nodes)
        .build();
  }
}
