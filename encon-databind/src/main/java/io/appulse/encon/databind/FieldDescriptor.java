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

package io.appulse.encon.databind;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import io.appulse.encon.databind.deserializer.Deserializer;
import io.appulse.encon.databind.serializer.Serializer;

import lombok.SneakyThrows;
import lombok.Value;

/**
 *
 * @author alabazin
 */
@Value
public final class FieldDescriptor {

  Field field;

  int order;

  Serializer<?> serializer;

  Deserializer<?> deserializer;

  @SneakyThrows
  public FieldDescriptor (Field field) {
    this.field = field;
    AccessController.doPrivileged((PrivilegedAction<?>) () -> {
      this.field.setAccessible(true);
      return null;
    });

    order = 1;
    serializer = null;
    deserializer = null;
  }

  public String getName () {
    return field.getName();
  }
}
