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

import static io.appulse.encon.spring.EnconAutoConfiguration.MailboxOperationsConfiguration.registerMailboxOperations;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import java.util.Optional;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.utils.AnnotationUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class BeanPostProcessorErlangMailboxAnnotation implements BeanPostProcessor, Ordered {

  @NonNull
  Nodes nodes;

  @NonNull
  Node defaultNode;

  @Autowired
  AbstractApplicationContext context;

  @Override
  public int getOrder () {
    return 100;
  }

  @Override
  public Object postProcessBeforeInitialization (Object bean, String beanName) throws BeansException {
    Optional<ErlangMailbox> annotation = AnnotationUtils.findAnnotation(bean.getClass(), ErlangMailbox.class);
    if (!annotation.isPresent()) {
      return bean;
    }

    Node node = annotation
        .map(ErlangMailbox::node)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(it -> nodes.node(it)
            .orElseGet(() -> nodes.newNode(it))
        )
        .map(this::registerIfNotExist)
        .orElse(defaultNode);

    val mailbox = annotation
        .map(ErlangMailbox::name)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(it -> ofNullable(node.mailbox(it))
            .orElseGet(() -> node.mailbox()
                .name(it)
                .build()
            )
        )
        .map(this::registerIfNotExist)
        .orElseGet(() -> createAndRegisterNewMailbox(node));

    registerMailboxOperations(mailbox, context.getBeanFactory());
    return bean;
  }

  private Node registerIfNotExist (Node node) {
    return registerIfNotExist(node.getDescriptor().getNodeName(), node);
  }

  private Mailbox registerIfNotExist (Mailbox mailbox) {
    return registerIfNotExist(mailbox.getName(), mailbox);
  }

  private <T> T registerIfNotExist (String name, T bean) {
    val beanFactory = context.getBeanFactory();
    if (!beanFactory.containsBean(name)) {
      beanFactory.registerSingleton(name, bean);
    }
    return bean;
  }

  private Mailbox createAndRegisterNewMailbox (Node node) {
    val mailboxBeanName = new StringBuilder()
        .append(node.getDescriptor().getNodeName())
        .append("Mailbox")
        .toString();

    val beanFactory = context.getBeanFactory();
    if (beanFactory.containsBean(mailboxBeanName)) {
      throw new IllegalArgumentException();
    }

    val newMailbox = node.mailbox().build();
    beanFactory.registerSingleton(mailboxBeanName, newMailbox);
    return newMailbox;
  }
}
