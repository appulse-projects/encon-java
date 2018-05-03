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

import java.util.Optional;

import io.appulse.encon.module.connection.control.ControlMessage;
import io.appulse.encon.terms.ErlangTerm;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
public class DefaultMailboxHandler extends AbstractMailboxHandler {

  @Override
  protected void handle (Mailbox self, ControlMessage header, Optional<ErlangTerm> body) {
    log.debug("Mailbox '{}({})' received message\n  header: {}\n  body: {}",
              self.getName(), self.getPid(), header, body);
  }
}
