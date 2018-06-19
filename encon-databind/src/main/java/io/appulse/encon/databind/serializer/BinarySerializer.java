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

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.SerializationUtils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class BinarySerializer extends TermSerializer<Object> {

  @Override
  public ErlangTerm serialize (Object object) {
    val byteArray = ofNullable(object)
        .filter(it -> it instanceof byte[])
        .map(it -> (byte[]) it)
        .orElseGet(() -> ofNullable(object)
            .filter(it -> it instanceof Serializable)
            .map(it -> (Serializable) it)
            .map(SerializationUtils::serialize)
            .orElse(new byte[0]));

    return Serializer.BYTE_ARRAY_SERIALIZER.serialize(byteArray);
  }
}
