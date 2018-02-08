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

import io.appulse.encon.java.protocol.type.IntegralNumber;
import io.appulse.encon.java.protocol.type.Nil;
import io.appulse.encon.java.protocol.type.Pid;
import io.appulse.encon.java.protocol.type.Tuple;
import io.appulse.utils.Bytes;

public class DistributionHeader {

  public byte[] toBytes () {
    return Bytes.allocate()
        // header
        .put1B(131)
        .put1B(68)
        // control message
        .put(Tuple.builder()
            .add(new IntegralNumber(2))
            .add(new Nil())
            .add(new Pid(null))
            .build()
            .toBytes()
        )
        .array();
  }
}
