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

package io.appulse.encon.examples.load.config.spring;


import static io.appulse.encon.common.DistributionFlag.BIG_CREATION;
import static io.appulse.encon.common.DistributionFlag.BIT_BINARIES;
import static io.appulse.encon.common.DistributionFlag.EXTENDED_PIDS_PORTS;
import static io.appulse.encon.common.DistributionFlag.EXTENDED_REFERENCES;
import static io.appulse.encon.common.DistributionFlag.FUN_TAGS;
import static io.appulse.encon.common.DistributionFlag.MAP_TAG;
import static io.appulse.encon.common.DistributionFlag.NEW_FLOATS;
import static io.appulse.encon.common.DistributionFlag.NEW_FUN_TAGS;
import static io.appulse.encon.common.DistributionFlag.UTF8_ATOMS;
import static io.appulse.epmd.java.core.model.NodeType.R3_ERLANG;
import static io.appulse.epmd.java.core.model.NodeType.R3_HIDDEN;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R4;
import static io.appulse.epmd.java.core.model.Version.R5C;
import static io.appulse.epmd.java.core.model.Version.R6;
import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.Node;
import io.appulse.encon.config.Config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class MainTest {

  @Autowired
  Config defaultConfig;

  @Autowired
  ApplicationContext context;

  @Test
  public void test () {
    try {
      assertThat(context.containsBean("node-1")).isTrue();
      assertThat(context.containsBean("node-2")).isTrue();

      assertThat(context.containsBean("node-1_net_kernelMailbox")).isFalse();
      assertThat(context.containsBean("node-1_anotherMailbox")).isTrue();
      assertThat(context.containsBean("node-1_another_oneMailbox")).isTrue();

      assertThat(context.containsBean("node-2_net_kernelMailbox")).isFalse();
    } catch (Throwable ex) {
      log.error("loaded beans: {}", (Object) context.getBeanDefinitionNames());
      throw ex;
    }

    try {
      checkNode1(context.getBean("node-1", Node.class));
      checkNode2(context.getBean("node-2", Node.class));
    } catch (Throwable ex) {
      log.error("loaded config: {}", defaultConfig);
      throw ex;
    }
  }

  private void checkNode1 (Node node) {
    assertThat(node.getPort())
        .isEqualTo(8971);

    assertThat(node.getDescriptor().isShortName())
        .isEqualTo(true);

    assertThat(node.getCookie())
        .isEqualTo("non-secret");

    assertThat(node.getMeta().getType())
        .isEqualTo(R3_HIDDEN);

    assertThat(node.getMeta().getProtocol())
        .isEqualTo(TCP);

    assertThat(node.getMeta().getLow())
        .isEqualTo(R5C);

    assertThat(node.getMeta().getHigh())
        .isEqualTo(R6);

    assertThat(node.getMeta().getFlags()).contains(
        EXTENDED_REFERENCES,
        EXTENDED_PIDS_PORTS,
        BIT_BINARIES,
        NEW_FLOATS,
        FUN_TAGS,
        NEW_FUN_TAGS,
        UTF8_ATOMS,
        MAP_TAG,
        BIG_CREATION
    );


    assertThat(node.mailbox("net_kernel")).isNotNull();
    assertThat(node.mailbox("another")).isNotNull();
    assertThat(node.mailbox("another_one")).isNotNull();
    assertThat(node.mailboxes()).hasSize(3);
  }

  private void checkNode2 (Node node) {

    assertThat(node.getDescriptor().isShortName())
        .isEqualTo(false);

    assertThat(node.getCookie())
        .isEqualTo("popa");

    assertThat(node.getMeta().getType())
        .isEqualTo(R3_ERLANG);

    assertThat(node.getMeta().getProtocol())
        .isEqualTo(TCP);

    assertThat(node.getMeta().getLow())
        .isEqualTo(R4);

    assertThat(node.getMeta().getHigh())
        .isEqualTo(R6);

    assertThat(node.getMeta().getFlags()).contains(
          MAP_TAG,
          BIG_CREATION
    );


    assertThat(node.mailbox("net_kernel")).isNotNull();
    assertThat(node.mailboxes()).hasSize(1);
  }
}
