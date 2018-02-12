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

package io.appulse.encon.java.module.mailbox.request;

import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedHashMap;

import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangMap;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MapRequestBuilder extends AbstractPrepareMessageBeforeSendRequestBuilder {

  LinkedHashMap<ErlangTerm, ErlangTerm> map;

  MapRequestBuilder (@NonNull Mailbox mailbox) {
    super(mailbox);
    map = new LinkedHashMap<>();
  }

  public MapRequestBuilder put (@NonNull MapItem key, @NonNull MapItem value) {
    map.put(key.getTerm(), value.getTerm());
    return this;
  }

  @Override
  protected void prepareMessage () {
    val message = new ErlangMap(map);
    setMessage(message);
  }
}
