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

import java.util.Map;
import java.util.Optional;

import io.appulse.encon.module.mailbox.MailboxModule.NewMailboxBuilder;
import io.appulse.encon.terms.type.ErlangPid;

/**
 * Mailbox management API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface MailboxModuleApi {

  /**
   * A new mailbox builder.
   *
   * @return mailbox builder
   */
  NewMailboxBuilder mailbox ();

  /**
   * Searches local mailbox by its name.
   *
   * @param name the name of searching mailbox
   *
   * @return optional {@link Mailbox} instance
   */
  Optional<Mailbox> mailbox (String name);

  /**
   * Searches local mailbox by its pid.
   *
   * @param pid the pid of searching mailbox
   *
   * @return optional {@link Mailbox} instance
   */
  Optional<Mailbox> mailbox (ErlangPid pid);

  /**
   * Registers already created mailbox with specific name.
   *
   * @param mailbox mailbox instance for registration
   *
   * @param name    mailbox's registration name
   *
   * @return {@code true} if it was registered successfully, {@code false} otherwise
   */
  boolean register (Mailbox mailbox, String name);

  /**
   * Deregisters a mailbox by its name.
   * The mailbox keeps running, but it becomes unavailable by name anymore.
   *
   * @param name mailbox's registration name
   */
  void deregister (String name);

  /**
   * Removes a mailbox and cleanup its resources.
   *
   * @param mailbox the mailbox for removing
   */
  void remove (Mailbox mailbox);

  /**
   * Removes a mailbox and cleanup its resources by its name.
   *
   * @param name mailbox's registration name
   */
  void remove (String name);

  /**
   * Removes a mailbox and cleanup its resources by its pid.
   *
   * @param pid the pid of searching mailbox
   */
  void remove (ErlangPid pid);

  /**
   * Returns a map of all available node's mailboxes.
   *
   * @return a map of all available node's mailboxes
   */
  Map<ErlangPid, Mailbox> mailboxes ();
}
