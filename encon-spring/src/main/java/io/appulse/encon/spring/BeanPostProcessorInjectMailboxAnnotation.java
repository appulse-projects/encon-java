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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.stream.Stream;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.utils.AnnotationUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
class BeanPostProcessorInjectMailboxAnnotation implements BeanPostProcessor, Ordered {

  @NonNull
  Nodes nodes;

  @NonNull
  Node defaultNode;

  @Autowired
  AbstractApplicationContext context;

  @Override
  public int getOrder () {
    return 200;
  }

  @Override
  @SneakyThrows
  public Object postProcessBeforeInitialization (Object bean, String beanName) throws BeansException {
    val annotation = AnnotationUtils.findAnnotation(bean.getClass(), ErlangMailbox.class);
    if (!annotation.isPresent()) {
      return bean;
    }

    val fields = Stream.of(bean.getClass().getDeclaredFields())
        .filter(it -> it.isAnnotationPresent(InjectMailbox.class))
        .peek(it -> {
          if (!it.getType().equals(MailboxOperations.class)) {
            throw new IllegalStateException("Don't put @InjectRandomInt above " + it.getType());
          }
        })
        .peek(it -> {
          if (Modifier.isFinal(it.getModifiers())) {
            throw new IllegalStateException("Can't inject to final fields");
          }
        })
        .toArray(Field[]::new);

    if (fields.length == 0) {
      return bean;
    }

    val node = annotation
        .map(ErlangMailbox::node)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .flatMap(nodes::node)
        .orElse(defaultNode);

    val mailboxOperations = annotation
        .map(ErlangMailbox::name)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(node::mailbox)
        .filter(Objects::nonNull)
        .map(MailboxOperations::generateName)
        .map(context::getBean)
        .orElseGet(() -> {
          val name = MailboxOperations.generateName(node);
          return context.getBean(name);
        });

    for (Field field : fields) {
      AccessController.doPrivileged((PrivilegedAction<?>) () -> {
        field.setAccessible(true);
        return null;
      });
      field.set(bean, mailboxOperations);
    }

    return bean;
  }
}
