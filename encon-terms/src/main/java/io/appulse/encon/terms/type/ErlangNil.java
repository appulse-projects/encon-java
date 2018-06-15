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

import static io.appulse.encon.terms.TermType.NIL;

import io.appulse.encon.terms.ErlangTerm;

import io.netty.buffer.ByteBuf;
import lombok.ToString;

/**
 * The representation for an empty list, that is, the Erlang syntax {@code []}.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
public class ErlangNil extends ErlangTerm {

  private static final long serialVersionUID = 3124893467508024194L;

  /**
   * Default no-arguments constructor.
   */
  public ErlangNil () {
    super(NIL);
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    // no body
  }
}
