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

import static io.appulse.encon.common.DistributionFlag.BIG_CREATION;
import static io.appulse.encon.common.DistributionFlag.BIT_BINARIES;
import static io.appulse.encon.common.DistributionFlag.EXTENDED_PIDS_PORTS;
import static io.appulse.encon.common.DistributionFlag.EXTENDED_REFERENCES;
import static io.appulse.encon.common.DistributionFlag.FUN_TAGS;
import static io.appulse.encon.common.DistributionFlag.MAP_TAG;
import static io.appulse.encon.common.DistributionFlag.NEW_FLOATS;
import static io.appulse.encon.common.DistributionFlag.NEW_FUN_TAGS;
import static io.appulse.encon.common.DistributionFlag.UTF8_ATOMS;
import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R6;
import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.appulse.encon.common.DistributionFlag;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * Set of defaults for configuration settings.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class Defaults {

  /**
   * Cached defaults instance.
   */
  public static final Defaults INSTANCE = Defaults.builder().build();

  /**
   * Returns default cookie. It could be an empty string or content of ~/.erlang.cookie file.
   *
   * @return default cookie value
   */
  public static String getDefaultCookie () {
    val cookieFile = Paths.get(getHomeDir(), ".erlang.cookie");
    if (!Files.exists(cookieFile)) {
      return "";
    }

    try {
      return Files.lines(cookieFile)
          .filter(Objects::nonNull)
          .map(String::trim)
          .filter(it -> !it.isEmpty())
          .findFirst()
          .orElse("");
    } catch (IOException ex) {
      return "";
    }
  }

  /**
   * Returns user's home directory.
   *
   * @return user's home directory
   */
  public static String getHomeDir () {
    val home = System.getProperty("user.home");
    if (!System.getProperty("os.name").toLowerCase(ENGLISH).contains("windows")) {
      return home;
    }

    val drive = System.getenv("HOMEDRIVE");
    val path = System.getenv("HOMEPATH");
    return drive == null || path == null
           ? home
           : drive + path;
  }

  @SuppressWarnings("unchecked")
  static Defaults newInstance (@NonNull Map<String, Object> map) {
    DefaultsBuilder builder = Defaults.builder();

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

    ofNullable(map.get("mailbox"))
        .filter(it -> it instanceof Map)
        .map(it -> (Map<String, Object>) it)
        .map(MailboxConfig::newInstance)
        .ifPresent(builder::mailbox);

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

  @Builder.Default
  int epmdPort = EpmdClient.Default.PORT;

  @Builder.Default
  NodeType type = R6_ERLANG;

  @Builder.Default
  Boolean shortName = FALSE;

  @Builder.Default
  String cookie = getDefaultCookie();

  @Builder.Default
  Protocol protocol = TCP;

  @Builder.Default
  Version lowVersion = R6;

  @Builder.Default
  Version highVersion = R6;

  @Builder.Default
  Set<DistributionFlag> distributionFlags = new HashSet<>(asList(
      EXTENDED_REFERENCES,
      EXTENDED_PIDS_PORTS,
      BIT_BINARIES,
      NEW_FLOATS,
      FUN_TAGS,
      NEW_FUN_TAGS,
      UTF8_ATOMS,
      MAP_TAG,
      BIG_CREATION
  ));

  @Builder.Default
  MailboxConfig mailbox = MailboxConfig.builder()
      .build();

  @Builder.Default
  ServerConfig server = ServerConfig.builder()
      .bossThreads(1)
      .workerThreads(2)
      .build();

  @Builder.Default
  CompressionConfig compression = CompressionConfig.builder()
      .enabled(FALSE)
      .level(-1)
      .build();

  /**
   * Copy constructor.
   *
   * @param defaults config to copy
   */
  public Defaults (Defaults defaults) {
    epmdPort = defaults.getEpmdPort();
    type = defaults.getType();
    shortName = defaults.getShortName();
    cookie = defaults.getCookie();
    protocol = defaults.getProtocol();
    lowVersion = defaults.getLowVersion();
    highVersion = defaults.getHighVersion();
    distributionFlags = ofNullable(defaults.getDistributionFlags())
        .map(HashSet::new)
        .orElse(null);
    mailbox = ofNullable(defaults.getMailbox())
        .map(MailboxConfig::new)
        .orElse(null);
    server = ofNullable(defaults.getServer())
        .map(ServerConfig::new)
        .orElse(null);
    compression = ofNullable(defaults.getCompression())
        .map(CompressionConfig::new)
        .orElse(null);
  }
}
