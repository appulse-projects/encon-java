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

import io.appulse.encon.config.MailboxConfig;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.config.ServerConfig;
import io.appulse.encon.module.connection.regular.Message;
import io.appulse.encon.module.mailbox.Mailbox;
import io.appulse.encon.module.mailbox.exception.ReceivedExitException;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangAtom;
import io.appulse.utils.test.TestMethodNamePrinter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
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

    Mailbox mailbox1 = node.mailbox()
        .name("popa1")
        .build();

    CompletableFuture<String> future11 = mailbox1.receiveAsync()
        .thenApply(it -> it.getBodyUnsafe().asText());
    CompletableFuture<String> future12 = mailbox1.receiveAsync()
        .thenApply(it -> it.getBodyUnsafe().asText());

    try (val node2 = Nodes.singleNode(name2)) {

      String text1 = "Hello world 1";
      String text2 = "Hello world 2";

      Mailbox mailbox2 = node2.mailbox()
          .name("popa2")
          .build();

      CompletableFuture<String> future2 = mailbox2.receiveAsync()
          .thenApply(it -> it.getBodyUnsafe().asText());

      mailbox2.request()
          .body(string(text1))
          .send(name1, "popa1");

      assertThat(future11.get(2, SECONDS))
          .isEqualTo(text1);
      assertThat(future12.get(2, SECONDS))
          .isEqualTo(text1);

      mailbox1.request()
          .body(string(text2))
          .send(name2, "popa2");

      assertThat(future2.get(2, SECONDS))
          .isNotNull();
    }
  }

//  @Test
  public void sendWithRedirect () throws Exception {
    val config = NodeConfig.builder()
        .cookie("secret")
        .build();

    val name1 = createShortName();
    val name2 = createShortName();

    node = Nodes.singleNode(name1, config);

    try (Node node2 = Nodes.singleNode(name2, config)) {
      Mailbox mailbox1 = node.mailbox().build();
      Mailbox mailbox2 = node2.mailbox().name("popka").build();

      CompletableFuture<ErlangTerm> future = mailbox2.receiveAsync()
          .thenApplyAsync(message -> message.getBody().get().asTuple().get(1).get());

      val reference = node.generateReference();
      mailbox1.request()
          .body(tuple(
              mailbox1.getPid(),
              mailbox2.getPid(),
              tuple(
                  string("hello, world"),
                  atom("popa"),
                  atom(false),
                  number(42),
                  reference
              )
          ))
          .send(ELIXIR_ECHO_SERVER, "echo_server");

      SECONDS.sleep(10);

      assertThat(future)
          .isCompleted()
          .isCompletedWithValueMatching(ErlangTerm::isTuple, "It is not a tuple")
          .isCompletedWithValueMatching(it -> it.get(0)
              .filter(ErlangTerm::isTextual)
              .map(ErlangTerm::asText)
              .map("hello, world"::equals)
              .orElse(false),
                                        "Tuple(0)")
          .isCompletedWithValueMatching(it -> it.get(1)
              .filter(ErlangTerm::isAtom)
              .map(ErlangTerm::asText)
              .map("popa"::equals)
              .orElse(false),
                                        "Tuple(1)")
          .isCompletedWithValueMatching(it -> it.get(2)
              .filter(ErlangTerm::isBoolean)
              .map(ErlangTerm::asBoolean)
              .map(FALSE::equals)
              .orElse(false),
                                        "Tuple(2)")
          .isCompletedWithValueMatching(it -> it.get(3)
              .filter(ErlangTerm::isInt)
              .map(ErlangTerm::asInt)
              .map(value -> value == 42)
              .orElse(false),
                                        "Tuple(3)")
          .isCompletedWithValueMatching(it -> it.get(4)
              .filter(ErlangTerm::isReference)
              .map(ErlangTerm::asReference)
              .map(reference::equals)
              .orElse(false),
                                        "Tuple(4)");
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
        .handler((self, message) -> {
          future.complete(message.getBodyUnsafe().asTuple().get(1).get());
        })
        .build();

    val reference = node.generateReference();
    mailbox.request()
        .body(tuple(
            mailbox.getPid(),
            tuple(
                string("hello, world"),
                atom("popa"),
                atom(false),
                number(42),
                reference
            )
        ))
        .send(ELIXIR_ECHO_SERVER, "echo_server");

    SECONDS.sleep(2);

    assertThat(future)
        .isCompleted()
        .isCompletedWithValueMatching(ErlangTerm::isTuple, "It is not a tuple")
        .isCompletedWithValueMatching(it -> it.get(0)
            .filter(ErlangTerm::isTextual)
            .map(ErlangTerm::asText)
            .map("hello, world"::equals)
            .orElse(false),
                                      "Tuple(0)")
        .isCompletedWithValueMatching(it -> it.get(1)
            .filter(ErlangTerm::isAtom)
            .map(ErlangTerm::asText)
            .map("popa"::equals)
            .orElse(false),
                                      "Tuple(1)")
        .isCompletedWithValueMatching(it -> it.get(2)
            .filter(ErlangTerm::isBoolean)
            .map(ErlangTerm::asBoolean)
            .map(FALSE::equals)
            .orElse(false),
                                      "Tuple(2)")
        .isCompletedWithValueMatching(it -> it.get(3)
            .filter(ErlangTerm::isInt)
            .map(ErlangTerm::asInt)
            .map(value -> value == 42)
            .orElse(false),
                                      "Tuple(3)")
        .isCompletedWithValueMatching(it -> it.get(4)
            .filter(ErlangTerm::isReference)
            .map(ErlangTerm::asReference)
            .map(reference::equals)
            .orElse(false),
                                      "Tuple(4)");
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
    assertThat(future.isCompletedExceptionally())
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
        .append("node_")
        .append(ThreadLocalRandom.current().nextInt(1000))
        .toString();
  }

  private String createFullName () {
    return new StringBuilder()
        .append("node_")
        .append(ThreadLocalRandom.current().nextInt(1000))
        .append("@localhost")
        .toString();
  }
}
