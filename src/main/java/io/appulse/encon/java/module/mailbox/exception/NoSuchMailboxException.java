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

package io.appulse.encon.java.module.mailbox.exception;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class NoSuchMailboxException extends RuntimeException {

  private static final long serialVersionUID = -4639023750789048749L;

  public NoSuchMailboxException() {
    super();
  }

  public NoSuchMailboxException(String message) {
    super(message);
  }

  public NoSuchMailboxException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoSuchMailboxException(Throwable cause) {
    super(cause);
  }
}
