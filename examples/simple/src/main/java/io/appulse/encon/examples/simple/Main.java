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

package io.appulse.encon.examples.simple;

import static io.appulse.encon.terms.Erlang.string;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
public class Main {

  public static void main(String[] args) {
    NodeConfig config = NodeConfig.builder()
        .shortName(true)
        .build();

    Node node = Nodes.singleNode("java@localhost", config);

    Mailbox mailbox = node.mailbox()
        .name("my_process")
        .build();

    // receives a tuple {pid, string}
    ErlangTerm payload = mailbox.receive().getBody();

    ErlangPid from = payload.get(0)
        .filter(ErlangTerm::isPid)
        .map(ErlangTerm::asPid)
        .orElseThrow(() -> new RuntimeException("Expected first element is PID"));

    String text = payload.get(1)
        .filter(ErlangTerm::isTextual)
        .map(ErlangTerm::asText)
        .orElseThrow(() -> new RuntimeException("Expected second element is string"));

    System.out.format("from %s message: %s\n", from, text);

    // sends back a string
    mailbox.send(from, string("hello world"));

    // and close the node
    node.close();
  }
}
