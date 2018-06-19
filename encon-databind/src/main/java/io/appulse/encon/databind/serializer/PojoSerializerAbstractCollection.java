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

package io.appulse.encon.databind.serializer;

import io.appulse.encon.databind.parser.FieldDescriptor;
import io.appulse.encon.terms.ErlangTerm;

import lombok.SneakyThrows;
import lombok.val;

/**
 * Abstract collection serializer.
 *
 * @since 1.1.0
 * @author alabazin
 */
abstract class PojoSerializerAbstractCollection<T> implements Serializer<T> {

  /**
   * Serializes field into {@link ErlangTerm} instance by its {@link FieldDescriptor}.
   *
   * @param descriptor field's descriptor
   *
   * @param object     field's concrete value
   *
   * @return serialized {@link ErlangTerm} instance
   */
  @SneakyThrows
  protected final ErlangTerm serialize (FieldDescriptor descriptor, Object object) {
    val serializer = descriptor.getSerializer();
    val field = descriptor.getField();
    val value = field.get(object);
    return serializer.serializeUntyped(value);
  }
}
