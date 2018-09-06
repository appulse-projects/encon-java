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

import static io.appulse.encon.terms.Erlang.tuple;
import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.databind.TermMapper;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;
import java.io.Closeable;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
class EchoClientNode implements Closeable {

  Node node;

  Mailbox mailbox;

  EchoClientNode (String cookie) {
    NodeConfig config = NodeConfig.builder()
        .shortName(TRUE)
        .cookie(cookie)
        .build();

    node = Nodes.singleNode("client", config);

    mailbox = node.mailbox().build();
  }

  @Override
  public void close () {
    node.close();
  }

  void send (String remoteNode, String remoteMailbox, ErlangTerm term) {
    ErlangTerm tuple = tuple(mailbox.getPid(), term);
    mailbox.send(remoteNode, remoteMailbox, tuple);
  }

  void send (String remoteNode, String remoteMailbox, Object payload) {
    ErlangTerm term = TermMapper.serialize(payload);
    send(remoteNode, remoteMailbox, term);
  }

  ErlangTerm receive () {
    return mailbox.receive().getBody();
  }

  <T> T receive (Class<T> type) {
    ErlangTerm term = receive();
    return TermMapper.deserialize(term, type);
  }
}
