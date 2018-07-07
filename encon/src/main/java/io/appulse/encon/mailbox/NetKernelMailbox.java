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

package io.appulse.encon.mailbox;

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.tuple;

import java.util.concurrent.SynchronousQueue;

import io.appulse.encon.Node;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.2.0
 * @author Artem Labazin
 */
@Slf4j
class NetKernelMailbox extends Mailbox {

  NetKernelMailbox (Node node, ErlangPid pid) {
    super(null, node, pid, new SynchronousQueue<>());
  }

  @Override
  public void deliver (Message message) {
    val body = message.getBody();
    if (body == null) {
      log.error("Invalid net_kernel call, without body");
      return;
    }

    if (!body.isTuple()) {
      log.debug("Not a tuple");
      return;
    }
    if (!body.getUnsafe(0).asText().equals("$gen_call")) {
      log.debug("Uh?");
      return;
    }

    ErlangTerm tuple = body.getUnsafe(1);
    send(tuple.getUnsafe(0).asPid(), tuple(
         tuple.getUnsafe(1).asReference(),
         atom("yes")
     ));

    log.debug("Ping response was sent");
  }
}
