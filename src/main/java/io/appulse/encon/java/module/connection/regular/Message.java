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

package io.appulse.encon.java.module.connection.regular;

import static io.appulse.encon.java.protocol.Erlang.atom;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import io.appulse.encon.java.module.connection.control.ControlMessage;
import io.appulse.encon.java.module.connection.control.ControlMessageTag;
import io.appulse.encon.java.module.connection.control.Exit;
import io.appulse.encon.java.module.connection.control.Exit2;
import io.appulse.encon.java.module.connection.control.Link;
import io.appulse.encon.java.module.connection.control.Send;
import io.appulse.encon.java.module.connection.control.SendToRegisteredProcess;
import io.appulse.encon.java.module.connection.control.Unlink;
import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangAtom;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.utils.Bytes;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Value
public class Message {

  public static Message sendToRegisteredProcess (@NonNull ErlangPid from, @NonNull String mailbox,
                                                 @NonNull ErlangTerm body) {
    return sendToRegisteredProcess(from, atom(mailbox), body);
  }

  public static Message sendToRegisteredProcess (@NonNull ErlangPid from, @NonNull ErlangAtom mailbox,
                                                 @NonNull ErlangTerm body) {
    return new Message(new SendToRegisteredProcess(from, mailbox), of(body));
  }

  public static Message send (@NonNull String mailbox, @NonNull ErlangTerm body) {
    return send(atom(mailbox), body);
  }

  public static Message send (@NonNull ErlangAtom mailbox, @NonNull ErlangTerm body) {
    return new Message(new Send(mailbox), of(body));
  }

  public static Message send (@NonNull ErlangPid pid, @NonNull ErlangTerm body) {
    return new Message(new Send(pid), of(body));
  }

  public static Message link (@NonNull ErlangPid from, @NonNull ErlangPid to) {
    return new Message(new Link(from, to), empty());
  }

  public static Message unlink (@NonNull ErlangPid from, @NonNull ErlangPid to) {
    return new Message(new Unlink(from, to), empty());
  }

  public static Message exit (@NonNull ErlangPid from, @NonNull ErlangPid to, @NonNull String reason) {
    return exit(from, to, atom(reason));
  }

  public static Message exit (@NonNull ErlangPid from, @NonNull ErlangPid to, @NonNull ErlangTerm reason) {
    return new Message(new Exit(from, to, reason), empty());
  }

  public static Message exit2 (@NonNull ErlangPid from, @NonNull ErlangPid to, @NonNull String reason) {
    return exit2(from, to, atom(reason));
  }

  public static Message exit2 (@NonNull ErlangPid from, @NonNull ErlangPid to, @NonNull ErlangTerm reason) {
    return new Message(new Exit2(from, to, reason), empty());
  }

  public static Message tickTock () {
    return new Message(new TickTockControlMessage(), of(new TickTockTerm()));
  }

  @NonNull
  ControlMessage header;

  @NonNull
  Optional<ErlangTerm> body;

  private static final class TickTockControlMessage extends ControlMessage {

    @Override
    public ControlMessageTag getTag () {
      return ControlMessageTag.UNDEFINED;
    }

    @Override
    protected ErlangTerm[] elements () {
      throw new UnsupportedOperationException();
    }
  }

  private static final class TickTockTerm extends ErlangTerm {

    private static final long serialVersionUID = 3273253476743681854L;

    private static final byte[] TICK_TOCK_BYTES = new byte[] { 0, 0, 0, 0 };

    TickTockTerm () {
      super(TermType.UNDEFINED);
    }

    @Override
    public byte[] toBytes () {
      return TICK_TOCK_BYTES.clone();
    }

    @Override
    protected void read (Bytes buffer) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected void read (ByteBuf buffer) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected void write (Bytes buffer) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected void write (ByteBuf buffer) {
      throw new UnsupportedOperationException();
    }
  }
}
