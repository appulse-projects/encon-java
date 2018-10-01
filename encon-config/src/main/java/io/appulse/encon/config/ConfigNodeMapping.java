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
import static io.appulse.encon.config.ConfigTree.NodeType.VALUE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.appulse.encon.config.ConfigTree.ConfigNode;
import io.appulse.encon.config.ConfigTree.ConfigNodeList;
import io.appulse.encon.config.ConfigTree.ConfigNodeMap;
import io.appulse.encon.config.ConfigTree.ConfigNodeValue;
import io.appulse.encon.config.exception.ConfigMappingEnumValueNotFoundException;
import io.appulse.encon.config.exception.ConfigMappingException;
import io.appulse.encon.config.exception.ConfigMappingNoMappingFunctionException;
import io.appulse.encon.config.exception.ConfigMappingUnsupportedTypeException;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@SuppressWarnings("unchecked")
final class ConfigNodeMapping {

  static <T> T map (@NonNull ConfigNode node, @NonNull Class<T> type) {
    if (type == Object.class) {
      return (T) node.toObject();
    } else if (type.isEnum()) {
      return toEnum(node, type);
    } else if (node.getType() == VALUE) {
      return toSimpleType((ConfigNodeValue) node, type);
    } else if (node.getType() == LIST && Collection.class.isAssignableFrom(type)) {
      return toCollection(node, type);
    } else if (node.getType() == MAP) {
      return toMap(node, type);
    }
    throw new ConfigMappingUnsupportedTypeException(type);
  }

  static <T> Map<String, T> toGenericMap (ConfigNodeMap node, Class<T> valueType) {
    return node.getNodes()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, it -> map(it.getValue(), valueType)));
  }

  static <T> List<T> toGenericList (ConfigNodeList node, Class<T> valueType) {
    return node.getNodes()
        .stream()
        .map(it -> map(it, valueType))
        .collect(toList());
  }

  static <T> Set<T> toGenericSet (ConfigNodeList node, Class<T> valueType) {
    return node.getNodes()
        .stream()
        .map(it -> map(it, valueType))
        .collect(toSet());
  }

  private static Object fromString (ConfigNode node, Function<String, Object> parser) {
    return ofNullable(node.toObject())
        .map(Object::toString)
        .map(parser)
        .orElseThrow(ConfigMappingException::new);
  }

  private static <T> T toSimpleType (@NonNull ConfigNodeValue node, @NonNull Class<T> type) {
    if (type == Object.class) {
      return (T) node.toObject();
    } else if (type == Character.class || type == Character.TYPE) {
      return (T) fromString(node, it -> it.toCharArray()[0]);
    } else if (type == Byte.class || type == Byte.TYPE) {
      return (T) fromString(node, Byte::parseByte);
    } else if (type == Short.class || type == Short.TYPE) {
      return (T) fromString(node, Short::parseShort);
    } else if (type == Integer.class || type == Integer.TYPE) {
      return (T) fromString(node, Integer::parseInt);
    } else if (type == Long.class || type == Long.TYPE) {
      return (T) fromString(node, Long::parseLong);
    } else if (type == Float.class || type == Float.TYPE) {
      return (T) fromString(node, Float::parseFloat);
    } else if (type == Double.class || type == Double.TYPE) {
      return (T) fromString(node, Double::parseDouble);
    } else if (type == Boolean.class || type == Boolean.TYPE) {
      return (T) fromString(node, it -> "yes".equalsIgnoreCase(it) ||
                                        "1".equalsIgnoreCase(it) ||
                                        Boolean.parseBoolean(it));
    } else if (type == BigInteger.class) {
      return (T) fromString(node, BigInteger::new);
    } else if (type == BigDecimal.class) {
      return (T) fromString(node, BigDecimal::new);
    } else if (type == String.class || type == CharSequence.class) {
      return (T) fromString(node, it -> it);
    }
    throw new ConfigMappingNoMappingFunctionException(type, node.toObject().toString());
  }

  private static <T> T toCollection (ConfigNode node, Class<T> type) {
    List<Object> list = (List<Object>) node.toObject();
    return Set.class.isAssignableFrom(type)
           ? (T) list.stream().collect(toSet())
           : (T) list;
  }

  @SneakyThrows
  private static <T> T toMap (ConfigNode node, Class<T> type) {
    if (Map.class.isAssignableFrom(type)) {
      return (T) node.toObject();
    }

    T result = type.getConstructor().newInstance();
    Map<String, ConfigNode> nodes = ((ConfigNodeMap) node).getNodes();
    for (Field field : type.getDeclaredFields()) {
      if (FieldUtils.isIgnored(field)) {
        continue;
      }

      String key = FieldUtils.getName(field);
      ConfigNode leaf = nodes.get(key);
      if (leaf == null) {
        continue;
      }

      Class<?> fieldType = field.getType();
      Object value;
      if (Map.class.isAssignableFrom(fieldType)) {
        value = toGenericMap((ConfigNodeMap) leaf, FieldUtils.getGenericType(field, 1));
      } else if (Set.class.isAssignableFrom(fieldType)) {
        value = toGenericSet((ConfigNodeList) leaf, FieldUtils.getGenericType(field, 0));
      } else if (Collection.class.isAssignableFrom(fieldType)) {
        value = toGenericList((ConfigNodeList) leaf, FieldUtils.getGenericType(field, 0));
      } else {
        value = map(leaf, fieldType);
      }

      FieldUtils.setValue(result, field, value);
    }
    return result;
  }

  private static <T> T toEnum (ConfigNode node, Class<T> type) {
    String nodeAsString = node.toObject().toString();

    for (Object enumObject : type.getEnumConstants()) {
      Enum enumItem = (Enum) enumObject;
      String name = enumItem.name();
      String toString = enumItem.toString();
      if (nodeAsString.equalsIgnoreCase(name) || nodeAsString.equalsIgnoreCase(toString)) {
        return (T) enumItem;
      }
    }
    throw new ConfigMappingEnumValueNotFoundException(type, nodeAsString);
  }

  private ConfigNodeMapping () {
  }
}
