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

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.encon.java.Node;
import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.mailbox.request.RequestBuilder;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangPid;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Mailbox implements Closeable {

  @Getter
  @NonFinal
  @Setter(PACKAGE)
  String name;

  @NonNull
  NodeInternalApi internal;

  @NonNull
  InboxHandler inboxHandler;

  @Getter
  @NonNull
  ErlangPid pid;

  ExecutorService executor = Executors.newSingleThreadExecutor();

  public Node getNode () {
    return internal.node();
  }

  public RequestBuilder request () {
    return new RequestBuilder(this);
  }

  public CompletableFuture<ErlangTerm> receiveAsync () {
    throw new UnsupportedOperationException();
  }

  @SneakyThrows
  public ErlangTerm receive () {
    return receiveAsync().get();
  }

  @SneakyThrows
  public ErlangTerm receive (long timeout, @NonNull TimeUnit unit) {
    return receiveAsync().get(timeout, unit);
  }

  public void send (@NonNull ErlangPid pid, @NonNull ErlangTerm message) {
    if (pid.getDescriptor().equals(internal.node().getDescriptor())) {
      internal.mailboxes()
          .getMailbox(pid)
          .orElseThrow(RuntimeException::new)
          .inbox(message);;
      return;
    }

    RemoteNode remote = internal.node()
        .lookup(pid.getDescriptor())
        .orElseThrow(RuntimeException::new);

    internal.connections()
        .connect(remote)
        .send(pid, message);;
  }

  public void send (@NonNull String mailbox, @NonNull ErlangTerm message) {
    internal.mailboxes()
        .getMailbox(mailbox)
        .orElseThrow(RuntimeException::new)
        .inbox(message);
  }

  public void send (@NonNull String node, @NonNull String mailbox, @NonNull ErlangTerm message) {
    val descriptor = NodeDescriptor.from(node);
    send(descriptor, mailbox, message);
  }

  public void send (@NonNull NodeDescriptor descriptor, @NonNull String mailbox, @NonNull ErlangTerm message) {
    RemoteNode remote = internal.node()
        .lookup(descriptor)
        .orElseThrow(RuntimeException::new);

    send(remote, mailbox, message);
  }

  public void send (@NonNull RemoteNode remote, @NonNull String mailbox, @NonNull ErlangTerm message) {
    log.debug("Sending message\nTo node: {}\nMailbox: {}\nPayload: {}",
              remote, mailbox, message);

    if (internal.node().getDescriptor().equals(remote.getDescriptor())) {
      send(mailbox, message);
    } else {
      internal.connections()
          .connect(remote)
          .sendRegistered(pid, mailbox, message);
    }
    log.debug("Message was sent");
  }

  public void inbox (@NonNull ErlangTerm message) {
    log.debug("Inbox {}", message);
    executor.execute(() -> inboxHandler.receive(this, message));
    log.debug("END");
  }

  @Override
  public void close () {
    log.debug("Closing mailbox '{}:{}'...", name, pid);
    executor.shutdown();
    name = null;
  }

  public interface InboxHandler {

    void receive (Mailbox self, ErlangTerm message);
  }
}
