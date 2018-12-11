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

import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PRIVATE;
import static io.appulse.encon.config.GetPojoTest.Flag.ONE;
import static io.appulse.encon.config.GetPojoTest.Flag.TWO;
import static io.appulse.encon.config.GetPojoTest.Flag.THREE;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.List;
import java.util.Map;

import io.appulse.utils.ResourceUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@Slf4j
@DisplayName("Casting configuration tests")
class GetPojoTest {

  static NodesConfig.NodeConfig.MailboxConfig NODE_1_MAILBOX = NodesConfig.NodeConfig.MailboxConfig.builder()
      .name("popa")
      .build();

  static NodesConfig.NodeConfig NODE_CONFIG_1 = NodesConfig.NodeConfig.builder()
      .shortName(Boolean.TRUE)
      .cookie("secret")
      .clearFlags().flag(ONE).flag(THREE)
      .mailbox("mailbox-1", NodesConfig.NodeConfig.MailboxConfig.builder().build())
      .mailbox("mailbox-2", NODE_1_MAILBOX)
      .build();

  static NodesConfig.NodeConfig NODE_CONFIG_2 = NodesConfig.NodeConfig.builder()
      .epmdPort(1234)
      .clearFlags().flag(TWO)
      .mailbox("mailbox-3", NodesConfig.NodeConfig.MailboxConfig.builder().build())
      .build();

  static NodesConfig NODES_CONFIG = NodesConfig.builder()
      .node("node-1", NODE_CONFIG_1)
      .node("node-2", NODE_CONFIG_2)
      .build();

  static HandlersConfig.MailboxesConfig.MailboxConfig HANDLER_NODE_1_MAILBOX_1 = HandlersConfig.MailboxesConfig.MailboxConfig.builder()
      .handler("handler-1")
      .build();

  static HandlersConfig.MailboxesConfig.MailboxConfig HANDLER_NODE_1_MAILBOX_2 = HandlersConfig.MailboxesConfig.MailboxConfig.builder()
      .handler("handler-2")
      .build();

  static HandlersConfig.MailboxesConfig HANDLER_NODE_1 = HandlersConfig.MailboxesConfig.builder()
      .mailbox("mailbox-1", HANDLER_NODE_1_MAILBOX_1)
      .mailbox("mailbox-2", HANDLER_NODE_1_MAILBOX_2)
      .build();

  static HandlersConfig.MailboxesConfig.MailboxConfig HANDLER_NODE_2_MAILBOX_1 = HandlersConfig.MailboxesConfig.MailboxConfig.builder()
      .handler("handler-3")
      .build();

  static HandlersConfig.MailboxesConfig HANDLER_NODE_2 = HandlersConfig.MailboxesConfig.builder()
      .mailbox("mailbox-3", HANDLER_NODE_2_MAILBOX_1)
      .build();

  static HandlersConfig HANDLER = HandlersConfig.builder()
      .node("node-1", HANDLER_NODE_1)
      .node("node-2", HANDLER_NODE_2)
      .build();

  @Test
  @DisplayName("load YAML configuration")
  void loadYamlTest () {
    URL url = ResourceUtils.getResourceUrls("", "pojo.yml").get(0);
    Config config = Config.load(url);

    validate(config);
  }

  @Test
  @DisplayName("create programmatical configuration")
  void programmaticalTest () {
    Config config = Config.builder()
        .config(NODES_CONFIG)
        .config(HANDLER)
        .build();

    validate(config);
  }

  private void validate (Config config) {
    try {
      assertThat(config.get("nodes.node-1.mailboxes.mailbox-2", NodesConfig.NodeConfig.MailboxConfig.class))
          .hasValue(NODE_1_MAILBOX);

      assertThat(config.get("nodes.node-1", NodesConfig.NodeConfig.class))
          .hasValue(NODE_CONFIG_1);

      assertThat(config.get("nodes.node-2", NodesConfig.NodeConfig.class))
          .hasValue(NODE_CONFIG_2);

      assertThat(config.get(NodesConfig.class))
          .hasValue(NODES_CONFIG);


      assertThat(config.get("nodes.node-1.mailboxes.mailbox-1", HandlersConfig.MailboxesConfig.MailboxConfig.class))
          .hasValue(HANDLER_NODE_1_MAILBOX_1);

      assertThat(config.get("nodes.node-1.mailboxes.mailbox-2", HandlersConfig.MailboxesConfig.MailboxConfig.class))
          .hasValue(HANDLER_NODE_1_MAILBOX_2);

      assertThat(config.get("nodes.node-2.mailboxes.mailbox-3", HandlersConfig.MailboxesConfig.MailboxConfig.class))
          .hasValue(HANDLER_NODE_2_MAILBOX_1);

      assertThat(config.get("nodes.node-1", HandlersConfig.MailboxesConfig.class))
          .hasValue(HANDLER_NODE_1);

      assertThat(config.get("nodes.node-2", HandlersConfig.MailboxesConfig.class))
          .hasValue(HANDLER_NODE_2);

      assertThat(config.get(HandlersConfig.class))
          .hasValue(HANDLER);
    } catch (AssertionError ex) {
      log.error("configuration\n{}", config);
      throw ex;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = PRIVATE)
  public static class HandlersConfig {

    @Singular
    Map<String, MailboxesConfig> nodes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = PRIVATE)
    public static class MailboxesConfig {

      @Singular
      Map<String, MailboxConfig> mailboxes;

      @Data
      @Builder
      @NoArgsConstructor
      @AllArgsConstructor
      @FieldDefaults(level = PRIVATE)
      public static class MailboxConfig {

        String handler;
      }
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = PRIVATE)
  public static class NodesConfig {

    @Singular
    Map<String, NodeConfig> nodes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = PRIVATE)
    public static class NodeConfig {

      @Builder.Default
      Integer epmdPort = 6435;

      @Builder.Default
      Boolean shortName = FALSE;

      @Builder.Default
      String cookie = "";

      @Singular
      List<Flag> flags;

      @Singular
      Map<String, MailboxConfig> mailboxes;

      @Data
      @Builder
      @NoArgsConstructor
      @AllArgsConstructor
      @FieldDefaults(level = PRIVATE)
      public static class MailboxConfig {

        String name;
      }


    }
  }

  public enum Flag {

    ONE,
    TWO,
    THREE;
  }
}
