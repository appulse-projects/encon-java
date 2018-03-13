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

package io.appulse.encon.java.module.mailbox.request;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangPid;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@FieldDefaults(level = PRIVATE)
@RequiredArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
public class RequestInvoker {

  final Mailbox self;

  @Setter(PROTECTED)
  ErlangTerm message;

  public void send (@NonNull ErlangPid pid) {
    self.send(pid, message);
  }

  public void send (@NonNull String mailbox) {
    this.self.send(mailbox, message);
  }

  public void send (@NonNull String node, @NonNull String mailbox) {
    this.self.send(node, mailbox, message);
  }

  public void send (@NonNull NodeDescriptor descriptor, @NonNull String mailbox) {
    this.self.send(descriptor, mailbox, message);
  }

  public void send (@NonNull RemoteNode remote, @NonNull String mailbox) {
    this.self.send(remote, mailbox, message);
  }
}
