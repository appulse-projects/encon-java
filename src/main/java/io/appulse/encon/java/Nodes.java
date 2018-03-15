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

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.appulse.encon.java.config.Config;
import io.appulse.encon.java.config.Defaults;
import io.appulse.encon.java.config.NodeConfig;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class Nodes implements Closeable {

  public static Nodes start () {
    val config = Config.builder().build();
    return start(config);
  }

  public static Nodes start (@NonNull Config config) {
    log.debug("Creating ERTS instance with config {}", config);

    val erts = new Nodes(config.getDefaults(), new ConcurrentHashMap<>());
    config.getNodes()
        .entrySet()
        .forEach(it -> erts.newNode(it.getKey(), it.getValue()));

    return erts;
  }

  public static Node singleNode (@NonNull String name) {
    return singleNode(name, NodeConfig.DEFAULT);
  }

  public static Node singleNode (@NonNull String name, @NonNull NodeConfig nodeConfig) {
    nodeConfig.withDefaultsFrom(Defaults.INSTANCE);
    return Node.newInstance(name, nodeConfig);
  }

  Defaults defaults;

  Map<NodeDescriptor, Node> nodes;

  public Node newNode (@NonNull String name) {
    val nodeConfig = NodeConfig.builder().build();
    return newNode(name, nodeConfig);
  }

  public Node newNode (@NonNull String name, @NonNull NodeConfig nodeConfig) {
    nodeConfig.withDefaultsFrom(defaults);
    val node = Node.newInstance(name, nodeConfig);
    nodes.put(node.getDescriptor(), node);
    return node;
  }

  public Optional<Node> node (@NonNull String node) {
    val descriptor = NodeDescriptor.from(node);
    return node(descriptor);
  }

  public Optional<Node> node (@NonNull NodeDescriptor descriptor) {
    return ofNullable(nodes.get(descriptor));
  }

  public Collection<Node> nodes () {
    return nodes.values();
  }

  public Node remove (@NonNull String node) {
    val descriptor = NodeDescriptor.from(node);
    return remove(descriptor);
  }

  public Node remove (@NonNull NodeDescriptor descriptor) {
    return nodes.remove(descriptor);
  }

  @Override
  public void close () {
    nodes.values().forEach(Node::close);
    nodes.clear();
  }
}
