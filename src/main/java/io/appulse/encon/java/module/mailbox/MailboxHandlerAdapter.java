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

import static java.util.Optional.ofNullable;

import io.appulse.encon.java.module.connection.control.ControlMessage;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import java.util.Optional;
import lombok.NonNull;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 29.04.2018
 */
public abstract class MailboxHandlerAdapter implements MailboxHandler {

  @Override
  public void receive (@NonNull Mailbox self, @NonNull ControlMessage header, ErlangTerm body) {
    val optionalBody = ofNullable(body);
    receive(self, header, optionalBody);
  }

  public abstract void receive (Mailbox self, ControlMessage header, Optional<ErlangTerm> body);
}
