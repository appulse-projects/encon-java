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

package io.appulse.encon.java.protocol.request;

import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.protocol.type.Tuple;

import lombok.NonNull;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
class TupleRequestBuilder extends AbstractArrayRequestBuilder {

  TupleRequestBuilder (@NonNull Mailbox mailbox) {
    super(mailbox);
  }

  @Override
  protected void prepareMessage () {
    val tuple = Tuple.builder()
        .adds(getTerms())
        .build();

    setMessage(tuple);
  }
}
