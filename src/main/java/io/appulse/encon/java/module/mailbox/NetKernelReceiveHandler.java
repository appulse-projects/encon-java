/*
 * Copyright 2018 Appulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.appulse.encon.java.module.mailbox;

import io.appulse.encon.java.protocol.term.ErlangTerm;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author alabazin
 */
@Slf4j
public class NetKernelReceiveHandler implements ReceiveHandler {

  @Override
  public void receive(Mailbox self, ErlangTerm message) {
    log.debug("Handler working");
    if (!message.isTuple()) {
      log.debug("Not a tuple");
      return;
    }
    if (!message.get(0).map(ErlangTerm::asText).orElse("").equals("$gen_call")) {
      log.debug("Uh?");
      return;
    }

    ErlangTerm tuple = message.get(1).get();
    self.request().makeTuple()
        .add(tuple.get(1).get().asReference())
        .addAtom("yes")
        .send(tuple.get(0).get().asPid());

    log.debug("Ping response was sent to {}", tuple);
  }
}
