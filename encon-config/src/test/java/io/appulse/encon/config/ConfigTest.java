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

package io.appulse.encon.config;

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
import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.SCTP;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Protocol.UDP;
import static io.appulse.epmd.java.core.model.Version.R3;
import static io.appulse.epmd.java.core.model.Version.R4;
import static io.appulse.epmd.java.core.model.Version.R5C;
import static io.appulse.epmd.java.core.model.Version.R6;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.appulse.encon.common.DistributionFlag;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;
import io.appulse.utils.test.TestMethodNamePrinter;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class ConfigTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void defaultDefaults () {
    Config config = Config.builder()
        .build();

    Defaults defaults = config.getDefaults();
    assertThat(defaults)
        .isNotNull();

    assertThat(config.getNodes())
        .isEmpty();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(defaults.getEpmdPort())
          .isEqualTo(EpmdClient.Default.PORT);

      softly.assertThat(defaults.getType())
          .isEqualTo(R6_ERLANG);

      softly.assertThat(defaults.getShortName())
          .isFalse();

      softly.assertThat(defaults.getProtocol())
          .isEqualTo(TCP);

      softly.assertThat(defaults.getLowVersion())
          .isEqualTo(R6);

      softly.assertThat(defaults.getHighVersion())
          .isEqualTo(R6);

      softly.assertThat(defaults.getDistributionFlags())
          .isNotEmpty();

      softly.assertThat(defaults.getMailbox())
          .isNotNull();

      softly.assertThat(defaults.getServer())
          .isNotNull();

      softly.assertThat(defaults.getServer().getPort())
          .isNull();

      softly.assertThat(defaults.getServer().getBossThreads())
          .isEqualTo(1);

      softly.assertThat(defaults.getServer().getWorkerThreads())
          .isEqualTo(2);
    });
  }

  @Test
  public void nonDefaultDefaults () {
    int epmdPort = 4321;
    NodeType type = R3_HIDDEN;
    Protocol protocol = UDP;
    Version low = R3;
    Version high = R5C;
    Set<DistributionFlag> distributionFlags = new HashSet<>(asList(
        UTF8_ATOMS
    ));
    int bossThreads = 7;
    int workerThreads = 14;

    Config config = Config.builder()
        .defaults(Defaults.builder()
            .epmdPort(epmdPort)
            .type(type)
            .protocol(protocol)
            .lowVersion(low)
            .highVersion(high)
            .distributionFlags(distributionFlags)
            .mailbox(MailboxConfig.builder()
                .build())
            .server(ServerConfig.builder()
                .bossThreads(bossThreads)
                .workerThreads(workerThreads)
                .build())
            .build())
        .build();

    Defaults defaults = config.getDefaults();
    assertThat(defaults).isNotNull();

    assertThat(config.getNodes())
        .isEmpty();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(defaults.getEpmdPort())
          .isEqualTo(epmdPort);

      softly.assertThat(defaults.getType())
          .isEqualTo(type);

      softly.assertThat(defaults.getShortName())
          .isFalse();

      softly.assertThat(defaults.getProtocol())
          .isEqualTo(protocol);

      softly.assertThat(defaults.getLowVersion())
          .isEqualTo(low);

      softly.assertThat(defaults.getHighVersion())
          .isEqualTo(high);

      softly.assertThat(defaults.getDistributionFlags())
          .isEqualTo(distributionFlags);

      softly.assertThat(defaults.getMailbox())
          .isNotNull();

      softly.assertThat(defaults.getServer())
          .isNotNull();

      softly.assertThat(defaults.getServer().getPort())
          .isNull();

      softly.assertThat(defaults.getServer().getBossThreads())
          .isEqualTo(bossThreads);

      softly.assertThat(defaults.getServer().getWorkerThreads())
          .isEqualTo(workerThreads);
    });
  }

  @Test
  public void nodeWithDefaults () {
    Config config = Config.builder()
        .node("popa", NodeConfig.builder().build())
        .build();

    NodeConfig nodeConfig = config.getNodes().get("popa");
    assertThat(nodeConfig).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(nodeConfig.getEpmdPort())
          .isEqualTo(EpmdClient.Default.PORT);

      softly.assertThat(nodeConfig.getType())
          .isEqualTo(R6_ERLANG);

      softly.assertThat(nodeConfig.getShortName())
          .isFalse();

      softly.assertThat(nodeConfig.getProtocol())
          .isEqualTo(TCP);

      softly.assertThat(nodeConfig.getLowVersion())
          .isEqualTo(R6);

      softly.assertThat(nodeConfig.getHighVersion())
          .isEqualTo(R6);

      softly.assertThat(nodeConfig.getDistributionFlags())
          .isNotEmpty();

      softly.assertThat(nodeConfig.getMailboxes())
          .isEmpty();

      softly.assertThat(nodeConfig.getServer())
          .isNotNull();

      softly.assertThat(nodeConfig.getServer().getPort())
          .isNotNull();

      softly.assertThat(nodeConfig.getServer().getBossThreads())
          .isEqualTo(1);

      softly.assertThat(nodeConfig.getServer().getWorkerThreads())
          .isEqualTo(2);
    });
  }

  @Test
  public void load () {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("connector.yml").getFile());

    Config config = Config.load(file);

    Defaults defaults = config.getDefaults();
    assertThat(defaults)
        .isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(defaults.getEpmdPort())
          .isEqualTo(8888);

      softly.assertThat(defaults.getType())
          .isEqualTo(R3_ERLANG);

      softly.assertThat(defaults.getShortName())
          .isTrue();

      softly.assertThat(defaults.getCookie())
          .isEqualTo("secret");

      softly.assertThat(defaults.getProtocol())
          .isEqualTo(UDP);

      softly.assertThat(defaults.getLowVersion())
          .isEqualTo(R4);

      softly.assertThat(defaults.getHighVersion())
          .isEqualTo(R6);

      softly.assertThat(defaults.getDistributionFlags())
          .contains(MAP_TAG, BIG_CREATION);

      softly.assertThat(defaults.getServer())
          .isNotNull();

      softly.assertThat(defaults.getServer().getPort())
          .isNull();

      softly.assertThat(defaults.getServer().getBossThreads())
          .isEqualTo(2);

      softly.assertThat(defaults.getServer().getWorkerThreads())
          .isEqualTo(4);
    });

    Map<String, NodeConfig> nodes = config.getNodes();
    assertThat(nodes).isNotNull();

    NodeConfig node1 = nodes.get("node-1");
    assertThat(node1).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(node1.getEpmdPort())
          .isEqualTo(7373);

      softly.assertThat(node1.getType())
          .isEqualTo(R3_HIDDEN);

      softly.assertThat(node1.getShortName())
          .isTrue();

      softly.assertThat(node1.getCookie())
          .isEqualTo("non-secret");

      softly.assertThat(node1.getProtocol())
          .isEqualTo(SCTP);

      softly.assertThat(node1.getLowVersion())
          .isEqualTo(R5C);

      softly.assertThat(node1.getHighVersion())
          .isEqualTo(R6);

      softly.assertThat(node1.getDistributionFlags()).contains(
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

      softly.assertThat(node1.getMailboxes())
          .isNotEmpty();

      MailboxConfig mailbox1 = node1.getMailboxes()
          .stream()
          .filter(it -> "net_kernel".equals(it.getName()))
          .findFirst()
          .orElse(null);
      assertThat(mailbox1).isNotNull();

      MailboxConfig mailbox2 = node1.getMailboxes()
          .stream()
          .filter(it -> "another".equals(it.getName()))
          .findFirst()
          .orElse(null);
      assertThat(mailbox2).isNotNull();

      MailboxConfig mailbox3 = node1.getMailboxes()
          .stream()
          .filter(it -> "another_one".equals(it.getName()))
          .findFirst()
          .orElse(null);
      assertThat(mailbox3).isNotNull();

      softly.assertThat(node1.getServer())
          .isNotNull();

      softly.assertThat(node1.getServer().getPort())
          .isEqualTo(8971);

      softly.assertThat(node1.getServer().getBossThreads())
          .isEqualTo(1);

      softly.assertThat(node1.getServer().getWorkerThreads())
          .isEqualTo(2);
    });

    NodeConfig node2 = nodes.get("node-2");
    assertThat(node2).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(node2.getEpmdPort())
          .isEqualTo(8888);

      softly.assertThat(node2.getType())
          .isEqualTo(R3_ERLANG);

      softly.assertThat(node2.getShortName())
          .isFalse();

      softly.assertThat(node2.getCookie())
          .isEqualTo("popa");

      softly.assertThat(node2.getProtocol())
          .isEqualTo(UDP);

      softly.assertThat(node2.getLowVersion())
          .isEqualTo(R4);

      softly.assertThat(node2.getHighVersion())
          .isEqualTo(R6);

      softly.assertThat(node2.getDistributionFlags()).contains(
          MAP_TAG,
          BIG_CREATION
      );

      softly.assertThat(node2.getMailboxes())
          .isNotEmpty();

      MailboxConfig mailbox1 = node2.getMailboxes()
          .stream()
          .filter(it -> "net_kernel".equals(it.getName()))
          .findFirst()
          .orElse(null);
      assertThat(mailbox1).isNotNull();

      softly.assertThat(node2.getServer())
          .isNotNull();

      softly.assertThat(node2.getServer().getPort())
          .isNotNull();

      softly.assertThat(node2.getServer().getBossThreads())
          .isEqualTo(2);

      softly.assertThat(node2.getServer().getWorkerThreads())
          .isEqualTo(4);
    });
  }

  @Test
  public void manual () {
    Config config = Config.builder()
        .defaults(Defaults.builder()
            .epmdPort(8888)
            .type(R3_ERLANG)
            .shortName(true)
            .cookie("secret")
            .protocol(UDP)
            .lowVersion(R4)
            .highVersion(R6)
            .distributionFlags(new HashSet<>(asList(
                MAP_TAG,
                BIG_CREATION
            )))
            .mailbox(MailboxConfig.builder()
                .build())
            .server(ServerConfig.builder()
                .bossThreads(2)
                .workerThreads(4)
                .build())
            .build()
        )
        .node("node-1", NodeConfig.builder()
            .epmdPort(7373)
            .type(R3_HIDDEN)
            .shortName(false)
            .cookie("non-secret")
            .protocol(SCTP)
            .lowVersion(R5C)
            .highVersion(R6)
            .distributionFlag(EXTENDED_REFERENCES)
            .distributionFlag(EXTENDED_PIDS_PORTS)
            .distributionFlag(BIT_BINARIES)
            .distributionFlag(NEW_FLOATS)
            .distributionFlag(FUN_TAGS)
            .distributionFlag(NEW_FUN_TAGS)
            .distributionFlag(UTF8_ATOMS)
            .distributionFlag(MAP_TAG)
            .distributionFlag(BIG_CREATION)
            .mailbox(MailboxConfig.builder()
                .name("net_kernel")
                .build())
            .mailbox(MailboxConfig.builder()
                .build())
            .mailbox(MailboxConfig.builder()
                .name("another")
                .build())
            .server(ServerConfig.builder()
                .port(8971)
                .bossThreads(1)
                .workerThreads(2)
                .build())
            .build()
        )
        .node("node-2", NodeConfig.builder()
            .cookie("popa")
            .mailbox(MailboxConfig.builder()
                .name("net_kernel")
                .build())
            .build()
        )
        .build();


    Defaults defaults = config.getDefaults();
    assertThat(defaults)
        .isNotNull();

    SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(defaults.getEpmdPort())
            .isEqualTo(8888);

        softly.assertThat(defaults.getType())
            .isEqualTo(R3_ERLANG);

        softly.assertThat(defaults.getShortName())
            .isTrue();

        softly.assertThat(defaults.getCookie())
            .isEqualTo("secret");

        softly.assertThat(defaults.getProtocol())
            .isEqualTo(UDP);

        softly.assertThat(defaults.getLowVersion())
            .isEqualTo(R4);

        softly.assertThat(defaults.getHighVersion())
            .isEqualTo(R6);

        softly.assertThat(defaults.getDistributionFlags())
            .contains(MAP_TAG, BIG_CREATION);

        softly.assertThat(defaults.getMailbox())
            .isNotNull();

        softly.assertThat(defaults.getServer())
            .isNotNull();

        softly.assertThat(defaults.getServer().getPort())
            .isNull();

        softly.assertThat(defaults.getServer().getBossThreads())
            .isEqualTo(2);

        softly.assertThat(defaults.getServer().getWorkerThreads())
            .isEqualTo(4);
    });

    Map<String, NodeConfig> nodes = config.getNodes();
    assertThat(nodes).isNotNull();

    NodeConfig node1 = nodes.get("node-1");
    assertThat(node1).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(node1.getEpmdPort())
            .isEqualTo(7373);

        softly.assertThat(node1.getType())
            .isEqualTo(R3_HIDDEN);

        softly.assertThat(node1.getShortName())
            .isFalse();

        softly.assertThat(node1.getCookie())
            .isEqualTo("non-secret");

        softly.assertThat(node1.getProtocol())
            .isEqualTo(SCTP);

        softly.assertThat(node1.getLowVersion())
            .isEqualTo(R5C);

        softly.assertThat(node1.getHighVersion())
            .isEqualTo(R6);

        softly.assertThat(node1.getDistributionFlags()).contains(
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

        softly.assertThat(node1.getMailboxes())
            .isNotEmpty();

        MailboxConfig mailbox1 = node1.getMailboxes()
            .stream()
            .filter(it -> "net_kernel".equals(it.getName()))
            .findFirst()
            .orElse(null);
        assertThat(mailbox1).isNotNull();

        MailboxConfig mailbox2 = node1.getMailboxes()
            .stream()
            .filter(it -> it.getName() == null)
            .findFirst()
            .orElse(null);
        assertThat(mailbox2).isNotNull();

        MailboxConfig mailbox3 = node1.getMailboxes()
            .stream()
            .filter(it -> "another".equals(it.getName()))
            .findFirst()
            .orElse(null);
        assertThat(mailbox3).isNotNull();

        softly.assertThat(node1.getServer())
            .isNotNull();

        softly.assertThat(node1.getServer().getPort())
            .isEqualTo(8971);

        softly.assertThat(node1.getServer().getBossThreads())
            .isEqualTo(1);

        softly.assertThat(node1.getServer().getWorkerThreads())
            .isEqualTo(2);
    });

    NodeConfig node2 = nodes.get("node-2");
    assertThat(node2).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(node2.getEpmdPort())
            .isEqualTo(8888);

        softly.assertThat(node2.getType())
            .isEqualTo(R3_ERLANG);

        softly.assertThat(node2.getShortName())
            .isTrue();

        softly.assertThat(node2.getCookie())
            .isEqualTo("popa");

        softly.assertThat(node2.getProtocol())
            .isEqualTo(UDP);

        softly.assertThat(node2.getLowVersion())
            .isEqualTo(R4);

        softly.assertThat(node2.getHighVersion())
            .isEqualTo(R6);

        softly.assertThat(node2.getDistributionFlags()).contains(
            MAP_TAG,
            BIG_CREATION
        );

        softly.assertThat(node2.getMailboxes())
            .isNotEmpty();

        MailboxConfig mailbox1 = node2.getMailboxes()
            .stream()
            .filter(it -> "net_kernel".equals(it.getName()))
            .findFirst()
            .orElse(null);
        assertThat(mailbox1).isNotNull();

        softly.assertThat(node2.getServer())
            .isNotNull();

        softly.assertThat(node2.getServer().getPort())
            .isNotNull();

        softly.assertThat(node2.getServer().getBossThreads())
            .isEqualTo(2);

        softly.assertThat(node2.getServer().getWorkerThreads())
            .isEqualTo(4);
    });
  }
}
