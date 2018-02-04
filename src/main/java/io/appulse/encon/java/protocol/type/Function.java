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

package io.appulse.encon.java.protocol.type;

import static io.appulse.encon.java.protocol.TermType.FUNCTION;
import static io.appulse.encon.java.protocol.TermType.NEW_FUNCTION;
import static lombok.AccessLevel.PRIVATE;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
public class Function extends ErlangTerm {

  Pid pid;

  String module;

  int index;

  int unique;

  ErlangTerm[] variables;

  int arity;

  byte[] md5;

  int oldIndex;

  public Function (TermType type) {
    super(type);
  }

  @Builder
  private Function (Pid pid, String module, int index, int unique, ErlangTerm[] variables,
              int arity, byte[] md5, int oldIndex) {
    super(md5 == null
        ? FUNCTION
        : NEW_FUNCTION);

    this.pid = pid;
    this.module = module;
    this.index = index;
    this.unique = unique;
    this.variables = variables;
    this.arity = arity;
    this.md5 = md5;
    this.oldIndex = oldIndex;
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    int freeVariablesCount = 0;

    if (getType() == FUNCTION) {
      freeVariablesCount = buffer.getInt();

      pid = ErlangTerm.newInstance(buffer);

      Atom atomModule = ErlangTerm.newInstance(buffer);
      module = atomModule.asText();

      IntegralNumber numberIndex = ErlangTerm.newInstance(buffer);
      index = numberIndex.asInt();

      IntegralNumber numberUnique = ErlangTerm.newInstance(buffer);
      unique = numberUnique.asInt();

    } else if (getType() == NEW_FUNCTION) {
      buffer.getInt(); // skip size

      arity = buffer.getByte();

      md5 = buffer.getBytes(16);

      index = buffer.getInt();

      freeVariablesCount = buffer.getInt();

      Atom atomModule = ErlangTerm.newInstance(buffer);
      module = atomModule.asText();

      IntegralNumber numberOldIndex = ErlangTerm.newInstance(buffer);
      oldIndex = numberOldIndex.asInt();

      IntegralNumber numberUnique = ErlangTerm.newInstance(buffer);
      unique = numberUnique.asInt();

      pid = ErlangTerm.newInstance(buffer);
    }

    variables = IntStream.range(0, freeVariablesCount)
        .mapToObj(it -> ErlangTerm.newInstance(buffer))
        .toArray(ErlangTerm[]::new);
  }

  @Override
  @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
  protected void write (@NonNull Bytes buffer) {
    switch (getType()) {
    case FUNCTION:
      buffer.put4B(variables.length);
      buffer.put(pid.toBytes());
      buffer.put(new Atom(module).toBytes());
      buffer.put(new IntegralNumber(index).toBytes());
      buffer.put(new IntegralNumber(unique).toBytes());
      Stream.of(variables)
          .map(ErlangTerm::toBytes)
          .forEach(buffer::put);
      break;
    case NEW_FUNCTION:
      int position = buffer.position();
      buffer.put4B(-1);
      buffer.put1B(arity);
      buffer.put(md5);
      buffer.put4B(index);
      buffer.put4B(variables.length);
      buffer.put(new Atom(module).toBytes());
      buffer.put(new IntegralNumber(oldIndex).toBytes());
      buffer.put(new IntegralNumber(unique).toBytes());
      buffer.put(pid.toBytes());
      Stream.of(variables)
          .map(ErlangTerm::toBytes)
          .forEach(buffer::put);
      buffer.put4B(position, buffer.position() - position);
      break;
    default:
      throw new RuntimeException();
    }
  }
}
