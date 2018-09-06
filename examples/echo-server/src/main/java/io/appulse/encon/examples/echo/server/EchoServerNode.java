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

package io.appulse.encon.examples.echo.server;

import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Optional;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
class EchoServerNode implements Runnable, Closeable {

  Node node;

  Mailbox mailbox;

  EchoServerNode (String nodeName, String cookie, String mailboxName) {
    NodeConfig config = NodeConfig.builder()
        .shortName(TRUE)
        .cookie(cookie)
        .build();

    node = Nodes.singleNode(nodeName, config);

    mailbox = node.mailbox()
        .name(mailboxName)
        .build();
  }

  @Override
  public void run () {
    ErlangPid myPid = mailbox.getPid();

    while (!Thread.interrupted()) {
      Message message = mailbox.receive();
      ErlangTerm body = message.getBody();

      log.info("New message {}", body);

      Optional<ErlangPid> remotePid = body.get(0)
          .map(ErlangTerm::asPid);

      if (!remotePid.isPresent()) {
        log.error("There is no remote PID");
        continue;
      }

      ErlangTerm payload = body.getUnsafe(1);

      mailbox.send(remotePid.get(), payload);
    }
  }

  @Override
  public void close () {
    node.close();
  }
}
