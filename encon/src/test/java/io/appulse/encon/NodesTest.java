/*
 * Copyright 2020 the original author or authors.
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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Optional.ofNullable;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.appulse.encon.config.Config;
import io.appulse.encon.config.Defaults;
import io.appulse.encon.config.MailboxConfig;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.config.ServerConfig;
import io.appulse.epmd.java.server.SubcommandServer;
import io.appulse.utils.SocketUtils;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
public class NodesTest {

  private static ExecutorService executor;

  private static Future<?> future;

  @BeforeClass
  public static void beforeClass () throws Exception {
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

  @AfterClass
  public static void afterClass () {
    if (future != null) {
      future.cancel(true);
    }

    ofNullable(executor)
      .ifPresent(ExecutorService::shutdown);
  }

  @Test
  public void instantiating () {
    val config = Config.builder()
        .defaults(Defaults.builder()
            .cookie("kojima-secret")
            .server(ServerConfig.builder()
                .bossThreads(1)
                .workerThreads(1)
                .build())
            .build())
        .node("kojima1", new NodeConfig())
        .node("kojima2", new NodeConfig())
        .node("ocelot", NodeConfig.builder()
              .mailbox(MailboxConfig.builder()
                  .name("revolver")
                  .build())
              .build())
        .build();

    try (val nodes = Nodes.start(config)) {
      assertThat(nodes.node("kojima1"))
          .isPresent();
      assertThat(nodes.node("kojima2"))
          .isPresent();
      assertThat(nodes.node("ocelot"))
          .isPresent();
      assertThat(nodes.node("kojima3"))
          .isNotPresent();
    }
  }
}
