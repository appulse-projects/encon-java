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
package io.appulse.encon.databind.deserializer;

import static io.appulse.encon.terms.Erlang.atom;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import io.appulse.encon.databind.parser.FieldDescriptor;
import io.appulse.encon.terms.ErlangTerm;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author alabazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MapWrapperDeserializer <T> implements Deserializer<T> {

  Class<T> type;

  List<FieldDescriptor> fields;

  @Override
  @SneakyThrows
  public T deserialize (ErlangTerm map) {
    T result = type.newInstance();
    for (int i = 0; i < fields.size(); i++) {
      FieldDescriptor descriptor = fields.get(i);
      ErlangTerm term = map.getUnsafe(atom(descriptor.getName()));
      Object value = descriptor.getDeserializer().deserialize(term);
      descriptor.getField().set(result, value);
    }
    return result;
  }
}
