/*
 * Copyright 2020 the original author or authors.
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

package io.appulse.encon.mailbox.exception;

import io.appulse.encon.terms.type.ErlangPid;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Unknown mailbox exception.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class MailboxWithSuchPidDoesntExistException extends NoSuchMailboxException {

  private static final long serialVersionUID = 2742510481673158004L;

  ErlangPid pid;

  public MailboxWithSuchPidDoesntExistException (ErlangPid pid) {
    super("Mailbox with PID " + pid + " doesn't exist");
    this.pid = pid;
  }
}
