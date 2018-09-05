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

package io.appulse.encon.handler.mailbox;

import java.io.Closeable;

/**
 * Mailbox wrapper for automatic handling received messages.
 *
 * @since 1.4.0
 * @author Artem Labazin
 */
public interface MailboxHandler extends Closeable {

  /**
   * Starts single thread executor for long-term messages handling.
   */
  void startExecutor ();

  /**
   * One-time message handling. It is applicable for the situations, for example,
   * when you already have your own executor service.
   */
  void oneTimeShot ();

  /**
   * Overrides {@link Closeable#close} method without a checked exception.
   */
  @Override
  void close ();
}
