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

package io.appulse.encon.java;

import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Value
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(exclude = "address")
public class NodeDescriptor implements Serializable {

  private static final long serialVersionUID = 7324588959922091097L;

  private static final InetAddress LOCALHOST;

  private static final InetAddress LOOPBACK_ADDRESS;

  private static final Map<String, InetAddress> INET_ADDRESS_CACHE;

  private static final Map<String, NodeDescriptor> NODE_DESCRIPTOR_CACHE;

  private static final Function<String, InetAddress> COMPUTE_INET_ADDRESS;

  private static final Function<String, NodeDescriptor> COMPUTE_NODE_DESCRIPTOR;

  static {
    try {
      LOCALHOST = InetAddress.getLocalHost();
      LOOPBACK_ADDRESS = InetAddress.getLoopbackAddress();
    } catch (UnknownHostException ex) {
      throw new IllegalArgumentException("Couldn't determine localhost address", ex);
    }

    INET_ADDRESS_CACHE = new ConcurrentHashMap<>();
    COMPUTE_INET_ADDRESS = key -> {
      try {
        return InetAddress.getByName(key);
      } catch (UnknownHostException ex) {
        throw new RuntimeException("Wrapped", ex);
      }
    };

    val hostname = LOCALHOST.getHostName().toLowerCase(ENGLISH);
    INET_ADDRESS_CACHE.put(hostname, LOCALHOST);
    if (hostname.endsWith(".local")) {
      val shortHostname = hostname.substring(0, hostname.length() - ".local".length());
      INET_ADDRESS_CACHE.put(shortHostname, LOOPBACK_ADDRESS);
    }
    INET_ADDRESS_CACHE.put(LOOPBACK_ADDRESS.getHostName().toLowerCase(ENGLISH), LOOPBACK_ADDRESS);

    NODE_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();
    COMPUTE_NODE_DESCRIPTOR = str -> {
      val tokens = str.split("@", 2);
      val shortName = tokens[0];
      val fullName = tokens.length == 2
                     ? str
                     : shortName + '@' + LOOPBACK_ADDRESS.getHostName();

      val address = tokens.length == 2
                    ? INET_ADDRESS_CACHE.computeIfAbsent(tokens[1].toLowerCase(ENGLISH), COMPUTE_INET_ADDRESS)
                    : LOOPBACK_ADDRESS;

      return new NodeDescriptor(shortName, fullName, address);
    };
  }

  @SneakyThrows
  public static NodeDescriptor from (String node) {
    val cached = NODE_DESCRIPTOR_CACHE.get(node);
    if (cached != null) {
      return cached;
    }
    return ofNullable(node)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(it -> it.toLowerCase(ENGLISH))
        .map(it -> NODE_DESCRIPTOR_CACHE.computeIfAbsent(it, COMPUTE_NODE_DESCRIPTOR))
        .orElseThrow(() -> new IllegalArgumentException("Invalid node descriptor string"));
  }

  String shortName;

  String fullName;

  InetAddress address;
}
