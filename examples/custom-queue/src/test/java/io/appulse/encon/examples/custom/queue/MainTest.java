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

package io.appulse.encon.examples.custom.queue;

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.tuple;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;

import org.junit.Test;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
public class MainTest {

  @Test
  public void test () throws Exception {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.execute(() -> Main.main(null));

    SECONDS.sleep(2);

    try (Node node = Nodes.singleNode("test@localhost", true)) {
      Mailbox mailbox = node.mailbox()
          .build();

      mailbox.send("java@localhost", "my_process", tuple(mailbox.getPid(), atom("popa")));


      ErlangTerm payload = mailbox.receive(20, SECONDS).getBody();

      assertThat(payload.asText()).isEqualTo("hello world");
    }
  }
}
