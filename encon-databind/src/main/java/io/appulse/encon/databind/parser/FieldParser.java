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

package io.appulse.encon.databind.parser;

import static io.appulse.encon.databind.deserializer.Deserializer.DESERIALIZERS;
import static io.appulse.encon.databind.deserializer.Deserializer.NEW_DESERIALIZER;
import static io.appulse.encon.databind.serializer.Serializer.NEW_SERIALIZER;
import static io.appulse.encon.databind.serializer.Serializer.SERIALIZERS;
import static java.util.Optional.ofNullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangTuple;
import io.appulse.encon.databind.annotation.TermDeserialize;
import io.appulse.encon.databind.annotation.TermOrder;
import io.appulse.encon.databind.annotation.TermSerialize;
import io.appulse.encon.databind.deserializer.Deserializer;
import io.appulse.encon.databind.deserializer.FieldDeserializerListOrTuple;
import io.appulse.encon.databind.deserializer.FieldDeserializerMap;
import io.appulse.encon.databind.deserializer.FieldDeserializerSet;
import io.appulse.encon.databind.serializer.FieldSerializerList;
import io.appulse.encon.databind.serializer.FieldSerializerMap;
import io.appulse.encon.databind.serializer.FieldSerializerTuple;
import io.appulse.encon.databind.serializer.Serializer;
import io.appulse.utils.AnnotationUtils;

import lombok.NonNull;
import lombok.val;

/**
 * Utility class for parsing POJO's fields to {@link FieldDescriptor}.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
public final class FieldParser {

  /**
   * Parses a POJO's field to {@link PojoDescriptor} instance.
   *
   * @param field a POJO's field
   *
   * @return {@link PojoDescriptor} instance
   */
  public static FieldDescriptor parse (@NonNull Field field) {
    AccessController.doPrivileged((PrivilegedAction<?>) () -> {
      field.setAccessible(true);
      return null;
    });

    val order = ofNullable(field.getAnnotation(TermOrder.class))
        .map(TermOrder::value)
        .orElse(0);

    Serializer<?> serializer;
    Deserializer<?> deserializer;

    if (field.isAnnotationPresent(AsErlangList.class)) {
      Class<?> genericType;
      if (field.getType().isArray()) {
        genericType = field.getType().getComponentType();
      } else if (String.class.isAssignableFrom(field.getType())) {
        genericType = Integer.class;
      } else {
        genericType = getGenericType(field, 0);
      }

      serializer = new FieldSerializerList(genericType);
      deserializer = new FieldDeserializerListOrTuple(genericType, field.getType());
    } else if (field.isAnnotationPresent(AsErlangTuple.class)) {
      Class<?> genericType;
      if (field.getType().isArray()) {
        genericType = field.getType().getComponentType();
      } else if (String.class.isAssignableFrom(field.getType())) {
        genericType = Integer.class;
      } else {
        genericType = getGenericType(field, 0);
      }

      serializer = new FieldSerializerTuple(genericType);
      deserializer = new FieldDeserializerListOrTuple(genericType, field.getType());
    } else {
      serializer = parseSerializer(field);
      deserializer = parseDeserializer(field);
    }

    return FieldDescriptor.builder()
        .field(field)
        .order(order)
        .serializer(serializer)
        .deserializer(deserializer)
        .build();
  }

  private static Serializer<?> parseSerializer (Field field) {
    val optional = AnnotationUtils.findAnnotation(field, TermSerialize.class)
        .map(TermSerialize::value)
        .map(it -> SERIALIZERS.computeIfAbsent(it, NEW_SERIALIZER));

    if (optional.isPresent()) {
      return optional.get();
    }

    Class<?> type = field.getType();
    Serializer result = Serializer.findInPredefined(type);
    if (result != null) {
      return result;
    }

    if (Collection.class.isAssignableFrom(type)) {
      Class<?> genericType = getGenericType(field, 0);
      return new FieldSerializerList(genericType);
    } else if (Map.class.isAssignableFrom(type)) {
      return FieldSerializerMap.builder()
          .keyClass(getGenericType(field, 0))
          .valueClass(getGenericType(field, 1))
          .build();
    }
    return null;
  }

  private static Deserializer<?> parseDeserializer (Field field) {
    val optional = AnnotationUtils.findAnnotation(field, TermDeserialize.class)
        .map(TermDeserialize::value)
        .map(it -> DESERIALIZERS.computeIfAbsent(it, NEW_DESERIALIZER));

    if (optional.isPresent()) {
      return optional.get();
    }

    Class<?> type = field.getType();
    Deserializer<?> result = Deserializer.findInPredefined(type);
    if (result != null) {
      return result;
    }

    if (List.class.isAssignableFrom(type)) {
      Class<?> genericType = getGenericType(field, 0);
      return new FieldDeserializerListOrTuple(genericType, field.getType());
    } else if (Set.class.isAssignableFrom(type)) {
      Class<?> genericType = getGenericType(field, 0);
      return new FieldDeserializerSet(genericType);
    } else if (Map.class.isAssignableFrom(type)) {
      return FieldDeserializerMap.builder()
          .keyClass(getGenericType(field, 0))
          .valueClass(getGenericType(field, 1))
          .build();
    }
    return null;
  }

  private static Class<?> getGenericType (Field field, int index) {
    Type genericType = field.getGenericType();
    ParameterizedType parameterizedType = (ParameterizedType) genericType;
    Type[] types = parameterizedType.getActualTypeArguments();
    return (Class<?>) types[index];
  }

  private FieldParser () {
  }
}
