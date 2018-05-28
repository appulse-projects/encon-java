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

import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.databind.parser.PojoParser;
import io.appulse.encon.databind.deserializer.Deserializer;
import io.appulse.encon.databind.parser.PojoDescriptor;
import io.appulse.encon.databind.serializer.Serializer;
import io.appulse.encon.terms.ErlangTerm;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class TermMapper {

  @SuppressWarnings("unchecked")
  public <T> T deserialize (@NonNull ErlangTerm container, @NonNull Class<T> type) {
    if (ErlangTerm.class.isAssignableFrom(type)) {
      return (T) container;
    }
    PojoDescriptor descriptor = PojoParser.parse(type);
    Deserializer<?> deserializer = descriptor.getDeserializer();
    return (T) deserializer.deserialize(container);
  }

  public ErlangTerm serialize (@NonNull Object object) {
    if (object instanceof ErlangTerm) {
      return (ErlangTerm) object;
    }
    val type = object.getClass();
    PojoDescriptor descriptor = PojoParser.parse(type);
    Serializer<?> serializer = descriptor.getSerializer();
    return serializer.serializeUntyped(object);
  }
}
