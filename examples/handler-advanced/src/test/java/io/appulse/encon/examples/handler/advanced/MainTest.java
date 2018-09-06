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

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.list;
import static io.appulse.encon.terms.Erlang.number;
import static io.appulse.encon.terms.Erlang.string;
import static io.appulse.encon.terms.Erlang.tuple;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.mailbox.Mailbox;

import org.junit.Test;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
public class MainTest {

  @Test
  public void test () throws Exception {
    Server server = new Server();
    server.start();

    SECONDS.sleep(2);

    try (Node node = Nodes.singleNode("test@localhost", true)) {
      Mailbox mailbox = node.mailbox()
          .build();

      mailbox.send("java@localhost", "my_process", number(42));
      mailbox.send("java@localhost", "my_process", list());
      mailbox.send("java@localhost", "my_process", tuple(number(4), string("popa"), atom(true)));

      // will be ignored by server rules
      // see MethodMatcherMessageHandler builder at Server class
      mailbox.send("java@localhost", "my_process", number(1));
      mailbox.send("java@localhost", "my_process", number(2));

      SECONDS.sleep(2);
    } finally {
      server.stop();
    }

    assertThat(server.getHistory()).containsSequence(
        "handler3(42)",
        "handler1()",
        "handler2(4, popa, true)"
    );
  }
}
