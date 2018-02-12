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

package io.appulse.encon.java.protocol;

import io.appulse.encon.java.protocol.type.ErlangInteger;
import io.appulse.encon.java.protocol.type.ErlangNil;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.encon.java.protocol.type.ErlangTuple;
import io.appulse.utils.Bytes;

public class DistributionHeader {

  public byte[] toBytes () {
    return Bytes.allocate()
        // header
        .put1B(131)
        .put1B(68)
        // control message
        .put(ErlangTuple.builder()
            .add(new ErlangInteger(2))
            .add(new ErlangNil())
            .add(new ErlangPid(null))
            .build()
            .toBytes()
        )
        .array();
  }
}
