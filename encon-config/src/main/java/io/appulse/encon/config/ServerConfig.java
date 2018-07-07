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
 * Server's configuration.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class ServerConfig {

  static ServerConfig newInstance (@NonNull Map<String, Object> map) {
    ServerConfigBuilder builder = ServerConfig.builder();

    ofNullable(map.get("port"))
        .map(Object::toString)
        .map(Integer::parseInt)
        .ifPresent(builder::port);

    ofNullable(map.get("boss-threads"))
        .map(Object::toString)
        .map(Integer::parseInt)
        .ifPresent(builder::bossThreads);

    ofNullable(map.get("worker-threads"))
        .map(Object::toString)
        .map(Integer::parseInt)
        .ifPresent(builder::workerThreads);

    return builder.build();
  }

  Integer port;

  Integer bossThreads;

  Integer workerThreads;

  /**
   * Copy constructor.
   *
   * @param serverConfig config to copy
   */
  public ServerConfig (ServerConfig serverConfig) {
    port = serverConfig.getPort();
    bossThreads = serverConfig.getBossThreads();
    workerThreads = serverConfig.getWorkerThreads();
  }

  /**
   * Method for setting up the default values.
   *
   * @param defaults ServerConfig with default values for server
   *
   * @return reference to this object (for chain calls)
   */
  ServerConfig withDefaultsFrom (@NonNull ServerConfig defaults) {
    port = ofNullable(port)
        .orElse(ServerPortGenerator.nextPort());

    bossThreads = ofNullable(bossThreads)
        .orElse(defaults.getBossThreads());

    workerThreads = ofNullable(workerThreads)
        .orElse(defaults.getWorkerThreads());

    return this;
  }
}
