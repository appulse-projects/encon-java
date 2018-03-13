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

package io.appulse.encon.java.protocol.exception;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class ErlangTermDecodeException extends RuntimeException {

  private static final long serialVersionUID = 1170646244362819840L;

  public ErlangTermDecodeException () {
    super();
  }

  public ErlangTermDecodeException (String message) {
    super(message);
  }

  public ErlangTermDecodeException (String message, Throwable cause) {
    super(message, cause);
  }

  public ErlangTermDecodeException (Throwable cause) {
    super(cause);
  }
}
