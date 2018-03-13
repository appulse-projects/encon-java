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

import static io.appulse.encon.java.protocol.TermType.EXTERNAL_FUNCTION;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ErlangExternalFunction extends ErlangTerm {

  String module;

  String name;

  int arity;

  public ErlangExternalFunction (TermType type) {
    super(type);
  }

  @Builder
  public ErlangExternalFunction (@NonNull String module, @NonNull String name, int arity) {
    this(EXTERNAL_FUNCTION);

    this.module = module;
    this.name = name;
    this.arity = arity;
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    ErlangAtom atomModule = ErlangTerm.newInstance(buffer);
    module = atomModule.asText();

    ErlangAtom atomName = ErlangTerm.newInstance(buffer);
    name = atomName.asText();

    ErlangInteger numberArity = ErlangTerm.newInstance(buffer);
    arity = numberArity.asInt();
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    buffer.put(new ErlangAtom(module).toBytes());
    buffer.put(new ErlangAtom(name).toBytes());
    buffer.put(new ErlangInteger(arity).toBytes());
  }
}
