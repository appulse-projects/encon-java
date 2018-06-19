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

package io.appulse.encon.module.lookup;

import java.util.Optional;

import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.terms.type.ErlangPid;

/**
 * Module for looking up remote node API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface LookupModuleApi {

  /**
   * Searches remote node (locally or on remote machine) by its name.
   *
   * @param name short (like 'node-name') or full (like 'node-name@example.com') remote node's name
   *
   * @return optional {@link RemoteNode} instance
   */
  Optional<RemoteNode> lookup (String name);

  /**
   * Searches remote node (locally or on remote machine) by its {@link ErlangPid}.
   *
   * @param pid remote's node pid
   *
   * @return optional {@link RemoteNode} instance
   */
  Optional<RemoteNode> lookup (ErlangPid pid);

  /**
   * Searches remote node (locally or on remote machine) by its identifier.
   *
   * @param descriptor identifier of the remote node
   *
   * @return optional {@link RemoteNode} instance
   */
  Optional<RemoteNode> lookup (NodeDescriptor descriptor);
}
