/*
 * Copyright 2020 the original author or authors.
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

package io.appulse.encon.benchmark;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openjdk.jmh.annotations.Level.Trial;
import static org.openjdk.jmh.annotations.Mode.Throughput;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 *
 * @since 1.6.3
 * @author Artem Labazin
 */
@OutputTimeUnit(SECONDS)
@Warmup(iterations = 10)
@BenchmarkMode(Throughput)
@Measurement(iterations = 20)
public class JInterface_SimpleBenchmarks {

  @Benchmark
  public void mailbox2mailboxAndBack (Mailbox2MailboxAndBackState state, Blackhole blackHole) throws Exception {
    state.mailbox1.send(state.pid2, state.data);
    OtpErlangObject message = state.mailbox2.receive();
    state.mailbox2.send(state.pid1, message);
    blackHole.consume(state.mailbox1.receive());
  }

  @State(Benchmark)
  public static class Mailbox2MailboxAndBackState {

    OtpNode node;

    OtpMbox mailbox1;

    OtpErlangPid pid1;

    OtpMbox mailbox2;

    OtpErlangPid pid2;

    OtpErlangObject data;

    @Setup(Trial)
    public void setup () throws Exception {
      node = new OtpNode("node-" + System.nanoTime() + "@localhost");

      mailbox1 = node.createMbox();
      pid1 = mailbox1.self();

      mailbox2 = node.createMbox();
      pid2 = mailbox2.self();

      data = new OtpErlangBinary(new byte[] { 1, 2, 3, 4, 5 });
    }

    @TearDown(Trial)
    public void tearDown () {
      mailbox1.close();
      mailbox2.close();

      node.close();
    }
  }
}
