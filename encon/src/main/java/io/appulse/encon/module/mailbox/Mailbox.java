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

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.appulse.encon.Node;
import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.module.NodeInternalApi;
import io.appulse.encon.module.connection.Connection;
import io.appulse.encon.module.connection.exception.CouldntConnectException;
import io.appulse.encon.module.connection.regular.Message;
import io.appulse.encon.module.lookup.exception.NoSuchRemoteNodeException;
import io.appulse.encon.module.mailbox.exception.MailboxWithSuchNameDoesntExistException;
import io.appulse.encon.module.mailbox.exception.MailboxWithSuchPidDoesntExistException;
import io.appulse.encon.module.mailbox.handler.MailboxHandler;
import io.appulse.encon.module.mailbox.handler.ReceiveMailboxHandler;
import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
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

  @Getter
  Set<ErlangPid> links = ConcurrentHashMap.newKeySet(10);

  AtomicBoolean closed = new AtomicBoolean(false);

  public Node getNode () {
    return internal.node();
  }

  public RequestBuilder request () {
    return new RequestBuilder(this);
  }

  public CompletableFuture<Message> receiveAsync () {
    if (!(handler instanceof ReceiveMailboxHandler)) {
      throw new UnsupportedOperationException("Mailbox.receive* methods work only with ManualMailboxHandler impleentations");
    }
    return ((ReceiveMailboxHandler) handler).receive();
  }

  @SneakyThrows
  public Message receive () {
    return receiveAsync().get();
  }

  @SneakyThrows
  public Message receive (long timeout, @NonNull TimeUnit unit) {
    return receiveAsync().get(timeout, unit);
  }

  public void send (@NonNull ErlangPid to, @NonNull ErlangTerm body) {
    val message = Message.send(to, body);
    if (isLocal(to)) {
      getMailbox(to).deliver(message);
    } else {
      getConnection(to).send(message);
    }
  }

  public void send (@NonNull String mailbox, @NonNull ErlangTerm body) {
    val message = Message.send(mailbox, body);
    getMailbox(mailbox).deliver(message);
  }

  public void send (@NonNull String node, @NonNull String mailbox, @NonNull ErlangTerm body) {
    val descriptor = NodeDescriptor.from(node);
    send(descriptor, mailbox, body);
  }

  public void send (@NonNull NodeDescriptor descriptor, @NonNull String mailbox, @NonNull ErlangTerm body) {
    RemoteNode remote = internal.node()
        .lookup(descriptor)
        .orElseThrow(() -> new NoSuchRemoteNodeException(descriptor));

    send(remote, mailbox, body);
  }

  public void send (@NonNull RemoteNode remote, @NonNull String mailbox, @NonNull ErlangTerm body) {
    if (isLocal(remote)) {
      send(mailbox, body);
    } else {
      internal.connections()
          .connect(remote)
          .send(Message.sendToRegisteredProcess(pid, mailbox, body));
    }
  }

  public void link (@NonNull ErlangPid to) {
    val message = Message.link(pid, to);
    if (isLocal(to)) {
      getMailbox(to).deliver(message);
    } else {
      getConnection(to).send(message);
    }
    links.add(to);
  }

  public void unlink (@NonNull ErlangPid to) {
    links.remove(to);

    val message = Message.unlink(pid, to);
    if (isLocal(to)) {
      getMailbox(to).deliver(message);
    } else {
      getConnection(to).send(message);
    }
  }

  public void exit (@NonNull ErlangTerm reason) {
    if (closed.get()) {
      return;
    }
    closed.set(true);

    log.debug("Exiting mailbox '{}:{}'. Reason: '{}'",
              name, pid, reason);

    executor.shutdown();

    links.forEach(it -> {
      val message = Message.exit(pid, it, reason);
      if (isLocal(it)) {
        getMailbox(it).deliver(message);
      } else {
        getConnection(it).send(message);
      }
    });

    internal.mailboxes()
        .remove(this);
  }

  public void exit (@NonNull String reason) {
    exit(Erlang.atom(reason));
  }

  public void deliver (@NonNull Message message) {
    log.debug("Delivered\n  {}\n", message.getHeader());
    executor.execute(() -> handler.handle(this, message));
  }

  @Override
  public void close () {
    exit("normal");
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
    val remoteNode = internal.lookups().lookupUnsafe(remotePid);
    if (remoteNode == null) {
      throw new CouldntConnectException();
    }
    return internal.connections().connect(remoteNode);
  }

  private boolean isLocal (@NonNull ErlangPid remotePid) {
    return internal.node().getDescriptor().equals(remotePid.getDescriptor());
  }

  private boolean isLocal (@NonNull RemoteNode remote) {
    return internal.node().getDescriptor().equals(remote.getDescriptor());
  }
}
