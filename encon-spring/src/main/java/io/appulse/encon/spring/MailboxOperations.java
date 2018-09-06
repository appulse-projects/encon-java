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

package io.appulse.encon.spring;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.appulse.encon.Node;
import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.databind.TermMapper;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MailboxOperations {

  static String generateName (Node node) {
    return new StringBuilder()
        .append(node.getDescriptor().getNodeName())
        .append("MailboxOperations")
        .toString();
  }

  static String generateName (Mailbox mailbox) {
    String name = mailbox.getName();
    if (name == null || name.isEmpty()) {
      return generateName(mailbox.getNode());
    }
    return new StringBuilder()
        .append(mailbox.getNode().getDescriptor().getNodeName())
        .append('_')
        .append(name)
        .append("MailboxOperations")
        .toString();
  }

  @NonNull
  Mailbox self;

  /**
   * Returns the name of the mailbox.
   *
   * @return mailbox's name
   */
  public String name () {
    return self.getName();
  }

  /**
   * Returns the PID of the mailbox.
   *
   * @return mailbox's PID
   */
  public ErlangPid pid () {
    return self.getPid();
  }

  /**
   * Returns the Node of the mailbox.
   *
   * @return mailbox's Node
   */
  public Node node () {
    return self.getNode();
  }

  /**
   * Sends a message.
   *
   * @param to      destination PID
   *
   * @param object  message payload
   */
  public void send (@NonNull ErlangPid to, @NonNull Object object) {
    val term = TermMapper.serialize(object);
    self.send(to, term);
  }

  /**
   * Sends a message.
   *
   * @param mailbox destination mailbox name
   *
   * @param object  message payload
   */
  public void send (@NonNull String mailbox, @NonNull Object object) {
    val term = TermMapper.serialize(object);
    self.send(mailbox, term);
  }

  /**
   * Sends a message.
   *
   * @param node    destination node name
   *
   * @param mailbox destination mailbox name
   *
   * @param object  message payload
   */
  public void send (@NonNull String node, @NonNull String mailbox, @NonNull Object object) {
    val term = TermMapper.serialize(object);
    self.send(node, mailbox, term);
  }

  /**
   * Sends a message.
   *
   * @param descriptor  destination node descriptor
   *
   * @param mailbox     destination mailbox name
   *
   * @param object      message payload
   */
  public void send (@NonNull NodeDescriptor descriptor, @NonNull String mailbox, @NonNull Object object) {
    val term = TermMapper.serialize(object);
    self.send(descriptor, mailbox, term);
  }

  /**
   * Sends a message.
   *
   * @param remote  destination node remote descriptor
   *
   * @param mailbox destination mailbox name
   *
   * @param object  message payload
   */
  public void send (@NonNull RemoteNode remote, @NonNull String mailbox, @NonNull Object object) {
    val term = TermMapper.serialize(object);
    self.send(remote, mailbox, term);
  }

  /**
   * Returns a new mailbox message.
   *
   * @return a new mailbox message
   */
  public ErlangTerm receive () {
    return self.receive().getBody();
  }

  /**
   * Returns a new mailbox message.
   *
   * @param timeout how long to wait before giving up, in units of
   *        {@code unit}
   * @param unit a {@code TimeUnit} determining how to interpret the
   *        {@code timeout} parameter
   *
   * @return a new mailbox message, or empty
   */
  public Optional<ErlangTerm> receive (long timeout, TimeUnit unit) {
    return ofNullable(self.receive(timeout, unit))
        .map(Message::getBody);
  }

  /**
   * Returns a new mailbox message.
   *
   * @param type the type of expected message
   *
   * @return a new mailbox message
   */
  public <T> T receive (Class<T> type) {
    val term = receive();
    return TermMapper.deserialize(term, type);
  }

  /**
   * Returns a new mailbox message.
   *
   * @param type the type of expected message
   *
   * @param timeout how long to wait before giving up, in units of
   *        {@code unit}
   * @param unit a {@code TimeUnit} determining how to interpret the
   *        {@code timeout} parameter
   *
   * @return a new mailbox message, or empty
   */
  public <T> Optional<T> receive (Class<T> type, long timeout, TimeUnit unit) {
    return receive(timeout, unit)
        .map(it -> TermMapper.deserialize(it, type));
  }
}
