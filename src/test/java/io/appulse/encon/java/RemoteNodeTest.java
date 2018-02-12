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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.time.Duration.ofSeconds;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;

import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.util.TestEpmdServer;
import io.appulse.encon.java.util.TestMethodNamePrinter;
import io.appulse.epmd.java.core.model.NodeType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
public class RemoteNodeTest {

  // @ClassRule
  // public static final GenericContainer<?> ECHO = new GenericContainer<>("xxlabaza/echo-service-elixir:latest")
  //     .withCommand("--cookie=secret", "--name=echo@localhost")
  //     .withLogConsumer(frame -> log.info(((OutputFrame) frame).getUtf8String()))
  //     .withExposedPorts(4369)
  //     .withStartupTimeout(ofSeconds(10))
  //     .waitingFor(new GenericContainer.AbstractWaitStrategy() {

  //       @Override
  //       @SneakyThrows
  //       protected void waitUntilReady () {
  //         SECONDS.sleep(2);
  //       }
  //     });

  // @Rule
  // public TestRule watcher = new TestMethodNamePrinter();

  // TestEpmdServer epmd;

  Node node;

  @Before
  public void before () {
    // epmd = new TestEpmdServer();
    // epmd.start();
  }

  @After
  public void after () {
    // epmd.stop();
    // epmd = null;

    if (node != null) {
      node.close();
      node = null;
    }
  }

  @Test
  public void ping () throws Exception {
    node = Node.builder()
        .name("gurka")
        .port(8500)
        .cookie("secret")
        .meta(Meta.builder()
            .type(NodeType.R3_ERLANG)
            .build()
        )
        .build()
        // .register(epmd.getPort());
        .register(4369);

    CompletableFuture<Boolean> future = node.ping("echo@localhost");

    TimeUnit.SECONDS.sleep(3);

    assertThat(future)
        .isCompletedWithValue(true);
  }

  // @Test
  public void send () {
    node = Node.builder()
        .name("gurka")
        .port(8500)
        .cookie("secret")
        .meta(Meta.builder()
            .type(NodeType.R3_ERLANG)
            .build()
        )
        .build()
        // .register(epmd.getPort());
        .register(4369);

    CompletableFuture<String> future = new CompletableFuture<>();
    Mailbox mailbox =  node.createMailbox((self, message) -> {
        future.complete(message.asTuple().get(1).get().asText());
    });

    mailbox.request()
        .makeTuple()
            .add(mailbox.getPid())
            .add("Hello world!")
        .send("echo@localhost", "echo_server");

    assertThat(future)
        .isCompletedWithValue("Hello world!");
  }
}
