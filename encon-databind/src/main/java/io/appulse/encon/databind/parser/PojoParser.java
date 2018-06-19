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
import static io.appulse.utils.AnnotationUtils.findAnnotation;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import io.appulse.encon.databind.annotation.AsErlangBinary;
import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangMap;
import io.appulse.encon.databind.annotation.IgnoreField;
import io.appulse.encon.databind.annotation.TermDeserialize;
import io.appulse.encon.databind.annotation.TermSerialize;
import io.appulse.encon.databind.deserializer.BinaryDeserializer;
import io.appulse.encon.databind.deserializer.CollectionWrapperDeserializer;
import io.appulse.encon.databind.deserializer.MapWrapperDeserializer;
import io.appulse.encon.databind.parser.PojoDescriptor.PojoDescriptorBuilder;
import io.appulse.encon.databind.serializer.BinarySerializer;
import io.appulse.encon.databind.serializer.ListWrapperSerializer;
import io.appulse.encon.databind.serializer.MapWrapperSerializer;
import io.appulse.encon.databind.serializer.TupleWrapperSerializer;

import lombok.val;

/**
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
public final class PojoParser {

  private static final Map<Class<?>, PojoDescriptor> CACHE;

  static {
    CACHE = new ConcurrentHashMap<>(5);
  }

  public static PojoDescriptor parse (Class<?> type) {
    return CACHE.computeIfAbsent(type, PojoParser::createNewDescriptor);
  }

  private static PojoDescriptor createNewDescriptor (Class<?> type) {
    val builder = createDefaultDescriptor(type);

    ofNullable(type.getAnnotation(TermSerialize.class))
        .map(TermSerialize::value)
        .filter(Objects::nonNull)
        .map(it -> SERIALIZERS.computeIfAbsent(it, NEW_SERIALIZER))
        .filter(Objects::nonNull)
        .ifPresent(builder::serializer);

    ofNullable(type.getAnnotation(TermDeserialize.class))
        .map(TermDeserialize::value)
        .filter(Objects::nonNull)
        .map(it -> DESERIALIZERS.computeIfAbsent(it, NEW_DESERIALIZER))
        .filter(Objects::nonNull)
        .ifPresent(builder::deserializer);

    return builder.build();
  }

  private static PojoDescriptorBuilder createDefaultDescriptor (Class<?> type) {
    val fields = Stream.of(type.getDeclaredFields())
        .filter(it -> !it.isSynthetic())
        .filter(it -> !it.isAnnotationPresent(IgnoreField.class))
        .map(FieldParser::parse)
        .sorted(comparing(FieldDescriptor::getOrder))
        .collect(toList());

    val builder = PojoDescriptor.builder()
          .type(type);

    if (findAnnotation(type, AsErlangMap.class).isPresent()) {
      return builder
          .serializer(new MapWrapperSerializer(fields))
          .deserializer(new MapWrapperDeserializer(type, fields));
    }
    if (findAnnotation(type, AsErlangList.class).isPresent()) {
      return builder
          .serializer(new ListWrapperSerializer(fields))
          .deserializer(new CollectionWrapperDeserializer<>(type, fields));
    }
    if (findAnnotation(type, AsErlangBinary.class).isPresent()) {
      return builder
          .serializer(new BinarySerializer())
          .deserializer(new BinaryDeserializer());
    }
    // default is tuple
    return builder
        .serializer(new TupleWrapperSerializer(fields))
        .deserializer(new CollectionWrapperDeserializer<>(type, fields));
  }

  private PojoParser () {
  }
}
