/*
 * Copyright 2019 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.appulse.encon.config.Config;
import io.appulse.epmd.java.server.SubcommandServer;
import io.appulse.utils.SocketUtils;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import io.appulse.encon.NodesConfig.NodeConfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@DisplayName("Nodes cluster tests")
class NodesTest {

  private static ExecutorService executor;

  private static Future<?> future;

  @BeforeAll
  @SneakyThrows
  static void beforeAll () {
    if (SocketUtils.isPortAvailable(4369)) {
      executor = Executors.newSingleThreadExecutor();
      val server = SubcommandServer.builder()
          .port(SocketUtils.findFreePort().orElseThrow(RuntimeException::new))
          .ip(InetAddress.getByName("0.0.0.0"))
          .build();

      future = executor.submit(() -> {
        try {
          server.run();
        } catch (Throwable ex) {
          log.error("popa", ex);
        }
      });

      SECONDS.sleep(1);
    }
  }

  @AfterAll
  static void afterAll () {
    ofNullable(future)
      .ifPresent(it -> it.cancel(true));

    ofNullable(executor)
      .ifPresent(ExecutorService::shutdown);
  }

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("start several nodes from programmatically config")
  void instantiating () {
    Config config = Config.builder()
        .config(NodesConfig.builder()
            .node("kojima1", NodeConfig.DEFAULT)
            .node("kojima2", NodeConfig.DEFAULT)
            .build())
        .build();

    try (Nodes nodes = Nodes.start(config)) {
      assertThat(nodes.node("kojima1"))
          .isPresent();
      assertThat(nodes.node("kojima2"))
          .isPresent();
      assertThat(nodes.node("kojima3"))
          .isNotPresent();
    }
  }
}
