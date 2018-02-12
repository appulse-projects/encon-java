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

package io.appulse.encon.java.module.ping;

import java.util.concurrent.CompletableFuture;

import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
public interface PingModuleApi {

  /**
   * Ping some.
   *
   * @param node node
   *
   * @return future
   */
  CompletableFuture<Boolean> ping (String node);

  CompletableFuture<Boolean> ping (NodeDescriptor remoteDescriptor);

  CompletableFuture<Boolean> ping (RemoteNode remote);
}
