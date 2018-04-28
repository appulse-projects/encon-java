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
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@Value
@EqualsAndHashCode(of = "fullName")
@AllArgsConstructor(access = PRIVATE)
public class NodeDescriptor implements Serializable {

  private static final long serialVersionUID = 7324588959922091097L;

  private static final Map<String, InetAddress> INET_ADDRESS_CACHE;

  private static final Map<String, NodeDescriptor> NODE_DESCRIPTOR_CACHE;

  private static final Function<String, InetAddress> COMPUTE_INET_ADDRESS;

  private static final Function<String, NodeDescriptor> COMPUTE_NODE_DESCRIPTOR;

  static {
    InetAddress localhost;
    InetAddress loopback;
    try {
      localhost = InetAddress.getLocalHost();
      loopback = InetAddress.getLoopbackAddress();
    } catch (UnknownHostException ex) {
      throw new IllegalArgumentException("Couldn't determine localhost address", ex);
    }

    INET_ADDRESS_CACHE = new ConcurrentHashMap<>();
    COMPUTE_INET_ADDRESS = key -> {
      try {
        return InetAddress.getByName(key);
      } catch (UnknownHostException ex) {
        try {
          log.warn("Unknown host {}, trying to lookup {}.local", key, key);
          return InetAddress.getByName(key + ".local");
        } catch (UnknownHostException ex1) {
          throw new RuntimeException("Wrapped", ex);
        }
      }
    };

    INET_ADDRESS_CACHE.put(localhost.getHostName(), localhost);
    INET_ADDRESS_CACHE.put(loopback.getHostName(), loopback);

    NODE_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();
    COMPUTE_NODE_DESCRIPTOR = str -> {
      val tokens = str.split("@", 2);
      val shortName = tokens[0];
      val fullName = tokens.length == 2
                     ? str
                     : shortName + '@' + loopback.getHostName();

      val address = tokens.length == 2
                    ? getByName(tokens[1]) // INET_ADDRESS_CACHE.computeIfAbsent(tokens[1], COMPUTE_INET_ADDRESS)
                    : loopback;

      return new NodeDescriptor(shortName, fullName, address);
    };
  }

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

  private static InetAddress getByName (@NonNull String hostname) {
    try {
      return InetAddress.getByName(hostname);
    } catch (UnknownHostException ex) {
      try {
        log.warn("Unknown host {}, trying to lookup {}.local", hostname, hostname);
        return InetAddress.getByName(hostname + ".local");
      } catch (UnknownHostException ex1) {
        throw new RuntimeException("Wrapped", ex);
      }
    }
  }

  String shortName;

  String fullName;

  InetAddress address;
}
