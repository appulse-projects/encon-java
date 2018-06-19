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

package io.appulse.encon.databind.annotation;

import static io.appulse.encon.databind.deserializer.Deserializer.STRING_DESERIALIZER;
import static io.appulse.encon.databind.serializer.Serializer.ATOM_SERIALIZER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.appulse.encon.databind.annotation.AsErlangAtom.AtomTermDeserializer;
import io.appulse.encon.databind.annotation.AsErlangAtom.AtomTermSerializer;
import io.appulse.encon.databind.deserializer.Deserializer;
import io.appulse.encon.databind.serializer.Serializer;
import io.appulse.encon.terms.ErlangTerm;

/**
 * Marker annotation for fields, which should be serialized/deserialized
 * as {@link io.appulse.encon.terms.type.ErlangAtom}.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
@TermSerialize(AtomTermSerializer.class)
@TermDeserialize(AtomTermDeserializer.class)
public @interface AsErlangAtom {

  /**
   * String atom serializer.
   */
  class AtomTermSerializer implements Serializer<String> {

    @Override
    public ErlangTerm serialize (String object) {
      return ATOM_SERIALIZER.serialize(object);
    }
  }

  /**
   * String atom deserializer.
   */
  class AtomTermDeserializer implements Deserializer<String> {

    @Override
    public String deserialize (ErlangTerm term) {
      return STRING_DESERIALIZER.deserialize(term);
    }
  }
}
