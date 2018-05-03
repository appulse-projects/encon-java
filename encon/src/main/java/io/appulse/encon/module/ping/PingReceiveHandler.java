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

package io.appulse.encon.module.ping;

import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.CompletableFuture;

import io.appulse.encon.module.connection.control.ControlMessage;
import io.appulse.encon.module.mailbox.Mailbox;
import io.appulse.encon.module.mailbox.MailboxHandler;
import io.appulse.encon.terms.ErlangTerm;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class PingReceiveHandler implements MailboxHandler {

  CompletableFuture<Boolean> future;

  @Override
  public void receive (@NonNull Mailbox self, @NonNull ControlMessage header, ErlangTerm body) {
    future.complete(TRUE);
    self.getNode().remove(self);
  }
}
