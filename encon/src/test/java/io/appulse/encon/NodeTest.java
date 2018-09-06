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

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.number;
import static io.appulse.encon.terms.Erlang.string;
import static io.appulse.encon.terms.Erlang.tuple;
import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R6;
import static java.lang.Boolean.FALSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Optional.ofNullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.encon.config.MailboxConfig;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.config.ServerConfig;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.mailbox.exception.ReceivedExitException;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangAtom;
import io.appulse.utils.test.TestMethodNamePrinter;
import io.appulse.epmd.java.server.cli.CommonOptions;
import io.appulse.epmd.java.server.command.server.ServerCommandExecutor;
import io.appulse.epmd.java.server.command.server.ServerCommandOptions;
import io.appulse.utils.SocketUtils;

import java.util.concurrent.ThreadLocalRandom;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
public class NodeTest {

  private static final String ELIXIR_ECHO_SERVER = "echo@localhost";

  private static ExecutorService executor;

  private static ServerCommandExecutor epmdServer;

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();


  Node node;

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

  @After
  public void after () throws Exception {
    if (node != null) {
      node.close();
      node = null;
    }
    MILLISECONDS.sleep(300);
  }

  @Test
  public void register () {
    val name = createName();
    node = Nodes.singleNode(name, true);

    assertThat(node.newReference())
        .isNotNull();

    val nodeInfo = node.lookup(name);
    assertThat(nodeInfo).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(nodeInfo.getPort())
          .isNotEqualTo(0);

      softly.assertThat(nodeInfo.getType())
          .isEqualTo(R6_ERLANG);

      softly.assertThat(nodeInfo.getProtocol())
          .isEqualTo(TCP);

      softly.assertThat(nodeInfo.getHigh())
          .isEqualTo(R6);

      softly.assertThat(nodeInfo.getLow())
          .isEqualTo(R6);
    });
  }

  @Test
  public void ping () throws Exception {
    val name1 = createName();
    val name2 = createName();
    node = Nodes.singleNode(name1, NodeConfig.builder()
                            .shortName(true)
                            .cookie("secret")
                            .build()
    );

    assertThat(node.ping(ELIXIR_ECHO_SERVER))
        .isTrue();
    assertThat(node.ping(ELIXIR_ECHO_SERVER))
        .isTrue();
    assertThat(node.ping(ELIXIR_ECHO_SERVER))
        .isTrue();
    assertThat(node.ping(ELIXIR_ECHO_SERVER))
        .isTrue();

    assertThat(node.ping(name1))
        .isTrue();

    assertThat(node.ping(name2))
        .isFalse();

    try (val node2 = Nodes.singleNode(name2, NodeConfig.builder()
                                      .shortName(true)
                                      .cookie("secret")
                                      .build())) {

      assertThat(node.ping(name2))
          .isTrue();

      assertThat(node2.ping(name1))
          .isTrue();
    }
  }

  @Test
  public void instantiating () throws Exception {
    val name = createName();
    node = Nodes.singleNode(name, NodeConfig.builder()
                            .shortName(true)
                            .mailbox(MailboxConfig.builder()
                                .name("one")
                                .build())
                            .mailbox(MailboxConfig.builder()
                                .name("two")
                                .build())
                            .build()
    );

    assertThat(node.mailbox("one")).isNotNull();
    assertThat(node.mailbox("two")).isNotNull();
    assertThat(node.mailbox("three")).isNull();
  }

  @Test
  public void sendFromOneToAnotherNode () throws Exception {
    val name1 = createName();
    val name2 = createName();

    node = Nodes.singleNode(name1, true);

    Mailbox mailbox1 = node.mailbox()
        .name("popa1")
        .build();

    try (val node2 = Nodes.singleNode(name2, true)) {

      String text1 = "Hello world 1";
      String text2 = "Hello world 2";

      Mailbox mailbox2 = node2.mailbox()
          .name("popa2")
          .build();

      mailbox2.send(name1, "popa1", string(text1));

      assertThat(mailbox1.receive().getBody().asText())
          .isEqualTo(text1);

      mailbox1.send(name2, "popa2", string(text2));

      assertThat(mailbox2.receive().getBody().asText())
          .isEqualTo(text2);
    }
  }

  @Test
  public void sendWithRedirect () throws Exception {
    val config = NodeConfig.builder()
        .shortName(true)
        .cookie("secret")
        .server(ServerConfig.builder()
            .bossThreads(1)
            .workerThreads(2)
            .build())
        .build();

    val name1 = createName();
    val name2 = createName();

    node = Nodes.singleNode(name1, config);
    log.info("Node #1 was created: {}", name1);

    try (Node node2 = Nodes.singleNode(name2, config)) {
      log.info("Node #2 was created: {}", name2);

      Mailbox mailbox1 = node.mailbox()
          .name("mail1")
          .build();

      Mailbox mailbox2 = node2.mailbox()
          .name("mail2")
          .build();

      val reference = node.newReference();
      log.info("Sending message from {} to {}", name1, ELIXIR_ECHO_SERVER);
      mailbox1.send(ELIXIR_ECHO_SERVER, "echo_server",
                    tuple(
                        mailbox1.getPid(),
                        mailbox2.getPid(),
                        tuple(
                            string("hello, world"),
                            atom("popa"),
                            atom(false),
                            number(42),
                            reference
                        )
                    ));

      log.info("Waiting message on {}", node2);
      ErlangTerm tuple = mailbox2.receive()
          .getBody()
          .asTuple()
          .getUnsafe(1);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(tuple.getUnsafe(0).asText()).isEqualTo("hello, world");
        softly.assertThat(tuple.getUnsafe(1).asText()).isEqualTo("popa");
        softly.assertThat(tuple.getUnsafe(2).asBoolean()).isEqualTo(FALSE);
        softly.assertThat(tuple.getUnsafe(3).asInt()).isEqualTo(42);
        softly.assertThat(tuple.getUnsafe(4).asReference()).isEqualTo(reference);
      });
    }
  }

  @Test
  public void send () throws Exception {
    val name = createName();
    node = Nodes.singleNode(name, NodeConfig.builder()
                            .shortName(true)
                            .cookie("secret")
                            .server(ServerConfig.builder()
                                .port(8500)
                                .build()
                            )
                            .build()
    );

    Mailbox mailbox = node.mailbox()
        .build();

    val reference = node.newReference();
    mailbox.send(ELIXIR_ECHO_SERVER, "echo_server", tuple(
                 mailbox.getPid(),
                 tuple(
                     string("hello, world"),
                     atom("popa"),
                     atom(false),
                     number(42),
                     reference
                 )
             ));

    ErlangTerm tuple = mailbox.receive()
        .getBody()
        .asTuple()
        .getUnsafe(1);

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(tuple.getUnsafe(0).asText()).isEqualTo("hello, world");
      softly.assertThat(tuple.getUnsafe(1).asText()).isEqualTo("popa");
      softly.assertThat(tuple.getUnsafe(2).asBoolean()).isEqualTo(FALSE);
      softly.assertThat(tuple.getUnsafe(3).asInt()).isEqualTo(42);
      softly.assertThat(tuple.getUnsafe(4).asReference()).isEqualTo(reference);
    });
  }

  @Test
  public void link () throws Exception {
    val name = createName();
    node = Nodes.singleNode(name, true);

    Mailbox mailbox1 = node.mailbox().build();
    Mailbox mailbox2 = node.mailbox().build();

    mailbox1.link(mailbox2.getPid());
    SECONDS.sleep(1);
    mailbox2.unlink(mailbox1.getPid());
    SECONDS.sleep(1);

    mailbox1.exit("popa");
    try {
      mailbox2.receive();
    } catch (ReceivedExitException ex) {
      assertThat(ex.getFrom())
          .isEqualTo(mailbox1.getPid());

      assertThat(ex.getReason())
          .isEqualTo(new ErlangAtom("popa"));

      return;
    }
    assertThat(false).isTrue();
  }

  @Test
  public void exit () throws Exception {
    val name = createName();
    node = Nodes.singleNode(name, true);

    Mailbox mailbox1 = node.mailbox().build();
    Mailbox mailbox2 = node.mailbox().build();

    mailbox2.link(mailbox1.getPid());
    SECONDS.sleep(1);

    mailbox2.exit("popa");

    try {
      mailbox1.receive();
    } catch (ReceivedExitException ex) {
      assertThat(ex.getFrom())
          .isEqualTo(mailbox2.getPid());

      assertThat(ex.getReason())
          .isEqualTo(new ErlangAtom("popa"));

      return;
    }
    assertThat(false).isTrue();
  }

  private String createName () {
    return new StringBuilder()
        .append("node_")
        .append(ThreadLocalRandom.current().nextInt(1000))
        .append("@localhost")
        .toString();
  }
}
