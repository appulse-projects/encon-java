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
import java.util.stream.Stream;

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
 * <li>short name</li>
 * <li>full name</li>
 * <li>inet address</li>
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

  private static final InetAddress LOCALHOST;

  private static final InetAddress LOOPBACK;

  private static final String LONG_HOST_NAME;

  private static final String SHORT_HOST_NAME;

  static {
    try {
      LOCALHOST = InetAddress.getLocalHost();
    } catch (UnknownHostException ex) {
      throw new IllegalStateException(ex);
    }
    LOOPBACK = InetAddress.getLoopbackAddress();
    LONG_HOST_NAME = LOCALHOST.getHostName();
    val dotIndex = LONG_HOST_NAME.indexOf('.');
    SHORT_HOST_NAME = dotIndex > 0
                      ? LONG_HOST_NAME.substring(0, dotIndex)
                      : LONG_HOST_NAME;
    NODE_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();
  }

  /**
   * Parses node descriptor from string.
   * <p>
   * A string could be short like <b>popa_node</b> and long, like <b>popa_node@192.168.0.32</b>.
   * <p>
   * Node descriptors are caching.
   *
   * @param node node name
   *
   * @return parsed {@link NodeDescriptor} instance
   */
  public static NodeDescriptor from (@NonNull String node) {
    val cached = NODE_DESCRIPTOR_CACHE.get(node);
    if (cached != null) {
      return cached;
    }
    val descriptor = getFromCacheOrCreateNew(node);
    Stream.of(
        descriptor.getFullName(),
        descriptor.getNodeName(),
        node
    ).forEach(it -> NODE_DESCRIPTOR_CACHE.putIfAbsent(it, descriptor));
    return descriptor;
  }

  /**
   * Parses node descriptor from string.
   * <p>
   * A string could be short like <b>popa_node</b> and long, like <b>popa_node@192.168.0.32</b>.
   * <p>
   * Node descriptors are caching.
   *
   * @param node        node name
   *
   * @param isShortName indicates is this node name short or long
   *
   * @return parsed {@link NodeDescriptor} instance
   */
  public static NodeDescriptor from (@NonNull String node, boolean isShortName) {
    val cached = NODE_DESCRIPTOR_CACHE.get(node);
    if (cached != null) {
      return cached;
    }
    val descriptor = getFromCacheOrCreateNew(node, isShortName);
    Stream.of(
        descriptor.getFullName(),
        descriptor.getNodeName(),
        node
    ).forEach(it -> NODE_DESCRIPTOR_CACHE.putIfAbsent(it, descriptor));
    return descriptor;
  }

  /**
   * Checks, if this node cached or not.
   *
   * @param node node name
   *
   * @return {@code true}, if node descriptor was cached, or
   *         {@code false} if there was no such node
   */
  public static boolean wasCached (@NonNull String node) {
    val descriptor = getFromCacheOrCreateNew(node);
    return wasCached(descriptor);
  }

  /**
   * Checks, if this node cached or not.
   *
   * @param descriptor node descriptor
   *
   * @return {@code true}, if node descriptor was cached, or
   *         {@code false} if there was no such node
   */
  public static boolean wasCached (@NonNull NodeDescriptor descriptor) {
    return NODE_DESCRIPTOR_CACHE.containsKey(descriptor.getNodeName()) ||
           NODE_DESCRIPTOR_CACHE.containsKey(descriptor.getFullName());
  }

  /**
   * Removes a node from cached values.
   *
   * @param node node name
   *
   * @return {@code true}, if node descriptor was removed, or
   *         {@code false} if there was no such node
   */
  public static boolean removeFromCache (@NonNull String node) {
    val descriptor = getFromCacheOrCreateNew(node);
    return removeFromCache(descriptor);
  }

  /**
   * Removes a node from cached values.
   *
   * @param descriptor node descriptor
   *
   * @return {@code true}, if node descriptor was removed, or
   *         {@code false} if there was no such node
   */
  public static boolean removeFromCache (@NonNull NodeDescriptor descriptor) {
    val wasRemoved = NODE_DESCRIPTOR_CACHE.remove(descriptor.getNodeName()) != null;
    return NODE_DESCRIPTOR_CACHE.remove(descriptor.getFullName()) != null || wasRemoved;
  }

  private static NodeDescriptor getFromCacheOrCreateNew (@NonNull String node) {
    val atIndex = node.indexOf('@');
    val isShortName = atIndex < 0 || node.indexOf('.', atIndex) < 0;
    return getFromCacheOrCreateNew(node, isShortName);
  }

  private static NodeDescriptor getFromCacheOrCreateNew (@NonNull String node, boolean isShortName) {
    String trimmedNode = of(node)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException("Invalid node descriptor string '" + node + "'"));

    val cached = NODE_DESCRIPTOR_CACHE.get(trimmedNode);
    return cached == null
           ? parse(trimmedNode, isShortName)
           : cached;
  }

  @SneakyThrows
  private static NodeDescriptor parse (@NonNull String node, boolean isShortName) {
    val expectedTokensCount = 2;
    val tokens = node.split("@", expectedTokensCount);

    val nodeName = tokens[0];
    if (nodeName.isEmpty()) {
      throw new IllegalArgumentException();
    }

    String hostName;
    InetAddress address;
    if (tokens.length == expectedTokensCount) {
      hostName = tokens[1];
      if (isShortName && hostName.contains(".")) {
        val message = String.format("Using host name '%s' in short node name is illegal, " +
                                    "because it contains dots ('.') and suits only for long name nodes",
                                    hostName);
        throw new IllegalArgumentException(message);
      }
      address = isShortName
                ? LOOPBACK
                : InetAddress.getByName(hostName);
    } else if (isShortName) {
      hostName = SHORT_HOST_NAME;
      address = LOOPBACK;
    } else {
      hostName = LONG_HOST_NAME;
      address = LOCALHOST;
    }

    val fullName = nodeName + '@' + hostName;
    return new NodeDescriptor(nodeName, hostName, fullName, address, isShortName);
  }

  String nodeName;

  String hostName;

  String fullName;

  InetAddress address;

  boolean shortName;

  /**
   * Tells is this {@link NodeDescriptor} instance for short-named node or not.
   *
   * @return {@code true} if node name is short, {@code false} otherwise
   */
  public boolean isShortName () {
    return shortName;
  }

  /**
   * Tells is this {@link NodeDescriptor} instance for long-named node or not.
   *
   * @return {@code true} if node name is long, {@code false} otherwise
   */
  public boolean isLongName () {
    return !shortName;
  }
}
