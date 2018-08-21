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

package io.appulse.encon.handler.message.matcher;

import static io.appulse.encon.databind.TermMapper.deserialize;
import static lombok.AccessLevel.PRIVATE;

import java.util.stream.IntStream;

import io.appulse.encon.terms.ErlangTerm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
@Slf4j
@Getter
@ToString
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
final class MethodArgumentsTransformer {

  private static void debug (String message, Object... arguments) {
    if (log.isDebugEnabled()) {
      log.debug(message, arguments);
    }
  }

  Class<?> userPojoType;

  @NonNull
  Class<?>[] types;

  Object[] transform (ErlangTerm term) {
    Object[] result;

    if (userPojoType == null) {
      debug("deserialize term {} into {} arguments", term, types);
      result = IntStream.range(0, types.length)
          .mapToObj(index -> deserialize(term.getUnsafe(index), types[index]))
          .toArray();
    } else {
      debug("deserialize term {} into {} instance", term, userPojoType);
      result = new Object[] { deserialize(term, userPojoType) };
    }

    debug("result is {}", result);
    return result;
  }
}
