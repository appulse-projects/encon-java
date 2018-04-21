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

import static io.appulse.encon.java.protocol.TermType.NIL;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;
import io.netty.buffer.ByteBuf;
import lombok.ToString;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@ToString
public class ErlangNil extends ErlangTerm {

  private static final long serialVersionUID = 3124893467508024194L;

  public ErlangNil () {
    this(NIL);
  }

  public ErlangNil (TermType type) {
    super(type);
  }

  @Override
  protected void read (Bytes buffer) {
    // no body
  }

  @Override
  protected void read (ByteBuf buffer) {
    // no body
  }

  @Override
  protected void write (Bytes buffer) {
    // no body
  }

  @Override
  protected void write (ByteBuf buffer) {
    // no body
  }
}
