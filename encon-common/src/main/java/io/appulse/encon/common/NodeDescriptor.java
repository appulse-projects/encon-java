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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
@SuppressWarnings({
    "PMD.AvoidThrowingRawExceptionTypes",
    "PMD.AvoidLiteralsInIfCondition"
})
@SuppressFBWarnings("RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED")
public class NodeDescriptor implements Serializable {

  private static final long serialVersionUID = 7324588959922091097L;

  private static final Map<String, NodeDescriptor> NODE_DESCRIPTOR_CACHE;

  private static final InetAddress LOCALHOST;

  private static final InetAddress LOOPBACK;

  private static final String HOST_NAME;

  static {
    try {
      LOCALHOST = InetAddress.getLocalHost();
    } catch (UnknownHostException ex) {
      throw new RuntimeException(ex);
    }
    LOOPBACK = InetAddress.getLoopbackAddress();
    HOST_NAME = LOCALHOST.getHostName();
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
    NODE_DESCRIPTOR_CACHE.putIfAbsent(node, descriptor);
    NODE_DESCRIPTOR_CACHE.putIfAbsent(descriptor.getFullName(), descriptor);
    NODE_DESCRIPTOR_CACHE.putIfAbsent(descriptor.getNodeName(), descriptor);
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
    NODE_DESCRIPTOR_CACHE.putIfAbsent(node, descriptor);
    NODE_DESCRIPTOR_CACHE.putIfAbsent(descriptor.getFullName(), descriptor);
    NODE_DESCRIPTOR_CACHE.putIfAbsent(descriptor.getNodeName(), descriptor);
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
    val tokens = node.split("@", 2);

    val nodeName = tokens[0];
    if (nodeName.isEmpty()) {
      throw new IllegalArgumentException();
    }

    String hostName;
    InetAddress address;
    if (tokens.length == 2) {
      hostName = tokens[1];
      if (isShortName && hostName.contains(".")) {
        val message = String.format("Using host name '%s' in short node name is illegal, " +
                                    "because it contains dots ('.') and suits only for long name nodes",
                                    hostName);
        throw new IllegalArgumentException(message);
      }
      address = InetAddress.getByName(hostName);
    } else {
      hostName = isShortName
                 ? HOST_NAME.substring(HOST_NAME.indexOf('.'))
                 : HOST_NAME;

      address = isShortName
                ? LOOPBACK
                : LOCALHOST;
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
