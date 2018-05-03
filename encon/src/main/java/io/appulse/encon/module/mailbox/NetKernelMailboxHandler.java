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

package io.appulse.encon.module.mailbox;

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.tuple;

import io.appulse.encon.module.connection.control.ControlMessage;
import io.appulse.encon.terms.ErlangTerm;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
public class NetKernelMailboxHandler implements MailboxHandler {

  @Override
  public void receive (@NonNull Mailbox self, @NonNull ControlMessage header, ErlangTerm body) {
    if (body == null) {
      throw new IllegalArgumentException("Invalid net_kernel call, without body");
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
    self.request()
        .body(tuple(
            tuple.getUnsafe(1).asReference(),
            atom("yes")
        ))
        .send(tuple.getUnsafe(0).asPid());

    log.debug("Ping response was sent");
  }
}
