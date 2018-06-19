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

package io.appulse.encon.config.exception;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * The exception indicates what mailbox handler class is unreachable.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class NoSuchMailboxHandlerException extends RuntimeException {

  private static final long serialVersionUID = 9031562526640890127L;

  String classPath;

  /**
   * Constructs a new runtime exception with the specified cause and a detail message
   * of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
   * <p>
   * This constructor is useful for runtime exceptions that are little more than wrappers for other throwables.
   *
   * @param cause     the cause (which is saved for later retrieval by the Throwable.getCause() method).
   *                  (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   *
   * @param classPath handler class path
   */
  public NoSuchMailboxHandlerException (Throwable cause, String classPath) {
    super(cause);
    this.classPath = classPath;
  }
}
