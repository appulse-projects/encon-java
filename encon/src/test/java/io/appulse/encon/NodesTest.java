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

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Optional.ofNullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.encon.config.Config;
import io.appulse.encon.config.Defaults;
import io.appulse.encon.config.MailboxConfig;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.config.ServerConfig;
import io.appulse.epmd.java.server.cli.CommonOptions;
import io.appulse.epmd.java.server.command.server.ServerCommandExecutor;
import io.appulse.epmd.java.server.command.server.ServerCommandOptions;
import io.appulse.utils.SocketUtils;
import io.appulse.utils.test.TestMethodNamePrinter;

import lombok.val;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class NodesTest {

  private static ExecutorService executor;

  private static ServerCommandExecutor epmdServer;

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @BeforeClass
  public static void beforeClass () {
    if (SocketUtils.isPortAvailable(4369)) {
      executor = Executors.newSingleThreadExecutor();
      epmdServer = new ServerCommandExecutor(new CommonOptions(), new ServerCommandOptions());
      executor.execute(epmdServer::execute);
    }
  }

  @AfterClass
  public static void afterClass () {
    ofNullable(epmdServer)
      .ifPresent(ServerCommandExecutor::close);

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
