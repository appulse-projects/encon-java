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

package io.appulse.encon.java;

import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.java.config.Config;
import io.appulse.encon.java.config.Defaults;
import io.appulse.encon.java.config.MailboxConfig;
import io.appulse.encon.java.config.NodeConfig;
import io.appulse.encon.java.config.ServerConfig;
import io.appulse.encon.java.util.TestMethodNamePrinter;

import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author alabazin
 */
public class ErtsTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void instantiating () {
    val config = Config.builder()
        .defaults(Defaults.builder()
            .clientThreads(1)
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

    val erts = Erts.start(config);

    assertThat(erts.node("kojima1"))
        .isPresent();
    assertThat(erts.node("kojima2"))
        .isPresent();
    assertThat(erts.node("ocelot"))
        .isPresent();
    assertThat(erts.node("kojima3"))
        .isNotPresent();
  }
}
