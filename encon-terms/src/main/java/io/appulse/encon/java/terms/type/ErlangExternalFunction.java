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

package io.appulse.encon.java.terms.type;

import static io.appulse.encon.java.terms.TermType.EXTERNAL_FUNCTION;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.terms.Erlang;
import io.appulse.encon.java.terms.ErlangTerm;
import io.appulse.encon.java.terms.TermType;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangExternalFunction extends ErlangTerm {

  private static final long serialVersionUID = -8415499460097963982L;

  String module;

  String name;

  int arity;

  public ErlangExternalFunction (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    val atomModule = ErlangTerm.newInstance(buffer);
    module = atomModule.asText();

    val atomName = ErlangTerm.newInstance(buffer);
    name = atomName.asText();

    val numberArity = ErlangTerm.newInstance(buffer);
    arity = numberArity.asInt();
  }

  @Builder
  public ErlangExternalFunction (@NonNull String module, @NonNull String name, int arity) {
    super(EXTERNAL_FUNCTION);

    this.module = module;
    this.name = name;
    this.arity = arity;
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    Erlang.atom(module).writeTo(buffer);
    Erlang.atom(name).writeTo(buffer);
    Erlang.number(arity).writeTo(buffer);
  }
}
