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

import java.lang.reflect.Field;

import io.appulse.encon.databind.deserializer.Deserializer;
import io.appulse.encon.databind.serializer.Serializer;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

/**
 * Value type for describing user's fields.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@Value
@Builder
@ToString(of = "field")
public final class FieldDescriptor {

  Field field;

  int order;

  Serializer<?> serializer;

  Deserializer<?> deserializer;

  /**
   * Returns field's name.
   *
   * @return field's name
   */
  public String getName () {
    return field.getName();
  }
}
