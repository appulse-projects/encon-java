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
import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R6;

import io.appulse.encon.java.util.TestEpmdServer;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import lombok.val;

public class NodeTest {

  TestEpmdServer epmd;

  @Before
  public void before () {
    epmd = new TestEpmdServer();
    epmd.start();
  }

  @After
  public void after () {
    epmd.stop();
    epmd = null;
  }

  @Test
  public void register () {
    val node = Node.builder()
        .name("popa")
        .port(8971)
        .build()
        .register(epmd.getPort());

    assertThat(node.isRegistered()).isTrue();

    val optional = epmd.lookup("popa");
    assertThat(optional).isPresent();

    val nodeInfo = optional.get();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(nodeInfo.getPort())
          .isPresent()
          .hasValue(8971);

      softly.assertThat(nodeInfo.getType())
          .isPresent()
          .hasValue(R6_ERLANG);

      softly.assertThat(nodeInfo.getProtocol())
          .isPresent()
          .hasValue(TCP);

      softly.assertThat(nodeInfo.getHigh())
          .isPresent()
          .hasValue(R6);

      softly.assertThat(nodeInfo.getLow())
          .isPresent()
          .hasValue(R6);
    });

    node.close();
    assertThat(node.isRegistered()).isFalse();
  }
}
