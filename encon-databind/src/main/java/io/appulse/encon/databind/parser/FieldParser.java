/*
 * Copyright 2018 Appulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangTuple;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.appulse.encon.databind.annotation.TermDeserialize;
import io.appulse.encon.databind.annotation.TermOrder;
import io.appulse.encon.databind.annotation.TermSerialize;
import io.appulse.encon.databind.deserializer.Deserializer;
import io.appulse.encon.databind.deserializer.ListAndTupleDeserializer;
import io.appulse.encon.databind.deserializer.MapDeserializer;
import io.appulse.encon.databind.deserializer.SetDeserializer;
import io.appulse.encon.databind.serializer.ListSerializer;
import io.appulse.encon.databind.serializer.MapSerializer;
import io.appulse.encon.databind.serializer.Serializer;
import io.appulse.encon.databind.serializer.TupleSerializer;
import io.appulse.utils.AnnotationUtils;

import lombok.val;

/**
 *
 * @author alabazin
 */
public final class FieldParser {

  public static FieldDescriptor parse (Field field) {
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

      serializer = new ListSerializer(genericType);
      deserializer = new ListAndTupleDeserializer(genericType, field.getType());
    } else if (field.isAnnotationPresent(AsErlangTuple.class)) {
      Class<?> genericType;
      if (field.getType().isArray()) {
        genericType = field.getType().getComponentType();
      } else if (String.class.isAssignableFrom(field.getType())) {
        genericType = Integer.class;
      } else {
        genericType = getGenericType(field, 0);
      }

      serializer = new TupleSerializer(genericType);
      deserializer = new ListAndTupleDeserializer(genericType, field.getType());
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
      return new ListSerializer(genericType);
    } else if (Map.class.isAssignableFrom(type)) {
      return MapSerializer.builder()
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
      return new ListAndTupleDeserializer(genericType, field.getType());
    } else if (Set.class.isAssignableFrom(type)) {
      Class<?> genericType = getGenericType(field, 0);
      return new SetDeserializer(genericType);
    } else if (Map.class.isAssignableFrom(type)) {
      return MapDeserializer.builder()
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
