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

import static io.appulse.encon.java.protocol.Erlang.atom;
import static io.appulse.encon.java.protocol.Erlang.number;
import static io.appulse.encon.java.protocol.Erlang.string;
import static io.appulse.encon.java.protocol.Erlang.tuple;
import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R6;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import io.appulse.encon.java.config.MailboxConfig;
import io.appulse.encon.java.config.NodeConfig;
import io.appulse.encon.java.config.ServerConfig;
import io.appulse.encon.java.module.connection.regular.Message;
import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.module.mailbox.exception.ReceivedExitException;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangAtom;
import io.appulse.encon.java.protocol.type.ErlangString;
import io.appulse.utils.test.TestMethodNamePrinter;

import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class NodeTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  private static final String ELIXIR_ECHO_SERVER = "echo@localhost";

  Node node;

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
    val name = createShortName();
    node = Nodes.singleNode(name);

    assertThat(node.generatePid())
        .isNotNull();

    assertThat(node.generatePort())
        .isNotNull();

    assertThat(node.generateReference())
        .isNotNull();

    val optional = node.lookup(name);
    assertThat(optional).isPresent();

    val nodeInfo = optional.get();
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
    val name1 = createFullName();
    val name2 = createFullName();
    node = Nodes.singleNode(name1, NodeConfig.builder()
                           .cookie("secret")
                           .build()
    );

    assertThat(node.ping(ELIXIR_ECHO_SERVER).get(2, SECONDS))
        .isTrue();
    assertThat(node.ping(ELIXIR_ECHO_SERVER).get(2, SECONDS))
        .isTrue();
    assertThat(node.ping(ELIXIR_ECHO_SERVER).get(2, SECONDS))
        .isTrue();
    assertThat(node.ping(ELIXIR_ECHO_SERVER).get(2, SECONDS))
        .isTrue();

    assertThat(node.ping(name1).get(2, SECONDS))
        .isTrue();

    assertThat(node.ping(name2).get(2, SECONDS))
        .isFalse();

    try (val node2 = Nodes.singleNode(name2, NodeConfig.builder()
                                     .cookie("secret")
                                     .build())) {

      assertThat(node.ping(name2).get(2, SECONDS))
          .isTrue();

      assertThat(node2.ping(name1).get(2, SECONDS))
          .isTrue();
    }
  }

  @Test
  public void instantiating () throws Exception {
    val name = createShortName();
    node = Nodes.singleNode(name, NodeConfig.builder()
                           .mailbox(MailboxConfig.builder()
                               .name("one")
                               .build())
                           .mailbox(MailboxConfig.builder()
                               .name("two")
                               .build())
                           .build()
    );

    assertThat(node.mailbox("one"))
        .isPresent();

    assertThat(node.mailbox("two"))
        .isPresent();

    assertThat(node.mailbox("three"))
        .isNotPresent();
  }

  @Test
  public void sendFromOneToAnotherNode () throws Exception {
    val name1 = createFullName();
    val name2 = createFullName();

    node = Nodes.singleNode(name1);

    CompletableFuture<String> future1 = new CompletableFuture<>();
    Mailbox mailbox1 = node.mailbox()
        .name("popa1")
        .handler((self, header, body) -> future1.complete(body.get().asText()))
        .build();

    try (val node2 = Nodes.singleNode(name2)) {

      String text1 = "Hello world 1";
      String text2 = "Hello world 2";
      CompletionStage<Message> stage1 = mailbox1.receiveAsync();

      CompletableFuture<String> future2 = new CompletableFuture<>();
      Mailbox mailbox2 = node2.mailbox()
          .name("popa2")
          .handler((self, header, body) -> future2.complete(body.get().asText()))
          .build();

      mailbox2.request()
          .body(string(text1))
          .send(name1, "popa1");

      assertThat(future1.get(2, SECONDS))
          .isEqualTo(text1);

      assertThat(stage1)
          .isCompleted()
          .isCompletedWithValueMatching(it -> it.getBody().get().equals(new ErlangString(text1)));

      CompletionStage<Message> stage2 = mailbox2.receiveAsync();

      mailbox1.request()
          .body(string(text2))
          .send(name2, "popa2");

      assertThat(stage2.toCompletableFuture().get(2, SECONDS))
          .isNotNull();
    }
  }

  @Test
  public void send () throws Exception {
    val name = createShortName();
    node = Nodes.singleNode(name, NodeConfig.builder()
                           .server(ServerConfig.builder().port(8500).build())
                           .cookie("secret")
                           .build()
    );

    CompletableFuture<ErlangTerm> future = new CompletableFuture<>();
    Mailbox mailbox = node.mailbox()
        .handler((self, header, body) -> {
          future.complete(body.get().asTuple().get(1).get());
        })
        .build();

    mailbox.request()
        .body(tuple(
            mailbox.getPid(),
            tuple(
                string("hello, world"),
                atom("popa"),
                atom(false),
                number(42)
            )
        ))
        .send(ELIXIR_ECHO_SERVER, "echo_server");

    SECONDS.sleep(3);

    assertThat(future)
        .isCompleted()
        .isCompletedWithValueMatching(it -> it.isTuple(), "It is not a tuple")
        .isCompletedWithValueMatching(it -> {
          val optional = it.get(0);
          if (!optional.isPresent()) {
            return false;
          }
          if (!optional.get().isTextual()) {
            return false;
          }
          return optional.get()
              .asText()
              .equals("hello, world");
        }, "Tuple(0)")
        .isCompletedWithValueMatching(it -> {
          val optional = it.get(1);
          if (!optional.isPresent()) {
            return false;
          }
          if (!optional.get().isAtom()) {
            return false;
          }
          return optional.get()
              .asText()
              .equals("popa");
        }, "Tuple(1)")
        .isCompletedWithValueMatching(it -> {
          val optional = it.get(2);
          if (!optional.isPresent()) {
            return false;
          }
          if (!optional.get().isBoolean()) {
            return false;
          }
          return optional.get()
              .asBoolean() == false;
        }, "Tuple(2)")
        .isCompletedWithValueMatching(it -> {
          val optional = it.get(3);
          if (!optional.isPresent()) {
            return false;
          }
          if (!optional.get().isInt()) {
            return false;
          }
          return optional.get()
              .asInt() == 42;
        }, "Tuple(3)");
  }

  @Test
  public void link () throws Exception {
    val name = createFullName();
    node = Nodes.singleNode(name);

    Mailbox mailbox1 = node.mailbox().build();
    Mailbox mailbox2 = node.mailbox().build();

    mailbox1.link(mailbox2.getPid());
    SECONDS.sleep(1);
    mailbox2.unlink(mailbox1.getPid());
    SECONDS.sleep(1);

    CompletableFuture<Message> future = mailbox2.receiveAsync();
    mailbox1.exit("popa");

    SECONDS.sleep(1);
    assertThat(future.isDone())
        .isFalse();
  }

  @Test
  public void exit () throws Exception {
    val name = createFullName();
    node = Nodes.singleNode(name);

    Mailbox mailbox1 = node.mailbox().build();
    Mailbox mailbox2 = node.mailbox().build();

    mailbox2.link(mailbox1.getPid());
    SECONDS.sleep(1);

    CompletableFuture<Message> future = mailbox1.receiveAsync();
    mailbox2.exit("popa");

    try {
      future.get();
    } catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      assertThat(cause).isInstanceOf(ReceivedExitException.class);

      val exitException = (ReceivedExitException) cause;

      assertThat(exitException.getFrom())
          .isEqualTo(mailbox2.getPid());

      assertThat(exitException.getReason())
          .isEqualTo(new ErlangAtom("popa"));
    }
  }

  private String createShortName () {
    return new StringBuilder()
        .append("node-")
        .append(ThreadLocalRandom.current().nextInt())
        .toString();
  }

  private String createFullName () {
    return new StringBuilder()
        .append("node-")
        .append(ThreadLocalRandom.current().nextInt())
        .append("@localhost")
        .toString();
  }
}