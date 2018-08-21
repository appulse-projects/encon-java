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

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.appulse.encon.Node;
import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.Connection;
import io.appulse.encon.connection.control.Exit;
import io.appulse.encon.connection.control.ExitTraceToken;
import io.appulse.encon.connection.control.Link;
import io.appulse.encon.connection.control.Unlink;
import io.appulse.encon.connection.exception.CouldntConnectException;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.exception.NoSuchRemoteNodeException;
import io.appulse.encon.mailbox.exception.MailboxWithSuchNameDoesntExistException;
import io.appulse.encon.mailbox.exception.MailboxWithSuchPidDoesntExistException;
import io.appulse.encon.mailbox.exception.ReceivedExitException;
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
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Mailbox abstraction for receiving and sending Erlang's messages.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@Builder
@ToString(of = {
    "name",
    "pid"
})
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Mailbox implements Closeable {

  @Getter
  @NonFinal
  @Setter(PACKAGE)
  String name;

  @Getter
  @NonNull
  Node node;

  @Getter
  @NonNull
  ErlangPid pid;

  @NonNull
  BlockingQueue<Message> queue;

  @Getter
  Set<ErlangPid> links = ConcurrentHashMap.newKeySet();

  AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * Returns a new mailbox message.
   *
   * @param timeout how long to wait before giving up, in units of
   *        {@code unit}
   * @param unit a {@code TimeUnit} determining how to interpret the
   *        {@code timeout} parameter
   *
   * @return a new {@link Message}
   *
   * @throws ReceivedExitException someone exits
   */
  @SneakyThrows
  public Message receive (long timeout, TimeUnit unit) {
    Message message = queue.poll(timeout, unit);
    while (shouldContinueReceive(message)) {
      message = queue.poll(timeout, unit);
    }
    return message;
  }

  /**
   * Returns a new mailbox message.
   *
   * @return a new {@link Message}
   *
   * @throws ReceivedExitException someone exits
   */
  @SneakyThrows
  public Message receive () {
    Message message = queue.take();
    while (shouldContinueReceive(message)) {
      message = queue.take();
    }
    return message;
  }

  /**
   * Retuns mailbox's queue size.
   *
   * @return queue size
   */
  public int size () {
    return queue.size();
  }

  /**
   * Sends a message to local or remote mailbox.
   *
   * @param to local or remot mailbox's PID
   *
   * @param body message payload
   */
  public void send (@NonNull ErlangPid to, @NonNull ErlangTerm body) {
    val message = Message.send(to, body);
    if (isLocal(to)) {
      getMailbox(to).deliver(message);
    } else {
      getConnection(to).send(message);
    }
  }

  /**
   * Sends a message to local mailbox.
   *
   * @param mailbox mailbox name
   *
   * @param body message payload
   */
  public void send (@NonNull String mailbox, @NonNull ErlangTerm body) {
    val message = Message.send(mailbox, body);
    getMailbox(mailbox).deliver(message);
  }

  /**
   * Sends a message to remote node.
   *
   * @param remoteNodeName remote node name
   *
   * @param mailbox mailbox name
   *
   * @param body message payload
   */
  public void send (@NonNull String remoteNodeName, @NonNull String mailbox, @NonNull ErlangTerm body) {
    val descriptor = NodeDescriptor.from(remoteNodeName);
    send(descriptor, mailbox, body);
  }

  /**
   * Sends a message to remote node.
   *
   * @param descriptor remote node descriptor
   *
   * @param mailbox mailbox name
   *
   * @param body message payload
   */
  public void send (@NonNull NodeDescriptor descriptor, @NonNull String mailbox, @NonNull ErlangTerm body) {
    RemoteNode remote = node.lookup(descriptor);
    if (remote == null) {
      throw new NoSuchRemoteNodeException(descriptor);
    }

    send(remote, mailbox, body);
  }

  /**
   * Sends a message to remote node.
   *
   * @param remote remote node descriptor
   *
   * @param mailbox mailbox name
   *
   * @param body message payload
   */
  public void send (@NonNull RemoteNode remote, @NonNull String mailbox, @NonNull ErlangTerm body) {
    if (isLocal(remote)) {
      send(mailbox, body);
    } else {
      node.connect(remote)
          .send(Message.sendToRegisteredProcess(pid, mailbox, body));
    }
  }

  /**
   * Links the mailbox.
   *
   * @param to remote mailbox PID
   */
  public void link (@NonNull ErlangPid to) {
    val message = Message.link(pid, to);
    if (isLocal(to)) {
      getMailbox(to).deliver(message);
    } else {
      getConnection(to).send(message);
    }
    links.add(to);
  }

  /**
   * Unlinks the mailbox.
   *
   * @param to remote mailbox PID
   */
  public void unlink (@NonNull ErlangPid to) {
    links.remove(to);

    val message = Message.unlink(pid, to);
    if (isLocal(to)) {
      getMailbox(to).deliver(message);
    } else {
      getConnection(to).send(message);
    }
  }

  /**
   * Exits this mailbox.
   *
   * @param reason the exit's reason
   */
  public void exit (@NonNull ErlangTerm reason) {
    if (closed.get()) {
      return;
    }
    closed.set(true);

    log.debug("Exiting mailbox '{}:{}'. Reason: '{}'",
              pid, name, reason);

    links.forEach(it -> {
      val message = Message.exit(pid, it, reason);
      if (isLocal(it)) {
        getMailbox(it).deliver(message);
      } else {
        getConnection(it).send(message);
      }
    });

    node.remove(this);
  }

  /**
   * Exits this mailbox.
   *
   * @param reason the exit's reason
   */
  public void exit (@NonNull String reason) {
    exit(Erlang.atom(reason));
  }

  /**
   * Delivers a new message to mailbox.
   *
   * @param message a new message
   *
   * @throws NullPointerException in case of {@code message} is null
   */
  public void deliver (@NonNull Message message) {
    log.debug("{}:{} got message\n{}\n", pid, name, message);
    queue.add(message);
  }

  @Override
  public void close () {
    exit("normal");
  }

  private Mailbox getMailbox (@NonNull String remoteName) {
    Mailbox mailbox = node.mailbox(remoteName);
    if (mailbox == null) {
      throw new MailboxWithSuchNameDoesntExistException(remoteName);
    }
    return mailbox;
  }

  private Mailbox getMailbox (@NonNull ErlangPid remotePid) {
    Mailbox mailbox = node.mailbox(remotePid);
    if (mailbox == null) {
      throw new MailboxWithSuchPidDoesntExistException(remotePid);
    }
    return mailbox;
  }

  private Connection getConnection (@NonNull ErlangPid remotePid) {
    val remoteNode = node.lookup(remotePid);
    if (remoteNode == null) {
      throw new CouldntConnectException();
    }
    return node.connect(remoteNode);
  }

  private boolean isLocal (@NonNull ErlangPid remotePid) {
    return node.getDescriptor().equals(remotePid.getDescriptor());
  }

  private boolean isLocal (@NonNull RemoteNode remote) {
    return node.getDescriptor().equals(remote.getDescriptor());
  }

  private void exit (ErlangPid from, ErlangTerm reason) {
    links.remove(from);

    if (reason.isAtom()) {
      val test = reason.asText();
      if ("normal".equals(test)) {
        // ignore 'normal' exit
        return;
      } else if ("kill".equals(test)) {
        close();
        return;
      }
    }

    exit(reason);
    throw new ReceivedExitException(from, reason);
  }

  private boolean shouldContinueReceive (Message message) {
    if (message == null) {
      return false;
    }

    val header = message.getHeader();
    switch (header.getTag()) {
    case LINK:
      links.add(((Link) header).getFrom());
      break;
    case UNLINK:
      links.remove(((Unlink) header).getFrom());
      break;
    case EXIT:
    case EXIT2:
      Exit exit = (Exit) header;
      exit(exit.getFrom(), exit.getReason());
      break;
    case EXIT_TT:
    case EXIT2_TT:
      ExitTraceToken exitTrace = (ExitTraceToken) header;
      exit(exitTrace.getFrom(), exitTrace.getReason());
      break;
    default:
      return false;
    }
    return true;
  }
}
