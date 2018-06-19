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

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.appulse.encon.module.NodeInternalApi;
import io.appulse.encon.module.mailbox.handler.DefaultMailboxHandler;
import io.appulse.encon.module.mailbox.handler.MailboxHandler;
import io.appulse.encon.terms.type.ErlangPid;
import io.appulse.utils.threads.AppulseExecutors;
import io.appulse.utils.threads.AppulseThreadFactory;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class MailboxModule implements MailboxModuleApi, Closeable {

  NodeInternalApi internal;

  Map<ErlangPid, Mailbox> pids = new ConcurrentHashMap<>();

  Map<String, Mailbox> names = new ConcurrentHashMap<>();

  @Override
  public void close () {
    pids.values().forEach(Mailbox::close);

    pids.clear();
    names.clear();
  }

  @Override
  public NewMailboxBuilder mailbox () {
    return new NewMailboxBuilder();
  }

  @Override
  public boolean register (@NonNull Mailbox mailbox, @NonNull String name) {
    if (names.containsKey(name)) {
      return false;
    }
    names.put(name, mailbox);
    mailbox.setName(name);
    return true;
  }

  @Override
  public void deregister (@NonNull String name) {
    ofNullable(names.remove(name))
        .ifPresent(it -> it.setName(null));
  }

  @Override
  public Optional<Mailbox> mailbox (@NonNull String name) {
    return ofNullable(names.get(name));
  }

  public Mailbox mailboxUnsafe (String name) {
    return names.get(name);
  }

  @Override
  public Optional<Mailbox> mailbox (@NonNull ErlangPid pid) {
    return ofNullable(pids.get(pid));
  }

  public Mailbox mailboxUnsafe (ErlangPid pid) {
    return pids.get(pid);
  }

  @Override
  public void remove (@NonNull Mailbox mailbox) {
    ofNullable(pids.remove(mailbox.getPid()))
        .ifPresent(it -> it.close());

    ofNullable(mailbox.getName())
        .ifPresent(names::remove);
  }

  @Override
  public void remove (@NonNull String name) {
    mailbox(name)
        .ifPresent(this::remove);
  }

  @Override
  public void remove (@NonNull ErlangPid pid) {
    mailbox(pid)
        .ifPresent(this::remove);
  }

  @Override
  public Map<ErlangPid, Mailbox> mailboxes () {
    return pids;
  }

  @FieldDefaults(level = PRIVATE)
  @NoArgsConstructor(access = PRIVATE)
  public final class NewMailboxBuilder {

    final Mailbox.MailboxBuilder builder = Mailbox.builder()
        .handler(new DefaultMailboxHandler());

    public NewMailboxBuilder name (String name) {
      builder.name(name);
      return this;
    }

    public NewMailboxBuilder handler (@NonNull MailboxHandler handler) {
      builder.handler(handler);
      return this;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public NewMailboxBuilder handler (Class<?> type) {
      Class handlerType = ofNullable(type)
          .map(it -> (Class) it)
          .orElse(DefaultMailboxHandler.class);

      if (handlerType.isAssignableFrom(MailboxHandler.class)) {
        throw new IllegalArgumentException("");
      }
      return handler(((Class<? extends MailboxHandler>) handlerType).newInstance());
    }

    public Mailbox build () {
      ErlangPid pid = internal.node().generatePid();
      val threadFactory = AppulseThreadFactory.builder()
          .name(pid.toString())
          .daemon(false)
          .build();

      builder.executor(AppulseExecutors.newSingleThreadExecutor()
          .threadFactory(threadFactory)
          .build());

      Mailbox mailbox = builder
          .internal(internal)
          .pid(pid)
          .build();

      pids.put(mailbox.getPid(), mailbox);
      ofNullable(mailbox.getName())
          .ifPresent(it -> register(mailbox, it));

      return mailbox;
    }
  }
}
