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

package io.appulse.encon.examples.echo.server.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.number;
import static io.appulse.encon.terms.Erlang.string;
import static io.appulse.encon.terms.Erlang.tuple;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.Optional.ofNullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.encon.spring.EnconProperties;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.epmd.java.server.cli.CommonOptions;
import io.appulse.epmd.java.server.command.server.ServerCommandExecutor;
import io.appulse.epmd.java.server.command.server.ServerCommandOptions;
import io.appulse.utils.SocketUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@SpringBootTest(properties = "spring.encon.defaults.short-name=true")
@SpringBootConfiguration
@ComponentScan(basePackageClasses = {
    EchoClient.class,
    EnconProperties.class
})
@ExtendWith(SpringExtension.class)
class MainTest {

  private static ExecutorService executor;

  private static ServerCommandExecutor epmdServer;

  @BeforeAll
  static void beforeAll () {
    if (SocketUtils.isPortAvailable(4369)) {
      executor = Executors.newSingleThreadExecutor();
      epmdServer = new ServerCommandExecutor(new CommonOptions(), new ServerCommandOptions());
      executor.execute(epmdServer::execute);
    }
  }

  @AfterAll
  static void afterAll () {
    ofNullable(epmdServer)
      .ifPresent(ServerCommandExecutor::close);

    ofNullable(executor)
      .ifPresent(ExecutorService::shutdown);
  }

  @Autowired
  EchoClient client;

  @Autowired
  EchoServer server;

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  void term () {
    ErlangTerm request = tuple(
        client.pid(),
        tuple(
            atom("ok"),
            number(42),
            string("Hello world")
        )
    );
    client.send(server.pid(), request);

    ErlangTerm response = client.receive(3, SECONDS)
        .flatMap(it -> it.get(1))
        .orElse(null);

    assertThat(request.getUnsafe(1))
        .isEqualTo(response);
  }

  @Test
  void pojo () {
    MyPojo2 request = new MyPojo2(
        client.pid(),
        "Artem",
        27,
        true,
        543,
        asList("java", "nim", "elixir"),
        "developer",
        singleton("popa"),
        "some long string...or not",
        new Boolean[] { true, false, true }
    );
    client.send(server.pid(), request);

    assertThat(client.receive(MyPojo2.class, 3, SECONDS))
      .hasValue(request);
  }
}
