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

package io.appulse.encon;

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
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.appulse.encon.common.DistributionFlag;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;
import io.appulse.utils.SocketUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = PRIVATE)
public final class NodesConfig {

  @Singular
  Map<String, NodeConfig> nodes;

  public NodesConfig (@NonNull NodesConfig other) {
    this.nodes = other.getNodes()
        .entrySet()
        .stream()
        .collect(toMap(Entry::getKey, it -> new NodeConfig(it.getValue())));
  }

  @Data
  @Wither
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  @FieldDefaults(level = PRIVATE)
  public static final class NodeConfig {

    private static final Set<DistributionFlag> DEFAULT_DISTRIBUTION_FLAGS = new HashSet<>(asList(
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

    public static final NodeConfig DEFAULT = NodeConfig.builder().build();

    /**
     * Returns default cookie. It could be an empty string or content of ~/.erlang.cookie file.
     *
     * @return default cookie value
     */
    public static String getDefaultCookie () {
      Path cookieFile = Paths.get(getHomeDir(), ".erlang.cookie");
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
      String home = System.getProperty("user.home");
      if (!System.getProperty("os.name").toLowerCase(ENGLISH).contains("windows")) {
        return home;
      }

      String drive = System.getenv("HOMEDRIVE");
      String path = System.getenv("HOMEPATH");
      return drive == null || path == null
             ? home
             : drive + path;
    }

    public static NodeConfigBuilder builder () {
      Set<DistributionFlag> clone = new HashSet<>(DEFAULT_DISTRIBUTION_FLAGS);
      return new NodeConfigBuilder().distributionFlags(clone);
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

    @Singular
    Set<DistributionFlag> distributionFlags;

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

    public NodeConfig (@NonNull NodeConfig other) {
      epmdPort = other.getEpmdPort();
      type = other.getType();
      shortName = other.getShortName();
      cookie = other.getCookie();
      protocol = other.getProtocol();
      lowVersion = other.getLowVersion();
      highVersion = other.getHighVersion();

      Set<DistributionFlag> flags = ofNullable(other.getDistributionFlags())
          .orElse(DEFAULT_DISTRIBUTION_FLAGS);

      distributionFlags = new HashSet<>(flags);

      server = ofNullable(other.getServer())
          .map(ServerConfig::new)
          .orElse(null);

      compression = ofNullable(other.getCompression())
          .map(CompressionConfig::new)
          .orElse(null);
    }

    @Data
    @Wither
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @FieldDefaults(level = PRIVATE)
    public static final class ServerConfig {

      private static final AtomicInteger UPPER_BOUND = new AtomicInteger(65535);

      public static int findFreePort () {
        while (true) {
          int currentValue = UPPER_BOUND.get();
          Optional<Integer> optional = SocketUtils.findFreePort(1024, currentValue);
          if (!optional.isPresent()) {
            UPPER_BOUND.set(65535);
            continue;
          }

          int port = optional.get();
          if (UPPER_BOUND.compareAndSet(currentValue, port - 1)) {
            return port;
          }
        }
      }

      Integer port;

      @Builder.Default
      Integer bossThreads = 1;

      @Builder.Default
      Integer workerThreads = 2;

      public ServerConfig (@NonNull ServerConfig other) {
        port = other.getPort();
        bossThreads = other.getBossThreads();
        workerThreads = other.getWorkerThreads();
      }

      public int getOrFindAndSetPort () {
        if (port == null) {
          port = findFreePort();
        }
        return port;
      }
    }

    @Data
    @Wither
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @FieldDefaults(level = PRIVATE)
    public static final class CompressionConfig {

      @Builder.Default
      Boolean enabled = FALSE;

      @Builder.Default
      Integer level = -1;

      public CompressionConfig (@NonNull CompressionConfig other) {
        enabled = other.getEnabled();
        level = other.getLevel();
      }
    }
  }
}
