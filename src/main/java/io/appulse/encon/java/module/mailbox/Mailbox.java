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

import static io.appulse.encon.java.module.connection.control.ControlMessageTag.EXIT;
import static io.appulse.encon.java.module.connection.control.ControlMessageTag.EXIT2;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import io.appulse.encon.java.Node;
import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.Connection;
import io.appulse.encon.java.module.connection.control.Exit;
import io.appulse.encon.java.module.connection.control.Link;
import io.appulse.encon.java.module.connection.control.Send;
import io.appulse.encon.java.module.connection.control.Unlink;
import io.appulse.encon.java.module.connection.exception.CouldntConnectException;
import io.appulse.encon.java.module.connection.regular.Message;
import io.appulse.encon.java.module.lookup.exception.NoSuchRemoteNodeException;
import io.appulse.encon.java.module.mailbox.exception.MailboxWithSuchNameDoesntExistException;
import io.appulse.encon.java.module.mailbox.exception.MailboxWithSuchPidDoesntExistException;
import io.appulse.encon.java.module.mailbox.exception.ReceivedExitException;
import io.appulse.encon.java.module.mailbox.request.RequestBuilder;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangAtom;
import io.appulse.encon.java.protocol.type.ErlangPid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class Mailbox implements Closeable {

  @Getter
  @NonFinal
  @Setter(PACKAGE)
  String name;

  @NonNull
  NodeInternalApi internal;

  @NonNull
  MailboxHandler handler;

  @Getter
  @NonNull
  ErlangPid pid;

  @NonNull
  ExecutorService executor;

  @Getter(PACKAGE)
  Set<ErlangPid> links = ConcurrentHashMap.newKeySet(10);

  @NonFinal
  CompletableFuture<Message> currentFuture;

  @NonFinal
  volatile boolean closed;

  public Node getNode () {
    return internal.node();
  }

  public RequestBuilder request () {
    return new RequestBuilder(this);
  }

  public CompletableFuture<Message> receiveAsync () {
    return getCurrentFuture();
  }

  @SneakyThrows
  public Message receive () {
    return receiveAsync().get();
  }

  @SneakyThrows
  public Message receive (long timeout, @NonNull TimeUnit unit) {
    return receiveAsync().get(timeout, unit);
  }

  public void send (@NonNull ErlangPid to, @NonNull ErlangTerm message) {
    if (isLocal(to)) {
      getMailbox(to).send(message);
    } else {
      getConnection(to).send(to, message);
    }
  }

  public void send (@NonNull String mailbox, @NonNull ErlangTerm message) {
    getMailbox(mailbox).send(message);
  }

  public void send (@NonNull String node, @NonNull String mailbox, @NonNull ErlangTerm message) {
    val descriptor = NodeDescriptor.from(node);
    send(descriptor, mailbox, message);
  }

  public void send (@NonNull NodeDescriptor descriptor, @NonNull String mailbox, @NonNull ErlangTerm message) {
    RemoteNode remote = internal.node()
        .lookup(descriptor)
        .orElseThrow(() -> new NoSuchRemoteNodeException(descriptor));

    send(remote, mailbox, message);
  }

  public void send (@NonNull RemoteNode remote, @NonNull String mailbox, @NonNull ErlangTerm message) {
    log.debug("Sending message\nTo node: {}\nMailbox: {}\nPayload: {}",
              remote, mailbox, message);

    if (isLocal(remote)) {
      send(mailbox, message);
    } else {
      internal.connections()
          .connect(remote)
          .sendToRegisteredProcess(pid, mailbox, message);
    }
    log.debug("Message was sent");
  }

  public void link (@NonNull ErlangPid to) {
    if (isLocal(to)) {
      getMailbox(to).deliver(new Message(new Link(pid, to), empty()));
    } else {
      getConnection(to).link(pid, to);
    }
    links.add(to);
  }

  public void unlink (@NonNull ErlangPid to) {
    links.remove(to);

    if (isLocal(to)) {
      getMailbox(to).deliver(new Message(new Unlink(pid, to), empty()));
    } else {
      getConnection(to).unlink(pid, to);
    }
  }

  public void exit (@NonNull String reason) {
    closed = true;

    log.debug("Exiting mailbox '{}:{}'. Reason: '{}'",
              name, pid, reason);

    executor.shutdown();

    links.forEach(it -> {
      if (isLocal(it)) {
        getMailbox(it).deliver(new Message(new Exit(pid, it, new ErlangAtom(reason)), empty()));
      } else {
        getConnection(it).exit(pid, it, reason);
      }
    });

    internal.mailboxes()
        .remove(this);
  }

  public void deliver (@NonNull Message message) {
    log.debug("Deliver: {}", message);
    executor.execute(() -> handle(message));
  }

  @Override
  public void close () {
    if (closed) {
      return;
    }
    exit("normal");
  }

  private void send (@NonNull ErlangTerm body) {
    deliver(new Message(new Send(pid), of(body)));
  }

  @Synchronized
  private CompletableFuture<Message> getCurrentFuture () {
    if (currentFuture == null) {
      currentFuture = new CompletableFuture<>();
    }
    return currentFuture;
  }

  @Synchronized
  private void handle (@NonNull Message message) {
    if (currentFuture != null) {
      if (message.getHeader().getTag() == EXIT || message.getHeader().getTag() == EXIT2) {
        val exit = (Exit) message.getHeader();
        val fromPid = exit.getFrom();
        val reason = exit.getReason();
        currentFuture.completeExceptionally(new ReceivedExitException(fromPid, reason));
      } else {
        currentFuture.complete(message);
      }
      currentFuture = null;
    }
    handler.receive(this, message.getHeader(), message.getBody());
  }

  private Mailbox getMailbox (@NonNull String remoteName) {
    return internal.mailboxes()
        .mailbox(remoteName)
        .orElseThrow(() -> new MailboxWithSuchNameDoesntExistException(remoteName));
  }

  private Mailbox getMailbox (@NonNull ErlangPid remotePid) {
    return internal.mailboxes()
        .mailbox(remotePid)
        .orElseThrow(() -> new MailboxWithSuchPidDoesntExistException(remotePid));
  }

  private Connection getConnection (@NonNull ErlangPid remotePid) {
    return internal.node()
        .lookup(remotePid)
        .map(it -> internal.connections().connect(it))
        .orElseThrow(CouldntConnectException::new);
  }

  private boolean isLocal (@NonNull ErlangPid remotePid) {
    return internal.node().getDescriptor().equals(remotePid.getDescriptor());
  }

  private boolean isLocal (@NonNull RemoteNode remote) {
    return internal.node().getDescriptor().equals(remote.getDescriptor());
  }
}
