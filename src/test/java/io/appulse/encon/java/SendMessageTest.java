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
package io.appulse.encon.java;

import static io.appulse.encon.java.protocol.Erlang.number;
import static io.appulse.encon.java.protocol.Erlang.string;
import static io.appulse.encon.java.protocol.Erlang.tuple;
import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.java.module.connection.regular.Message;
import io.appulse.encon.java.module.connection.regular.MessageEncoder;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.encon.java.protocol.type.ErlangReference;
import io.appulse.encon.java.protocol.type.ErlangTuple;

import erlang.OtpErlangInt;
import erlang.OtpErlangObject;
import erlang.OtpErlangPid;
import erlang.OtpErlangRef;
import erlang.OtpErlangString;
import erlang.OtpErlangTuple;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;

/**
 *
 * @author alabazin
 */
public class SendMessageTest {

//  @Test
  @SneakyThrows
  public void popa () {
    assertThat(encon())
        .isEqualTo(jinterface());
  }

  @SneakyThrows
  private byte[] encon () {
    ErlangPid pid = ErlangPid.builder()
        .node("popa@localhost")
        .id(1)
        .serial(27)
        .creation(3)
        .build();

    ErlangReference ref = ErlangReference.builder()
        .node("popa@localhost")
        .id(3)
        .creation(3)
        .build();

    ErlangTuple tuple = tuple(pid, tuple(number(42), string("Hello world"), ref));

    Message message = Message.send(pid, tuple);
    ChannelHandlerContext context = null;
    new MessageEncoder().write(context, message, null);
    return null;
  }

  private byte[] jinterface () {
    OtpErlangPid pid = new OtpErlangPid("popa@localhost", 1, 27, 3);
    OtpErlangTuple tuple = new OtpErlangTuple(new OtpErlangObject[] {
      pid,
      new OtpErlangTuple(new OtpErlangObject[] {
        new OtpErlangInt(42),
        new OtpErlangString("Hello world"),
        new OtpErlangRef("popa@localhost", 3, 3)
      })
    });
    return null;
  }
}
