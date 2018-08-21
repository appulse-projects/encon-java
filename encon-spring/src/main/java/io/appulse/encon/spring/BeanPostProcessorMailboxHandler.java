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

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.handler.mailbox.DefaultMailboxHandler;
import io.appulse.encon.handler.message.MessageHandler;
import io.appulse.encon.handler.message.matcher.MethodMatcherMessageHandler;
import io.appulse.encon.handler.message.matcher.MethodMatcherMessageHandlerBuilder.ConsumerWithException;
import io.appulse.utils.AnnotationUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class BeanPostProcessorMailboxHandler implements BeanPostProcessor, Ordered {

  @NonNull
  Nodes nodes;

  @NonNull
  Node defaultNode;

  @Override
  public int getOrder () {
    return 300;
  }

  @Override
  public Object postProcessBeforeInitialization (Object bean, String beanName) throws BeansException {
    val annotation = AnnotationUtils.findAnnotation(bean.getClass(), ErlangMailbox.class);
    if (!annotation.isPresent()) {
      return bean;
    }

    val node = annotation
        .map(ErlangMailbox::node)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .flatMap(nodes::node)
        .orElse(defaultNode);

    val mailbox = annotation
        .map(ErlangMailbox::name)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(node::mailbox)
        .filter(Objects::nonNull)
        .orElseGet(() -> {
          val mailboxes = node.mailboxes().values();
          val allowedMAilboxCountForChoosingDefaultOne = 2; // 2 is a user mailbox + net_kernel
          if (mailboxes.size() > allowedMAilboxCountForChoosingDefaultOne) {
            throw new IllegalStateException("Too many mailboxes in node " + node.getDescriptor().getNodeName() + ". "
                                            + "Can't choose default one");
          }
          return mailboxes.iterator().next();
        });

    List<Method> methods = Stream.of(ReflectionUtils.getAllDeclaredMethods(bean.getClass()))
        .filter(it -> it.isAnnotationPresent(MatchingCaseMapping.class))
        .collect(toList());

    if (!methods.isEmpty()) {
      DefaultMailboxHandler.builder()
          .mailbox(mailbox)
          .messageHandler(createMessageHandler(bean, methods))
          .build()
          .startExecutor();
    }
    return bean;
  }

  private MessageHandler createMessageHandler (Object object, List<Method> methodDescriptors) {
    val result = MethodMatcherMessageHandler.builder();
    val wrapped = result.wrap(object);

    methodDescriptors.stream()
        .map(ControllerMethodDescriptor::of)
        .forEach(it -> {
          ConsumerWithException<Object> consumer = obj -> {
            Object[] args = it.getMethod().getParameterCount() == 1
                            ? new Object[] { it.getPatternArguments()[0] }
                            : it.getPatternArguments();

            ReflectionUtils.invokeMethod(it.getMethod(), obj, args);
          };

          switch (it.getWrapper()) {
          case TUPLE:
            wrapped.tuple(consumer);
            break;
          case LIST:
            wrapped.list(consumer);
            break;
          case NONE:
          default:
            wrapped.none(consumer);
          }
        });

    val messageHandler = result.build();
    log.debug("message handler map\n  {}", messageHandler.getMap());
    return messageHandler;
  }
}
