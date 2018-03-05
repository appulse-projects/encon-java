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

package io.appulse.encon.java.module.ping;

import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.CompletableFuture;

import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.module.mailbox.ReceiveHandler;
import io.appulse.encon.java.protocol.term.ErlangTerm;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author alabazin
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class PingReceiveHandler implements ReceiveHandler {

  CompletableFuture<Boolean> future;

  @Override
  public void receive (@NonNull Mailbox self, @NonNull ErlangTerm message) {
    log.debug("Incoming message: {}", message);
    future.complete(TRUE);
    self.getNode().remove(self);
  }
}
