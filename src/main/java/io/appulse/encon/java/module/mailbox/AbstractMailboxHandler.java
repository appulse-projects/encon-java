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

import java.util.Optional;

import io.appulse.encon.java.module.connection.control.ControlMessage;
import io.appulse.encon.java.module.connection.control.Exit;
import io.appulse.encon.java.module.connection.control.Link;
import io.appulse.encon.java.module.connection.control.Unlink;
import io.appulse.encon.java.protocol.term.ErlangTerm;

import lombok.NonNull;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public abstract class AbstractMailboxHandler implements MailboxHandler {

  @Override
  public void receive (@NonNull Mailbox self, @NonNull ControlMessage header, Optional<ErlangTerm> body) {
    switch (header.getTag()) {
    case LINK:
      handle(self, (Link) header);
      return;
    case UNLINK:
      handle(self, (Unlink) header);
      return;
    case EXIT:
      handle(self, (Exit) header);
    default:
      handle(self, header, body);
    }
  }

  protected abstract void handle (Mailbox self, ControlMessage header, Optional<ErlangTerm> body);

  /**
   * Handles link requests.
   *
   * @param self reference to this mailbox
   *
   * @param header received Link control message
   */
  protected void handle (@NonNull Mailbox self, @NonNull Link header) {
    val pidFrom = header.getFrom();
    self.getLinks().add(pidFrom);
  }

  /**
   * Handles unlink requests.
   *
   * @param self reference to this mailbox
   *
   * @param header received Unlink control message
   */
  protected void handle (@NonNull Mailbox self, @NonNull Unlink header) {
    val pidFrom = header.getFrom();
    self.getLinks().remove(pidFrom);
  }

  /**
   * Handles exit requests.
   *
   * @param self reference to this mailbox
   *
   * @param header received Exit control message
   */
  protected void handle (@NonNull Mailbox self, @NonNull Exit header) {
    val pidFrom = header.getFrom();
    self.getLinks().remove(pidFrom);
  }
}
