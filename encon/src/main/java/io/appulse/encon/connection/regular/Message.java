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

package io.appulse.encon.connection.regular;

import static io.appulse.encon.terms.Erlang.atom;

import io.appulse.encon.connection.control.ControlMessage;
import io.appulse.encon.connection.control.Exit;
import io.appulse.encon.connection.control.Exit2;
import io.appulse.encon.connection.control.Link;
import io.appulse.encon.connection.control.Send;
import io.appulse.encon.connection.control.SendToRegisteredProcess;
import io.appulse.encon.connection.control.Unlink;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangAtom;
import io.appulse.encon.terms.type.ErlangPid;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import lombok.Value;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@SuppressWarnings("checkstyle:DesignForExtension")
public class Message {

  public static final int PASS_THROUGH_TAG = 0x70;

  public static final int VERSION_TAG = 0x83;

  public static Message sendToRegisteredProcess (@NonNull ErlangPid from,
                                                 @NonNull String mailbox,
                                                 @NonNull ErlangTerm body
  ) {
    return sendToRegisteredProcess(from, atom(mailbox), body);
  }

  public static Message sendToRegisteredProcess (@NonNull ErlangPid from,
                                                 @NonNull ErlangAtom mailbox,
                                                 @NonNull ErlangTerm body
  ) {
    return new Message(new SendToRegisteredProcess(from, mailbox), body);
  }

  public static Message send (@NonNull String mailbox, @NonNull ErlangTerm body) {
    return send(atom(mailbox), body);
  }

  public static Message send (@NonNull ErlangAtom mailbox, @NonNull ErlangTerm body) {
    return new Message(new Send(mailbox), body);
  }

  public static Message send (@NonNull ErlangPid pid, @NonNull ErlangTerm body) {
    return new Message(new Send(pid), body);
  }

  public static Message link (@NonNull ErlangPid from, @NonNull ErlangPid to) {
    return new Message(new Link(from, to), null);
  }

  public static Message unlink (@NonNull ErlangPid from, @NonNull ErlangPid to) {
    return new Message(new Unlink(from, to), null);
  }

  public static Message exit (@NonNull ErlangPid from, @NonNull ErlangPid to, @NonNull String reason) {
    return exit(from, to, atom(reason));
  }

  public static Message exit (@NonNull ErlangPid from, @NonNull ErlangPid to, @NonNull ErlangTerm reason) {
    return new Message(new Exit(from, to, reason), null);
  }

  public static Message exit2 (@NonNull ErlangPid from, @NonNull ErlangPid to, @NonNull String reason) {
    return exit2(from, to, atom(reason));
  }

  public static Message exit2 (@NonNull ErlangPid from, @NonNull ErlangPid to, @NonNull ErlangTerm reason) {
    return new Message(new Exit2(from, to, reason), null);
  }

  @NonNull
  ControlMessage header;

  ErlangTerm body;

  public void writeTo (ByteBuf buffer) {
    buffer.writeByte(0x70);
    buffer.writeByte(0x83);
    header.writeTo(buffer);

    if (body != null) {
      buffer.writeByte(0x83);
      body.writeTo(buffer);
    }
  }
}
