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

import static io.appulse.encon.java.module.mailbox.request.ArrayItems.items;
import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R6;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;

import io.appulse.encon.java.config.MailboxConfig;
import io.appulse.encon.java.config.NodeConfig;
import io.appulse.encon.java.config.ServerConfig;
import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.protocol.term.ErlangTerm;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
public class NodeTest {

  private static final int DEFAULT_EPMD_PORT = 4369;

  Node node;

  @After
  public void after () {
    if (node != null) {
      node.close();
      node = null;
    }
  }

  @Test
  public void register () {
    node = Erts.node("popa");

    assertThat(node.generatePid())
        .isNotNull();

    assertThat(node.generatePort())
        .isNotNull();

    assertThat(node.generateReference())
        .isNotNull();

    val optional = node.lookup("popa");
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

      softly.assertThat(nodeInfo.getExtra())
          .isNull();
    });

    node.close();
    node = null;
  }

  @Test
  public void ping () throws Exception {
    node = Erts.node("node-1@localhost", NodeConfig.builder()
                     .cookie("secret")
                     .build()
    );

    assertThat(node.ping("echo@localhost").get(2, SECONDS))
        .isTrue();

    assertThat(node.ping("node-1@localhost").get(2, SECONDS))
        .isTrue();

    assertThat(node.ping("node-2@localhost").get(2, SECONDS))
        .isFalse();

    try (val node2 = Erts.node("node-2@localhost", NodeConfig.builder()
                               .cookie("secret")
                               .build())) {

      assertThat(node.ping("node-2@localhost").get(2, SECONDS))
          .isTrue();

      assertThat(node2.ping("node-1@localhost").get(2, SECONDS))
          .isTrue();
    }
  }

  @Test
  public void instantiating () throws Exception {
    node = Erts.node("popa", NodeConfig.builder()
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
  public void send () throws Exception {
    node = Erts.node("popa", NodeConfig.builder()
                     .server(ServerConfig.builder().port(8500).build())
                     .cookie("secret")
                     .build()
    );

    CompletableFuture<ErlangTerm> future = new CompletableFuture<>();
    Mailbox mailbox = node.mailbox()
        .handler((self, message) -> {
            future.complete(message.asTuple().get(1).get());
        })
        .build();

    mailbox.request()
        .makeTuple()
        .add(mailbox.getPid())
        .addTuple(items()
            .add("hello, world")
            .addAtom("popa")
            .add(false)
            .add(42)
        )
        .send("echo@localhost", "echo_server");

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
}
