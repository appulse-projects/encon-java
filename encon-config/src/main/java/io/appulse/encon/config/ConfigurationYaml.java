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

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = PRIVATE, makeFinal = true)
final class ConfigurationYaml extends AbstractConfig {

  @AllArgsConstructor(staticName = "of")
  private static class Tuple {

    Map<String, Object> map;

    String key;

    void put (Object value) {
      map.put(key, value);
    }
  }

  @SuppressWarnings({ "unchecked", "PMD.AvoidInstantiatingObjectsInLoops"})
  private static Tuple unfoldKey (Map<String, Object> map, String key) {
    Map<String, Object> current = map;
    String currentKey = key;

    if (!currentKey.contains(".")) {
      return Tuple.of(current, currentKey);
    }

    String[] tokens = currentKey.split("\\.");
    int lastIndex = tokens.length - 1;
    for (int index = 0; index < lastIndex; index++) {
      Map<String, Object> temporary = (Map<String, Object>) current.get(tokens[index]);
      if (temporary == null) {
        temporary = new HashMap<>(2, 1.F);
        current.put(tokens[index], temporary);
      }
      current = temporary;
    }
    currentKey = tokens[lastIndex];
    return Tuple.of(current, currentKey);
  }

  private static Map<String, Object> processMap (Map<String, Object> map) {
    Map<String, Object> result = new HashMap<>();
    for (Entry<String, Object> entry : map.entrySet()) {
      Tuple currentMap = unfoldKey(result, entry.getKey());
      Object value = processValue(entry.getValue());
      currentMap.put(value);
    }
    return result;
  }

  private static List<Object> processList (Collection<Object> list) {
    return list.stream()
        .map(ConfigurationYaml::processValue)
        .collect(toList());
  }

  @SuppressWarnings("unchecked")
  private static Object processValue (Object object) {
    if (object instanceof Map) {
      return processMap((Map<String, Object>) object);
    } else if (object instanceof Collection) {
      return processList((Collection<Object>) object);
    } else if (object instanceof Enum) {
      return object.toString();
    }
    return object;
  }

  ConfigTree tree;

  ConfigurationYaml (@NonNull Map<String, Object> properties) {
    super();
    Map<String, Object> unfoldedProperties = processMap(properties);
    tree = ConfigTree.from(unfoldedProperties);
  }

  @Override
  ConfigTree getConfigTree () {
    return tree;
  }
}
