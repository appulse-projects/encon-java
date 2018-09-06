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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.databind.TermMapper;
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

      Pojo request = new Pojo(
          mailbox.getPid(),
          "Artem",
          27,
          true,
          543,
          asList("java", "nim", "elixir"),
          "developer",
          singleton("popa"),
          "some long string...or not",
          new Boolean[] { true, false, true }
      );
      ErlangTerm requestTerm = TermMapper.serialize(request);

      mailbox.send("java@localhost", "my_process", requestTerm);


      ErlangTerm responseTerm = mailbox.receive(20, SECONDS).getBody();
      Pojo response = TermMapper.deserialize(responseTerm, Pojo.class);

      assertThat(response).isEqualTo(request);
    }
  }
}
