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

package io.appulse.encon.terms.type;

import static io.appulse.encon.terms.TermType.FUNCTION;
import static io.appulse.encon.terms.TermType.NEW_FUNCTION;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * Provides a Java representation of Erlang internal functions.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangFunction extends ErlangTerm {

  private static final long serialVersionUID = 6531209051661233549L;

  ErlangPid pid;

  String module;

  int index;

  int unique;

  ErlangTerm[] variables;

  @NonFinal
  int arity;

  @NonFinal
  byte[] md5;

  @NonFinal
  int oldIndex;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangFunction (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    int freeVariablesCount = 0;
    if (type == FUNCTION) {
      freeVariablesCount = buffer.readInt();

      pid = ErlangTerm.newInstance(buffer);

      ErlangAtom atomModule = ErlangTerm.newInstance(buffer);
      module = atomModule.asText();

      ErlangInteger numberIndex = ErlangTerm.newInstance(buffer);
      index = numberIndex.asInt();

      ErlangInteger numberUnique = ErlangTerm.newInstance(buffer);
      unique = numberUnique.asInt();

    } else if (type == NEW_FUNCTION) {
      buffer.readInt(); // skip size

      arity = buffer.readByte();

      md5 = new byte[16];
      buffer.readBytes(md5);

      index = buffer.readInt();

      freeVariablesCount = buffer.readInt();

      ErlangAtom atomModule = ErlangTerm.newInstance(buffer);
      module = atomModule.asText();

      ErlangInteger numberOldIndex = ErlangTerm.newInstance(buffer);
      oldIndex = numberOldIndex.asInt();

      ErlangInteger numberUnique = ErlangTerm.newInstance(buffer);
      unique = numberUnique.asInt();

      pid = ErlangTerm.newInstance(buffer);
    } else {
      throw new IllegalErlangTermTypeException(getClass(), type);
    }

    variables = IntStream.range(0, freeVariablesCount)
        .mapToObj(it -> ErlangTerm.newInstance(buffer))
        .toArray(ErlangTerm[]::new);
  }

  @Builder
  private ErlangFunction (ErlangPid pid, String module, int index, int unique, ErlangTerm[] variables,
                          int arity, byte[] md5, int oldIndex) {
    super(md5 == null
          ? FUNCTION
          : NEW_FUNCTION);

    this.pid = pid;
    this.module = module;
    this.index = index;
    this.unique = unique;
    this.arity = arity;
    this.oldIndex = oldIndex;
    this.variables = ofNullable(variables)
        .map(it -> it.clone())
        .orElse(null);
    this.md5 = ofNullable(md5)
        .map(it -> it.clone())
        .orElse(null);
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    switch (getType()) {
    case FUNCTION:
      buffer.writeInt(variables.length);
      pid.writeTo(buffer);
      Erlang.atom(module).writeTo(buffer);
      Erlang.number(index).writeTo(buffer);
      Erlang.number(unique).writeTo(buffer);
      Stream.of(variables)
          .forEach(it -> it.writeTo(buffer));
      break;
    case NEW_FUNCTION:
      int position1 = buffer.writerIndex();

      buffer.writeInt(-1);
      buffer.writeByte(arity);
      buffer.writeBytes(md5);
      buffer.writeInt(index);
      buffer.writeInt(variables.length);
      Erlang.atom(module).writeTo(buffer);
      Erlang.number(oldIndex).writeTo(buffer);
      Erlang.number(unique).writeTo(buffer);
      pid.writeTo(buffer);
      Stream.of(variables)
          .forEach(it -> it.writeTo(buffer));

      int position2 = buffer.writerIndex();
      buffer.writerIndex(position1);
      buffer.writeInt(position2 - position1);
      buffer.writerIndex(position2 + Integer.BYTES);
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }
}
