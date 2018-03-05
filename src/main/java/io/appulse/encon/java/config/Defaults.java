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

import static io.appulse.encon.java.DistributionFlag.BIG_CREATION;
import static io.appulse.encon.java.DistributionFlag.BIT_BINARIES;
import static io.appulse.encon.java.DistributionFlag.EXTENDED_PIDS_PORTS;
import static io.appulse.encon.java.DistributionFlag.EXTENDED_REFERENCES;
import static io.appulse.encon.java.DistributionFlag.FUN_TAGS;
import static io.appulse.encon.java.DistributionFlag.MAP_TAG;
import static io.appulse.encon.java.DistributionFlag.NEW_FLOATS;
import static io.appulse.encon.java.DistributionFlag.NEW_FUN_TAGS;
import static io.appulse.encon.java.DistributionFlag.UTF8_ATOMS;
import static io.appulse.encon.java.module.mailbox.ReceiverType.SINGLE;
import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R6;
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

import io.appulse.encon.java.DistributionFlag;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 22.02.2018
 */
@Data
@Builder
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Defaults {

  public static final Defaults INSTANCE = Defaults.builder().build();

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

  public static String getHomeDir () {
    val home = System.getProperty("user.home");
    if (!System.getProperty("os.name").toLowerCase(ENGLISH).contains("windows")) {
      return home;
    }

    val drive = System.getenv("HOMEDRIVE");
    val path = System.getenv("HOMEPATH");
    return drive != null && path != null
           ? drive + path
           : home;
  }

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

    return builder.build();
  }

  @Builder.Default
  int epmdPort = EpmdClient.Default.PORT;

  @Builder.Default
  NodeType type = R6_ERLANG;

  @Builder.Default
  String cookie = getDefaultCookie();

  @Builder.Default
  Protocol protocol = TCP;

  @Builder.Default
  Version low = R6;

  @Builder.Default
  Version high = R6;

  @Builder.Default
  int clientThreads = 2;

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
      .receiverType(SINGLE)
      .handler(io.appulse.encon.java.module.mailbox.StubReceiveHandler.class)
      .build();

  @Builder.Default
  ServerConfig server = ServerConfig.builder()
      .bossThreads(1)
      .workerThreads(2)
      .build();
}
