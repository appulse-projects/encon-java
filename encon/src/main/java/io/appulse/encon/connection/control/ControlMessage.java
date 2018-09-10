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

package io.appulse.encon.connection.control;

import static io.appulse.encon.connection.control.ControlMessageTag.UNDEFINED;
import static java.util.stream.Collectors.toCollection;

import java.util.LinkedList;
import java.util.stream.Stream;

import io.appulse.encon.connection.control.exception.ControlMessageParsingException;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangInteger;
import io.appulse.encon.terms.type.ErlangNil;
import io.appulse.encon.terms.type.ErlangTuple;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public abstract class ControlMessage {

  protected static final ErlangTerm UNUSED = new ErlangNil();

  @SuppressWarnings("unchecked")
  public static <T extends ControlMessage> T parse (@NonNull ErlangTerm term) {
    if (!term.isTuple()) {
      throw new ControlMessageParsingException("Expected type is TUPLE, but was " + term.getType());
    }
    val tuple = term.asTuple();

    val first = tuple.getUnsafe(0);
    if (!first.isNumber()) {
      throw ControlMessageParsingException.fieldException(tuple, 0, Integer.class);
    }
    val code = first.asInt();

    val tag = ControlMessageTag.of(code);
    if (tag == UNDEFINED) {
      throw new ControlMessageParsingException("Unknown tag code: " + code);
    }

    switch (tag) {
    case LINK:
      return (T) new Link(tuple);
    case SEND:
      return (T) new Send(tuple);
    case EXIT:
      return (T) new Exit(tuple);
    case UNLINK:
      return (T) new Unlink(tuple);
    case NODE_LINK:
      return (T) new NodeLink(tuple);
    case REG_SEND:
      return (T) new SendToRegisteredProcess(tuple);
    case GROUP_LEADER:
      return (T) new GroupLeader(tuple);
    case EXIT2:
      return (T) new Exit2(tuple);
    case SEND_TT:
      return (T) new SendTraceToken(tuple);
    case EXIT_TT:
      return (T) new ExitTraceToken(tuple);
    case REG_SEND_TT:
      return (T) new SendToRegisteredProcessTraceToken(tuple);
    case EXIT2_TT:
      return (T) new Exit2TraceToken(tuple);
    case MONITOR_P:
      return (T) new MonitorProcess(tuple);
    case DEMONITOR_P:
      return (T) new DemonitorProcess(tuple);
    case MONITOR_P_EXIT:
      return (T) new MonitorProcessExit(tuple);
    default:
      throw new UnsupportedOperationException("Unsupported tag - " + tag);
    }
  }

  public final ErlangTuple toTuple () {
    val elements = Stream.of(elements())
        .collect(toCollection(LinkedList::new));

    elements.addFirst(ErlangInteger.cached(getTag().getCode()));
    return new ErlangTuple(elements);
  }

  /**
   * Converts this control message to bytes.
   *
   * @return byte array
   */
  public byte[] toBytes () {
    return toTuple().toBytes();
  }

  public final void writeTo (ByteBuf buffer) {
    toTuple().writeTo(buffer);
  }

  public abstract ControlMessageTag getTag ();

  protected abstract ErlangTerm[] elements ();
}
