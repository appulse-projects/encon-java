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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R6;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Test;

import io.appulse.encon.java.module.mailbox.Mailbox;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
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
    node = Node.builder()
        .name("popa")
        .port(8971)
        .build()
        .register(DEFAULT_EPMD_PORT);

    assertThat(node.isRegistered())
        .isTrue();

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
          .isEqualTo(8971);

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

    assertThat(node.isRegistered()).isFalse();
    node = null;
  }

  @Test
  public void generatorsFailsWithoutRegistration () {
    node = Node.builder()
        .name("popa")
        .port(8971)
        .build();

    assertThatThrownBy(() -> node.generatePid())
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> node.generateReference())
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> node.generatePort())
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void ping () throws Exception {
    node = Node.builder()
        .name("node-1")
        .port(8971)
        .cookie("secret")
        .build()
        .register(DEFAULT_EPMD_PORT);

    assertThat(node.ping("node-1"))
        .isCompletedWithValue(true);

    assertThat(node.ping("node-2"))
        .isCompletedWithValue(false);

    CompletableFuture<Boolean> future = node.ping("echo@localhost");

    TimeUnit.SECONDS.sleep(3);

    assertThat(future)
        .isCompletedWithValue(true);

    // val node2 = Node.builder()
    //     .name("node-2")
    //     .port(8972)
    //     .build()
    //     .register(epmd.getPort());

    // assertThat(node1.ping("node-2"))
    //     .isCompletedWithValue(true);
    // assertThat(node2.ping("node-1"))
    //     .isCompletedWithValue(true);

    // node1.close();
    // node2.close();
  }

  // @Test
  public void send () {
    node = Node.builder()
        .name("gurka")
        .port(8500)
        .cookie("secret")
        .build()
        .register(DEFAULT_EPMD_PORT);

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
