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

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.mailbox.Mailbox.InboxHandler;
import io.appulse.encon.java.protocol.type.Pid;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MailboxModule implements MailboxModuleApi, Closeable {

  NodeInternalApi internal;

  Map<Pid, WeakReference<Mailbox>> pids = new ConcurrentHashMap<>();

  Map<String, WeakReference<Mailbox>> names = new ConcurrentHashMap<>();

  @Override
  public void close () {
    pids.clear();
    names.clear();
  }

  @Override
  public Mailbox createMailbox (@NonNull InboxHandler handler) {
    Mailbox mailbox = Mailbox.builder()
        .internal(internal)
        .pid(internal.node().generatePid())
        .inboxHandler(handler)
        .build();

    pids.put(mailbox.getPid(), new WeakReference<>(mailbox));
    return mailbox;
  }

  @Override
  public Mailbox createMailbox (@NonNull String name, @NonNull InboxHandler handler) {
    val mailbox = createMailbox(handler);
    register(mailbox, name);
    return mailbox;
  }

  @Override
  public boolean register (@NonNull Mailbox mailbox, @NonNull String name) {
    if (names.containsKey(name)) {
      return false;
    }
    names.put(name, new WeakReference<>(mailbox));
    mailbox.setName(name);
    return true;
  }

  @Override
  public void deregisterMailbox (@NonNull String name) {
    ofNullable(names.remove(name))
        .map(WeakReference::get)
        .ifPresent(it -> it.setName(null));
  }

  @Override
  public Optional<Mailbox> getMailbox (String name) {
    return ofNullable(names.get(name))
        .map(WeakReference::get);
  }

  public Optional<Mailbox> getMailbox (Pid pid) {
    return ofNullable(pids.get(pid))
        .map(WeakReference::get);
  }

  public void remove (Mailbox mailbox) {
    ofNullable(pids.remove(mailbox.getPid()))
        .map(WeakReference::get)
        .ifPresent(it -> it.close());

    ofNullable(mailbox.getName())
        .ifPresent(names::remove);
  }
}
