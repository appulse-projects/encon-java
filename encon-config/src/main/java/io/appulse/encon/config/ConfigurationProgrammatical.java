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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@ToString
@SuppressWarnings("unchecked")
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = PRIVATE, makeFinal = true)
final class ConfigurationProgrammatical extends AbstractConfig {

  ConfigTree tree;

  private static boolean isClientPojo (Object object) {
    Class<?> type = object.getClass();
    String packageName = type.getPackage().getName();
    return !packageName.startsWith("java.");
  }

  @AllArgsConstructor(staticName = "of")
  private static class Tuple {

    Map<String, Object> map;

    String key;

    void put (Object value) {
      map.put(key, value);
    }
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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

  @SneakyThrows
  private static Map<String, Object> processObject (Object object) {
    Map<String, Object> result = new HashMap<>();
    Class<?> type = object.getClass();
    for (Field field : type.getDeclaredFields()) {
      if (FieldUtils.isIgnored(field)) {
        continue;
      }

      field.setAccessible(true);
      Object value = field.get(object);
      if (value == null) {
        continue;
      }

      result.put(FieldUtils.getName(field), processValue(value));
    }
    return result;
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
        .map(ConfigurationProgrammatical::processValue)
        .collect(toList());
  }

  private static Object processValue (Object object) {
    if (object instanceof Map) {
      return processMap((Map<String, Object>) object);
    } else if (object instanceof Collection) {
      return processList((Collection<Object>) object);
    } else if (object instanceof Enum) {
      return object.toString();
    } else if (isClientPojo(object)) {
      return processObject(object);
    }
    return object;
  }

  private static Object merge (Object left, Object right) {
    if (left == null) {
      return right;
    }
    if (right == null) {
      return right;
    }

    if ((left instanceof List) && (right instanceof List)) {
      List list = new ArrayList();
      list.addAll((List) left);
      list.addAll((List) right);
      return list;
    }

    if ((left instanceof Map) && (right instanceof Map)) {
      Map<String, Object> leftMap = (Map<String, Object>) left;
      Map<String, Object> rightMap = (Map<String, Object>) right;
      return merge(leftMap, rightMap);
    }

    throw new IllegalArgumentException(new StringBuilder()
        .append("Couldnt merge ")
        .append(left.toString())
        .append(" and ")
        .append(right.toString())
        .toString());
  }

  private static Map<String, Object> merge (Map<String, Object> left, Map<String, Object> right) {
    Map<String, Object> result = new HashMap<>(left);
    right.forEach((key, value) -> {
      result.merge(key, value, ConfigurationProgrammatical::merge);
    });
    return result;
  }

  @Builder
  private ConfigurationProgrammatical (@Singular List<Object> configs) {
    super();

    Map<String, Object> properties = configs.stream()
        .map(ConfigurationProgrammatical::processObject)
        .reduce(new HashMap<>(), ConfigurationProgrammatical::merge);

    tree = ConfigTree.from(properties);
  }

  @Override
  ConfigTree getConfigTree () {
    return tree;
  }
}
