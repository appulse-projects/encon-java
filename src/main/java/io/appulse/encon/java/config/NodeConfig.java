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

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.DistributionFlag;
import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class NodeConfig {

  public static final NodeConfig DEFAULT = NodeConfig.builder().build();

  @SuppressWarnings("unchecked")
  static NodeConfig newInstance (@NonNull Map<String, Object> map) {
    NodeConfigBuilder builder = NodeConfig.builder();

    ofNullable(map.get("epmd-port"))
        .map(Object::toString)
        .map(Integer::parseInt)
        .ifPresent(builder::epmdPort);

    ofNullable(map.get("type"))
        .map(Object::toString)
        .map(NodeType::valueOf)
        .ifPresent(builder::type);

    ofNullable(map.get("cookie"))
        .map(Object::toString)
        .ifPresent(builder::cookie);

    ofNullable(map.get("protocol"))
        .map(Object::toString)
        .map(Protocol::valueOf)
        .ifPresent(builder::protocol);

    if (map.containsKey("version") && map.get("version") instanceof Map) {
      val versionMap = (Map<String, Object>) map.get("version");

      ofNullable(versionMap.get("low"))
          .map(Object::toString)
          .map(Version::valueOf)
          .ifPresent(builder::low);

      ofNullable(versionMap.get("high"))
          .map(Object::toString)
          .map(Version::valueOf)
          .ifPresent(builder::high);
    }

    ofNullable(map.get("client-threads"))
        .map(Object::toString)
        .map(Integer::parseInt)
        .ifPresent(builder::clientThreads);

    ofNullable(map.get("distribution-flags"))
        .filter(it -> it instanceof List)
        .map(it -> (List<String>) it)
        .map(it -> it.stream().map(DistributionFlag::valueOf).collect(toSet()))
        .ifPresent(builder::distributionFlags);

    ofNullable(map.get("mailboxes"))
        .filter(it -> it instanceof List)
        .map(it -> (List<Map<String, Object>>) it)
        .map(it -> it.stream()
            .map(sub -> MailboxConfig.newInstance(sub))
            .collect(toList())
        )
        .ifPresent(builder::mailboxes);

    ofNullable(map.get("server"))
        .filter(it -> it instanceof Map)
        .map(it -> (Map<String, Object>) it)
        .map(ServerConfig::newInstance)
        .ifPresent(builder::server);

    ofNullable(map.get("compression"))
        .filter(it -> it instanceof Map)
        .map(it -> (Map<String, Object>) it)
        .map(CompressionConfig::newInstance)
        .ifPresent(builder::compression);

    return builder.build();
  }

  Integer epmdPort;

  NodeType type;

  String cookie;

  Protocol protocol;

  Version low;

  Version high;

  Integer clientThreads;

  @Singular
  Set<DistributionFlag> distributionFlags;

  @Singular
  List<MailboxConfig> mailboxes;

  ServerConfig server;

  CompressionConfig compression;

  /**
   * Method for setting up default values.
   *
   * @param defaults Defaults with default values for node
   *
   * @return reference to this object (for chain calls)
   */
  public NodeConfig withDefaultsFrom (@NonNull Defaults defaults) {
    epmdPort = ofNullable(epmdPort)
        .orElse(defaults.getEpmdPort());

    type = ofNullable(type)
        .orElse(defaults.getType());

    cookie = ofNullable(cookie)
        .orElse(defaults.getCookie());

    protocol = ofNullable(protocol)
        .orElse(defaults.getProtocol());

    low = ofNullable(low)
        .orElse(defaults.getLow());

    high = ofNullable(high)
        .orElse(defaults.getHigh());

    clientThreads = ofNullable(clientThreads)
        .orElse(defaults.getClientThreads());

    distributionFlags = ofNullable(distributionFlags)
        .filter(it -> !it.isEmpty())
        .orElse(defaults.getDistributionFlags());

    mailboxes = ofNullable(mailboxes)
        .map(it -> it.stream()
            .map(mailbox -> mailbox.withDefaultsFrom(defaults.getMailbox()))
            .collect(toList())
        )
        .orElse(emptyList());

    server = ofNullable(server)
        .orElse(ServerConfig.builder().build())
        .withDefaultsFrom(defaults.getServer());

    compression = ofNullable(compression)
        .orElse(CompressionConfig.builder().build())
        .withDefaultsFrom(defaults.getCompression());

    return this;
  }
}
