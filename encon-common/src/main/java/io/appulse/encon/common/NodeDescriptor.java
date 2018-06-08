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

package io.appulse.encon.common;

import static java.util.Optional.of;
import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

/**
 * Node identifier.
 * <p>
 * It includes basic elements:
 * <p>
 * <ul>
 *   <li>short name</li>
 *   <li>full name</li>
 *   <li>inet address</li>
 * </ul>
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@EqualsAndHashCode(of = "fullName")
@AllArgsConstructor(access = PRIVATE)
public class NodeDescriptor implements Serializable {

  private static final long serialVersionUID = 7324588959922091097L;

  private static final Map<String, NodeDescriptor> NODE_DESCRIPTOR_CACHE;

  private static final Function<String, NodeDescriptor> COMPUTE_NODE_DESCRIPTOR;

  static {
    val loopback = InetAddress.getLoopbackAddress();
    NODE_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();
    COMPUTE_NODE_DESCRIPTOR = str -> {
      val tokens = str.split("@", 2);
      val shortName = tokens[0];
      val fullName = tokens.length == 2
                     ? str
                     : shortName + '@' + loopback.getHostName();

      val address = tokens.length == 2
                    ? getByName(tokens[1])
                    : loopback;

      return new NodeDescriptor(shortName, fullName, address);
    };
  }

  /**
   * Parses node descriptor from string.
   * <p>
   * A string could be short like <b>popa_node</b> and full, like <b>popa_node@192.168.0.32</b>.
   * <p>
   * Node descriptors are caching.
   *
   * @param node node name
   *
   * @return parsed {@link NodeDescriptor} instance
   */
  @SneakyThrows
  public static NodeDescriptor from (@NonNull String node) {
    val cached = NODE_DESCRIPTOR_CACHE.get(node);
    if (cached != null) {
      return cached;
    }
    return of(node)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(it -> NODE_DESCRIPTOR_CACHE.computeIfAbsent(it, COMPUTE_NODE_DESCRIPTOR))
        .orElseThrow(() -> new IllegalArgumentException("Invalid node descriptor string"));
  }

  @SuppressWarnings("PMD.PreserveStackTrace")
  private static InetAddress getByName (@NonNull String hostname) {
    try {
      return InetAddress.getByName(hostname);
    } catch (UnknownHostException ex) {
      try {
        return InetAddress.getByName(hostname + ".local");
      } catch (UnknownHostException ex1) {
        throw new IllegalArgumentException("Wrapped", ex);
      }
    }
  }

  String shortName;

  String fullName;

  InetAddress address;
}
