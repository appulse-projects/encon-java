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

package io.appulse.encon.examples.handler.advanced;

import static io.appulse.encon.handler.message.matcher.Matchers.anyInt;
import static io.appulse.encon.handler.message.matcher.Matchers.anyString;
import static io.appulse.encon.handler.message.matcher.Matchers.eq;
import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedList;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.handler.mailbox.DefaultMailboxHandler;
import io.appulse.encon.handler.mailbox.MailboxHandler;
import io.appulse.encon.handler.message.MessageHandler;
import io.appulse.encon.handler.message.matcher.MethodMatcherMessageHandler;
import io.appulse.encon.mailbox.Mailbox;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE)
public class Server {

  @Getter
  final LinkedList<String> history = new LinkedList<>();

  Node node;

  MailboxHandler mailboxHandler;

  public void start () {
    node = Nodes.singleNode("java@localhost", true);
    Mailbox mailbox = node.mailbox()
        .name("my_process")
        .build();

    MyService1 service1 = new MyService1(history);
    MyService2 service2 = new MyService2(history);

    MessageHandler messageHandler = MethodMatcherMessageHandler.builder()
        .wrap(service1)
            // redirects '[]' (empty list) to method MyService1.handler1
            .list(it -> it.handler1())
            // redirects tuple {any_number, any_string, atom(true)}
            // to MyService1.handler2
            .tuple(it -> it.handler2(anyInt(), anyString(), eq(true)))
        .wrap(service2)
            // redirects {42} to MyService2.popa
            .none(it -> it.handler3(42))
        .build();

    mailboxHandler = DefaultMailboxHandler.builder()
      .messageHandler(messageHandler)
      .mailbox(mailbox)
      .build();

    mailboxHandler.startExecutor();
  }

  public void stop () {
    mailboxHandler.close();
    node.close();
  }
}
