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

package io.appulse.encon.mailbox;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import io.appulse.encon.Node;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class ModuleMailbox implements Closeable {

  @NonNull
  Node node;

  Supplier<ErlangPid> pidProducer;

  Map<ErlangPid, Mailbox> pids = new ConcurrentHashMap<>();

  Map<String, Mailbox> names = new ConcurrentHashMap<>();

  @Override
  public void close () {
    log.info("Closing mailbox module");
    pids.values().forEach(Mailbox::close);

    pids.clear();
    names.clear();
  }

  public NewMailboxBuilder mailbox () {
    return new NewMailboxBuilder();
  }

  public boolean register (@NonNull Mailbox mailbox, @NonNull String name) {
    if (names.containsKey(name)) {
      return false;
    }
    log.info("Registering mailbox name '{}'", name);
    names.put(name, mailbox);
    mailbox.setName(name);
    return true;
  }

  public void deregister (@NonNull String name) {
    ofNullable(names.remove(name))
        .ifPresent(it -> {
          log.info("Deregistering mailbox by name {}", name);
          it.setName(null);
        });
  }

  public Mailbox mailbox (@NonNull String name) {
    return names.get(name);
  }

  public Mailbox mailbox (@NonNull ErlangPid pid) {
    return pids.get(pid);
  }

  public void remove (@NonNull Mailbox mailbox) {
    log.debug("Removing mailbox {}", mailbox);

    ofNullable(pids.remove(mailbox.getPid()))
        .ifPresent(it -> it.close());

    ofNullable(mailbox.getName())
        .ifPresent(names::remove);
  }

  public void remove (@NonNull String name) {
    log.debug("Removing mailbox by its name '{}'", name);
    Mailbox mailbox = mailbox(name);
    if (mailbox != null) {
      remove(mailbox);
    }
  }

  public void remove (@NonNull ErlangPid pid) {
    log.debug("Removing mailbox by its pid '{}'", pid);
    Mailbox mailbox = mailbox(pid);
    if (mailbox != null) {
      remove(mailbox);
    }
  }

  public Map<ErlangPid, Mailbox> mailboxes () {
    return pids;
  }

  public void registerNetKernelMailbox () {
    if (names.containsKey("net_kernel")) {
      return;
    }
    ErlangPid pid = pidProducer.get();
    Mailbox netKernel = new NetKernelMailbox(node, pid);
    pids.put(pid, netKernel);
    register(netKernel, "net_kernel");
  }

  @FieldDefaults(level = PRIVATE)
  @NoArgsConstructor(access = PRIVATE)
  public final class NewMailboxBuilder {

    String name;

    BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

    public NewMailboxBuilder name (String mailboxName) {
      this.name = mailboxName;
      return this;
    }

    public NewMailboxBuilder queue (BlockingQueue<Message> mailboxQueue) {
      this.queue = mailboxQueue;
      return this;
    }

    public Mailbox build () {
      ErlangPid pid = pidProducer.get();
      Mailbox mailbox = Mailbox.builder()
          .name(name)
          .node(node)
          .queue(queue)
          .pid(pid)
          .build();

      pids.put(mailbox.getPid(), mailbox);
      ofNullable(mailbox.getName())
          .ifPresent(it -> register(mailbox, it));

      return mailbox;
    }
  }
}
