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
 * @since 1.6.0
 * @author Artem Labazin
 */
@OutputTimeUnit(SECONDS)
@Warmup(iterations = 1)
@BenchmarkMode(Throughput)
@Measurement(iterations = 1)
public class JInterface_SimpleBenchmarks {

  @Benchmark
  public void mailbox2mailboxAndBack (Mailbox2MailboxAndBackState state, Blackhole blackHole) throws Exception {
    state.mailbox1.send(state.pid2, state.data);
    OtpErlangObject message = state.mailbox2.receive();
    state.mailbox2.send(state.pid1, message);
    blackHole.consume(state.mailbox1.receive());
  }

  @Benchmark
  public void oneDirectionSend (OneDirectionSendState state, Blackhole blackHole) throws Exception {
    state.clientMailbox.send(state.serverMailboxPid, state.data);
    blackHole.consume(state.serverMailbox.receive());
  }

  @State(Benchmark)
  public static class Mailbox2MailboxAndBackState {

    OtpNode node1;

    OtpMbox mailbox1;

    OtpErlangPid pid1;

    OtpNode node2;

    OtpMbox mailbox2;

    OtpErlangPid pid2;

    OtpErlangObject data;

    @Setup(Trial)
    public void setup () throws Exception {
      node1 = new OtpNode("node-1-" + System.nanoTime() + "@localhost");
      mailbox1 = node1.createMbox();
      pid1 = mailbox1.self();

      node2 = new OtpNode("node-2-" + System.nanoTime() + "@localhost");
      mailbox2 = node2.createMbox();
      pid2 = mailbox2.self();

      data = new OtpErlangBinary(new byte[] { 1, 2, 3, 4, 5 });
    }

    @TearDown(Trial)
    public void tearDown () {
      mailbox1.close();
      node1.close();

      mailbox2.close();
      node2.close();
    }
  }

  @State(Benchmark)
  public static class OneDirectionSendState {

    OtpNode serverNode;

    OtpMbox serverMailbox;

    OtpErlangPid serverMailboxPid;

    Thread serverThread;

    OtpNode clientNode;

    OtpMbox clientMailbox;

    OtpErlangObject data;

    @Setup(Trial)
    public void setup () throws Exception {
      serverNode = new OtpNode("node-server-" + System.nanoTime() + "@localhost");
      serverMailbox = serverNode.createMbox();
      serverMailboxPid = serverMailbox.self();

      serverThread = new Thread(() -> {
          try {
            while (!java.lang.Thread.interrupted()) {
              serverMailbox.receive();
            }
          } catch (Throwable ex) {
          }
      });
      serverThread.start();

      clientNode = new OtpNode("node-client-" + System.nanoTime() + "@localhost");
      clientMailbox = clientNode.createMbox();

      data = new OtpErlangBinary(new byte[] { 1, 2, 3, 4, 5 });
    }

    @TearDown(Trial)
    public void tearDown () {
      clientMailbox.close();
      clientNode.close();

      serverMailbox.close();
      serverNode.close();

      serverThread.interrupt();
    }
  }
}
