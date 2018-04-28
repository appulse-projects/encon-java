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

package io.appulse.encon.java.module.mailbox;

import static io.appulse.encon.java.protocol.Erlang.atom;
import static io.appulse.encon.java.protocol.Erlang.tuple;

import java.util.Optional;

import io.appulse.encon.java.module.connection.control.ControlMessage;
import io.appulse.encon.java.protocol.term.ErlangTerm;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
public class NetKernelMailboxHandler implements MailboxHandler {

  @Override
  public void receive (@NonNull Mailbox self, @NonNull ControlMessage header, Optional<ErlangTerm> body) {
    val payload = body.orElseThrow(() -> new IllegalArgumentException("Invalid net_kernel call, without body"));

    if (!payload.isTuple()) {
      log.debug("Not a tuple");
      return;
    }
    if (!payload.get(0).map(ErlangTerm::asText).orElse("").equals("$gen_call")) {
      log.debug("Uh?");
      return;
    }

    ErlangTerm tuple = payload.get(1).get();
    self.request()
        .body(tuple(
            tuple.get(1).get().asReference(),
            atom("yes")
        ))
        .send(tuple.get(0).get().asPid());

    log.debug("Ping response was sent");
  }
}
