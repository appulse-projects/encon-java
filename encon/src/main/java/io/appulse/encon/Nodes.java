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

package io.appulse.encon;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.config.Config;
import io.appulse.encon.config.Defaults;
import io.appulse.encon.config.NodeConfig;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * The set of different helper functions
 * for management nodes cluster within one Java process.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class Nodes implements Iterable<Node>, Closeable {

  /**
   * Creates blank set of nodes with default config.
   *
   * @return a new {@link Nodes} instance
   */
  public static Nodes start () {
    val config = Config.builder().build();
    return start(config);
  }

  /**
   * Creates blank set of nodes with specified config.
   *
   * @param config a node's config
   *
   * @return a new {@link Nodes} instance
   */
  public static Nodes start (@NonNull Config config) {
    val copy = new Config(config);
    log.debug("Creating ERTS instance with config {}", copy);

    val erts = new Nodes(copy.getDefaults(), new ConcurrentHashMap<>());
    copy.getNodes()
        .entrySet()
        .forEach(it -> erts.newNode(it.getKey(), it.getValue()));

    return erts;
  }

  /**
   * Creates single {@link Node} instance with specific name.
   *
   * @param name short (like 'node-name') or full (like 'node-name@example.com') node's name
   *
   * @return new {@link Node} instance
   */
  public static Node singleNode (@NonNull String name) {
    return singleNode(name, NodeConfig.DEFAULT);
  }

  /**
   * Creates single {@link Node} instance with specific name.
   *
   * @param name         short (like 'node-name') or full (like 'node-name@example.com') node's name
   *
   * @param isShortNamed indicates is this node with short name or not
   *
   * @return new {@link Node} instance
   */
  public static Node singleNode (@NonNull String name, boolean isShortNamed) {
    return singleNode(name, NodeConfig.builder()
                      .shortName(isShortNamed)
                      .build());
  }

  /**
   * Creates single {@link Node} instance with specific name and config.
   *
   * @param name       short (like 'node-name') or full (like 'node-name@example.com') node's name
   *
   * @param nodeConfig new node's config
   *
   * @return new {@link Node} instance
   */
  public static Node singleNode (@NonNull String name, @NonNull NodeConfig nodeConfig) {
    val copy = new NodeConfig(nodeConfig);
    copy.withDefaultsFrom(Defaults.INSTANCE);
    return Node.newInstance(name, copy);
  }

  Defaults defaults;

  Map<NodeDescriptor, Node> nodes;

  /**
   * Creates a new node with short (like 'node-name') or full (like 'node-name@example.com') name.
   *
   * @param name short (like 'node-name') or full (like 'node-name@example.com') node's name
   *
   * @return new {@link Node} instance
   */
  public Node newNode (@NonNull String name) {
    val nodeConfig = NodeConfig.builder().build();
    return newNode(name, nodeConfig);
  }

  /**
   * Creates a new node with short (like 'node-name') or full (like 'node-name@example.com') name.
   *
   * @param name         short (like 'node-name') or full (like 'node-name@example.com') node's name
   *
   * @param isShortNamed indicates is this node with short name or not
   *
   * @return new {@link Node} instance
   */
  public Node newNode (@NonNull String name, boolean isShortNamed) {
    val nodeConfig = NodeConfig.builder()
        .shortName(isShortNamed)
        .build();
    return newNode(name, nodeConfig);
  }

  /**
   * Creates a new node with short (like 'node-name') or full (like 'node-name@example.com') name
   * and its config.
   *
   * @param name       short (like 'node-name') or full (like 'node-name@example.com') node's name
   *
   * @param nodeConfig new node's config
   *
   * @return new {@link Node} instance
   */
  public Node newNode (@NonNull String name, @NonNull NodeConfig nodeConfig) {
    val copy = new NodeConfig(nodeConfig);
    copy.withDefaultsFrom(defaults);
    val node = Node.newInstance(name, copy);
    nodes.put(node.getDescriptor(), node);
    return node;
  }

  /**
   * Searching a node by it's short (like 'node-name') or full (like 'node-name@example.com') name.
   *
   * @param name short (like 'node-name') or full (like 'node-name@example.com') node's name
   *
   * @return optional value, which could contains a searching node
   */
  public Optional<Node> node (@NonNull String name) {
    val descriptor = NodeDescriptor.from(name);
    return node(descriptor);
  }

  /**
   * Searching a node by it's identifier.
   *
   * @param descriptor identifier of the node
   *
   * @return optional value, which could contains a searching node
   */
  public Optional<Node> node (@NonNull NodeDescriptor descriptor) {
    return ofNullable(nodes.get(descriptor));
  }

  /**
   * Returns collection of all available local nodes.
   *
   * @return collection of all available local nodes.
   */
  public Collection<Node> nodes () {
    return nodes.values();
  }

  /**
   * Removes the mapping for a name from this nodes cluster if it is present.
   *
   * @param name short (like 'node-name') or full (like 'node-name@example.com') node's name
   *
   * @return the previous value associated with a name, or {@code null} if there was no mapping for name.
   */
  public Node remove (@NonNull String name) {
    val descriptor = NodeDescriptor.from(name);
    return remove(descriptor);
  }

  /**
   * Removes the mapping for a identifier from this nodes cluster if it is present.
   *
   * @param descriptor identifier of the node
   *
   * @return the previous value associated with a identifier,
   *         or {@code null} if there was no mapping for identifier.
   */
  public Node remove (@NonNull NodeDescriptor descriptor) {
    return nodes.remove(descriptor);
  }

  @Override
  public void close () {
    nodes.values().forEach(Node::close);
    nodes.clear();
  }

  @Override
  public Iterator<Node> iterator () {
    return nodes().iterator();
  }
}
