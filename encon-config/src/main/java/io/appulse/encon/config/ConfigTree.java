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

package io.appulse.encon.config;

import static io.appulse.encon.config.ConfigTree.NodeType.LIST;
import static io.appulse.encon.config.ConfigTree.NodeType.MAP;
import static io.appulse.encon.config.ConfigTree.NodeType.UNDEFINED;
import static io.appulse.encon.config.ConfigTree.NodeType.VALUE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ConfigTree {

  static ConfigTree from (Map<String, Object> map) {
    ConfigNodeMap root = processMap(map);
    return new ConfigTree(root);
  }

  private static ConfigNodeMap processMap (Map<String, Object> map) {
    ConfigNodeMap.ConfigNodeMapBuilder builder = ConfigNodeMap.builder();
    map.entrySet()
        .stream()
        .forEach(it -> {
          ConfigNode value = processValue(it.getValue());
          builder.node(it.getKey(), value);
        });
    return builder.build();
  }

  private static ConfigNodeList processList (List<Object> list) {
    ConfigNodeList.ConfigNodeListBuilder builder = ConfigNodeList.builder();
    list.stream()
        .map(ConfigTree::processValue)
        .forEach(builder::node);

    return builder.build();
  }

  @SuppressWarnings("unchecked")
  private static ConfigNode processValue (Object object) {
    if (object instanceof Map) {
      return processMap((Map<String, Object>) object);
    } else if (object instanceof List) {
      return processList((List<Object>) object);
    }
    return new ConfigNodeValue(object);
  }

  private static Optional<Deque<String>> toKey (String key) {
    return ofNullable(key)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(it -> it.split("\\."))
        .map(it -> Stream.of(it)
            .map(String::trim)
            .filter(token -> !token.isEmpty())
            .collect(toList())
        )
        .filter(it -> !it.isEmpty())
        .map(ArrayDeque::new);
  }

  @NonNull
  ConfigNodeMap root;

  @SuppressWarnings("unchecked")
  Map<String, Object> toMap () {
    return (Map<String, Object>) root.toObject();
  }

  boolean containsKey (@NonNull String key) {
    return toKey(key)
        .map(root::containsKey)
        .orElse(false);
  }

  ConfigNode get (String key) {
    return toKey(key)
        .map(root::get)
        .orElse(root);
  }

  enum NodeType {

    VALUE,
    MAP,
    LIST,
    UNDEFINED;

    static NodeType of (Object object) {
      if (object == null) {
        return UNDEFINED;
      } else if (object instanceof Map) {
        return MAP;
      } else if (object instanceof Collection) {
        return LIST;
      }
      return UNDEFINED;
    }
  }

  interface ConfigNode {

    boolean containsKey (@NonNull Deque<String> key);

    ConfigNode get (@NonNull Deque<String> key);

    Object toObject ();

    default NodeType getType () {
      return UNDEFINED;
    }
  }

  @Value
  static class ConfigNodeValue implements ConfigNode {

    @NonNull
    Object value;

    @Override
    public ConfigNode get (Deque<String> key) {
      throw new UnsupportedOperationException("");
    }

    @Override
    public boolean containsKey (Deque<String> key) {
      throw new UnsupportedOperationException("");
    }

    @Override
    public Object toObject () {
      return value;
    }

    @Override
    public NodeType getType () {
      return VALUE;
    }
  }

  @Value
  @Builder
  static class ConfigNodeMap implements ConfigNode {

    @Singular
    Map<String, ConfigNode> nodes;

    @Override
    public ConfigNode get (Deque<String> key) {
      val token = key.pop();
      val node = nodes.get(token);
      if (node == null) {
        return null;
      }
      return key.isEmpty()
             ? node
             : node.get(key);
    }

    @Override
    public boolean containsKey (Deque<String> key) {
      val token = key.pop();
      val node = nodes.get(token);
      if (node == null) {
        return false;
      }
      return key.isEmpty() || node.containsKey(key);
    }

    @Override
    public Object toObject () {
      return nodes
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, it -> it.getValue().toObject()));
    }

    @Override
    public NodeType getType () {
      return MAP;
    }
  }

  @Value
  @Builder
  static class ConfigNodeList implements ConfigNode {

    private static int parseIndex (String token) {
      val indexString = token.charAt(0) == '[' && token.endsWith("]")
                        ? token.substring(1, token.length() - 1)
                        : token;
      try {
        return Integer.parseInt(indexString);
      } catch (NumberFormatException ex) {
        return -1;
      }
    }

    @Singular
    List<ConfigNode> nodes;

    @Override
    public ConfigNode get (Deque<String> key) {
      val token = key.pop();
      val index = parseIndex(token);
      if (index < 0 || index >= nodes.size()) {
        return null;
      }

      val node = nodes.get (index);
      return key.isEmpty()
             ? node
             : node.get(key);
    }

    @Override
    public boolean containsKey (Deque<String> key) {
      val token = key.pop();
      val index = parseIndex(token);
      if (index < 0 || index >= nodes.size()) {
        return false;
      }

      val node = nodes.get(index);
      return key.isEmpty() || node.containsKey(key);
    }

    @Override
    public Object toObject () {
      return nodes.stream()
          .map(ConfigNode::toObject)
          .collect(toList());
    }

    @Override
    public NodeType getType () {
      return LIST;
    }
  }
}
