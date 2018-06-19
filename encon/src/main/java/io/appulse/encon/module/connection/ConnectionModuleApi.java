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

package io.appulse.encon.module.connection;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.appulse.encon.common.RemoteNode;

/**
 * Connection module API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface ConnectionModuleApi {

  /**
   * Asynchronous connection method to {@link RemoteNode}.
   *
   * @param remote remote node descriptor
   *
   * @return connection future container
   */
  CompletableFuture<Connection> connectAsync (RemoteNode remote);

  /**
   * Synchronous connection method to {@link RemoteNode}.
   *
   * @param remote remote node descriptor
   *
   * @return new or cached connection
   */
  Connection connect (RemoteNode remote);

  /**
   * Synchronous connection method to {@link RemoteNode}.
   *
   * @param remote  remote node descriptor
   *
   * @param timeout amount of units which need to wait of connection
   *
   * @param unit    timeout unit, like {@link TimeUnit#SECONDS}
   *
   * @return new or cached connection
   */
  Connection connect (RemoteNode remote, long timeout, TimeUnit unit);

  /**
   * Checks if a remote node is available or not.
   *
   * @param remote remote node descriptor
   *
   * @return {@code true} if node is accessable and {@code false} otherwise
   */
  boolean isAvailable (RemoteNode remote);
}
