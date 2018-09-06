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

package io.appulse.encon.spring;

import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.Config;
import io.appulse.encon.databind.TermMapper;
import io.appulse.encon.handler.mailbox.MailboxHandler;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.spring.EnconAutoConfiguration.MailboxOperationsConfiguration;

import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.AbstractApplicationContext;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@Configuration
@ConditionalOnClass({
    Node.class,
    MailboxHandler.class,
    TermMapper.class
})
@ConditionalOnProperty(
    value = "spring.encon.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@FieldDefaults(level = PRIVATE)
@EnableConfigurationProperties(EnconProperties.class)
@AutoConfigureBefore(MailboxOperationsConfiguration.class)
class EnconAutoConfiguration {

  @Autowired
  EnconProperties enconProperties;

  @Autowired
  AbstractApplicationContext context;

  @Bean
  @ConditionalOnMissingBean
  public Config defaultConfig () {
    return enconProperties.getConfig();
  }

  @Bean
  @ConditionalOnMissingBean
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public Nodes defaultEnconNodes (Config config) {
    val nodes = Nodes.start(config);
    for (Node node : nodes) {
      val descriptor = node.getDescriptor();
      val name = descriptor.getNodeName();

      val beanFactory = context.getBeanFactory();

      // register node as bean
      beanFactory.registerSingleton(name, node);

      // register all mailboxes as beans with name pattern:
      // <node_name>_<mailbox_name>Mailbox
      node.mailboxes()
          .values()
          .stream()
          .filter(it -> it.getName() != null && !it.getName().isEmpty())
          .filter(it -> !"net_kernel".equalsIgnoreCase(it.getName()))
          .forEach(it -> {
            val mailboxName = new StringBuilder()
                .append(name)
                .append('_')
                .append(it.getName())
                .append("Mailbox")
                .toString();

            beanFactory.registerSingleton(mailboxName, it);
          });
    }
    return nodes;
  }

  @Bean
  @Primary
  @ConditionalOnMissingBean
  public Node defaultEnconNode (Nodes nodes) {
    val collection = nodes.nodes();
    return collection.isEmpty()
           ? nodes.newNode("encon")
           : collection.iterator().next();
  }

  @Bean
  public BeanPostProcessorErlangMailboxAnnotation beanPostProcessorErlangMailboxAnnotation (Nodes nodes, Node defaultNode) {
    return new BeanPostProcessorErlangMailboxAnnotation(nodes, defaultNode, context);
  }

  @Bean
  public BeanPostProcessorInjectMailboxAnnotation beanPostProcessorInjectMailboxAnnotation (Nodes nodes, Node defaultNode) {
    return new BeanPostProcessorInjectMailboxAnnotation(nodes, defaultNode, context);
  }

  @Bean
  public BeanPostProcessorMailboxHandler beanPostProcessorMailboxHandler (Nodes nodes, Node defaultNode) {
    return new BeanPostProcessorMailboxHandler(nodes, defaultNode);
  }

  @Configuration
  @ConditionalOnBean(Node.class)
  public static class MailboxOperationsConfiguration {

    public static void registerMailboxOperations (Mailbox mailbox, ConfigurableListableBeanFactory beanFactory) {
      val name = MailboxOperations.generateName(mailbox);
      if (beanFactory.containsBean(name)) {
        return;
      }

      val mailboxOperations = MailboxOperations.builder()
          .self(mailbox)
          .build();

      beanFactory.registerSingleton(name, mailboxOperations);
    }

    @Autowired
    Collection<Node> nodes;

    @Autowired
    AbstractApplicationContext context;

    @PostConstruct
    void postConstruct () {
      nodes.stream()
          .map(Node::mailboxes)
          .map(Map::values)
          .flatMap(Collection::stream)
          .filter(Objects::nonNull)
          .filter(it -> !"net_kernel".equalsIgnoreCase(it.getName()))
          .forEach(this::registerMailboxOperations);
    }

    private void registerMailboxOperations (Mailbox mailbox) {
      registerMailboxOperations(mailbox, context.getBeanFactory());
    }
  }
}
