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

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.appulse.encon.common.DistributionFlag;
import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.FieldDefaults;

/**
 * Node configuration settings.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class NodeConfig {

  /**
   * Cached empty {@link NodeConfig} instance.
   */
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

    ofNullable(map.get("short-name"))
        .map(Object::toString)
        .map(Boolean::valueOf)
        .ifPresent(builder::shortName);

    ofNullable(map.get("cookie"))
        .map(Object::toString)
        .ifPresent(builder::cookie);

    ofNullable(map.get("protocol"))
        .map(Object::toString)
        .map(Protocol::valueOf)
        .ifPresent(builder::protocol);

    ofNullable(map.get("low-version"))
        .map(Object::toString)
        .map(Version::valueOf)
        .ifPresent(builder::lowVersion);

    ofNullable(map.get("high-version"))
        .map(Object::toString)
        .map(Version::valueOf)
        .ifPresent(builder::highVersion);

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

  Boolean shortName;

  String cookie;

  Protocol protocol;

  Version lowVersion;

  Version highVersion;

  @Singular
  Set<DistributionFlag> distributionFlags;

  @Singular
  List<MailboxConfig> mailboxes;

  ServerConfig server;

  CompressionConfig compression;

  /**
   * Copy constructor.
   *
   * @param nodeConfig config to copy
   */
  public NodeConfig (NodeConfig nodeConfig) {
    epmdPort = nodeConfig.getEpmdPort();
    type = nodeConfig.getType();
    shortName = nodeConfig.getShortName();
    cookie = nodeConfig.getCookie();
    protocol = nodeConfig.getProtocol();
    lowVersion = nodeConfig.getLowVersion();
    highVersion = nodeConfig.getHighVersion();
    distributionFlags = ofNullable(nodeConfig.getDistributionFlags())
        .map(HashSet::new)
        .orElse(null);
    mailboxes = ofNullable(nodeConfig.getMailboxes())
        .map(it -> it.stream()
            .map(MailboxConfig::new)
            .collect(toList())
        )
        .orElse(null);
    server = ofNullable(nodeConfig.getServer())
        .map(ServerConfig::new)
        .orElse(null);
    compression = ofNullable(nodeConfig.getCompression())
        .map(CompressionConfig::new)
        .orElse(null);
  }

  /**
   * Method for setting up the default values.
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

    shortName = ofNullable(shortName)
        .orElse(defaults.getShortName());

    cookie = ofNullable(cookie)
        .orElse(defaults.getCookie());

    protocol = ofNullable(protocol)
        .orElse(defaults.getProtocol());

    lowVersion = ofNullable(lowVersion)
        .orElse(defaults.getLowVersion());

    highVersion = ofNullable(highVersion)
        .orElse(defaults.getHighVersion());

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
