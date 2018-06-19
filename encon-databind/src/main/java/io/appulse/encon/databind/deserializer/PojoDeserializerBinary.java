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

package io.appulse.encon.databind.deserializer;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.SerializationUtils;

import lombok.NonNull;

/**
 * Eralng's term binary deserializer to specified type.
 *
 * @since 1.1.0
 * @author alabazin
 */
public class PojoDeserializerBinary implements Deserializer<Object> {

  @Override
  public Object deserialize (@NonNull ErlangTerm binary) {
    byte[] byteArray = binary.asBinary();
    return SerializationUtils.deserialize(byteArray);
  }
}
