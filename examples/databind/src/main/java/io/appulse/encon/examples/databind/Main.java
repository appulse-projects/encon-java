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

package io.appulse.encon.examples.databind;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.databind.TermMapper;
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
    Node node = Nodes.singleNode("java@localhost", true);

    Mailbox mailbox = node.mailbox()
        .name("my_process")
        .build();

    // receives an encoded POJO as Erlang term
    ErlangTerm request = mailbox.receive().getBody();
    // parse Erlang term to Pojo.class
    Pojo pojo = TermMapper.deserialize(request, Pojo.class);
    ErlangPid from = pojo.getSender();

    System.out.format("from %s message: %s\n", from, pojo);

    pojo.setSender(mailbox.getPid());
    // serialize object into Erlang term
    ErlangTerm response = TermMapper.serialize(pojo);

    // sends back a pojo
    mailbox.send(from, response);

    // and close the node
    node.close();
  }
}
