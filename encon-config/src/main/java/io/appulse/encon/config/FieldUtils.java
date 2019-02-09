/*
 * Copyright 2019 the original author or authors.
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

import static java.util.Optional.ofNullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.stream.Stream;

import lombok.SneakyThrows;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
final class FieldUtils {

  static boolean isIgnored (Field field) {
    int modifiers = field.getModifiers();
    if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
      return true;
    }
    FieldProperty fieldProperty = field.getAnnotation(FieldProperty.class);
    return fieldProperty != null && fieldProperty.ignore();
  }

  static String getName (Field field) {
    FieldProperty fieldProperty = field.getAnnotation(FieldProperty.class);
    return ofNullable(fieldProperty)
        .flatMap(prop -> Stream.of(prop.value(), prop.name())
            .filter(it -> !it.isEmpty())
            .findFirst()
        )
        .orElseGet(() -> field.getName());
  }

  static Class<?> getGenericType (Field field, int index) {
    Type genericType = field.getGenericType();
    ParameterizedType parameterizedType = (ParameterizedType) genericType;
    Type[] types = parameterizedType.getActualTypeArguments();
    return (Class<?>) types[index];
  }

  @SneakyThrows
  static void setValue (Object object, Field field, Object value) {
    String fieldName = field.getName();
    String setter = new StringBuilder()
        .append("set")
        .append(Character.toUpperCase(fieldName.charAt(0)))
        .append(fieldName.substring(1))
        .toString();

    try {
      object.getClass()
          .getDeclaredMethod(setter)
          .invoke(object, value);
    } catch (NoSuchMethodException ex) {
      AccessController.doPrivileged((PrivilegedAction<?>) () -> {
        field.setAccessible(true);
        return null;
      });
      field.set(object, value);
    }
  }

  private FieldUtils () {
  }
}
