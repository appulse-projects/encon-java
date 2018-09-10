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

import static io.appulse.encon.terms.Erlang.binary;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openjdk.jmh.annotations.Level.Trial;
import static org.openjdk.jmh.annotations.Mode.Throughput;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.config.ServerConfig;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

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
public class Encon_SimpleBenchmarks {

  @Benchmark
  public void mailbox2mailboxAndBack (Mailbox2MailboxAndBackState state, Blackhole blackHole) {
    state.mailbox1.send(state.pid2, state.data);
    Message message = state.mailbox2.receive();
    state.mailbox2.send(state.pid1, message.getBody());
    blackHole.consume(state.mailbox1.receive());
  }

  @Benchmark
  public void oneDirectionSend (OneDirectionSendState state, Blackhole blackHole) {
    state.clientMailbox.send(state.serverMailboxPid, state.data);
    blackHole.consume(state.serverMailbox.receive());
  }

  @State(Benchmark)
  public static class Mailbox2MailboxAndBackState {

    Node node1;

    Mailbox mailbox1;

    ErlangPid pid1;

    Node node2;

    Mailbox mailbox2;

    ErlangPid pid2;

    ErlangTerm data;

    @Setup(Trial)
    public void setup () throws Exception {
      NodeConfig config = NodeConfig.builder()
          .shortName(TRUE)
          .server(ServerConfig.builder()
              .bossThreads(1)
              .workerThreads(1)
              .build()
          )
          .build();

      node1 = Nodes.singleNode("node-1-" + System.nanoTime(), config);
      mailbox1 = node1.mailbox().build();
      pid1 = mailbox1.getPid();

      node2 = Nodes.singleNode("node-2-" + System.nanoTime(), config);
      mailbox2 = node2.mailbox().build();
      pid2 = mailbox2.getPid();

      data = binary(new byte[] { 1, 2, 3, 4, 5 });
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

    Node serverNode;

    Mailbox serverMailbox;

    ErlangPid serverMailboxPid;

    Thread serverThread;

    Node clientNode;

    Mailbox clientMailbox;

    ErlangTerm data;

    @Setup(Trial)
    public void setup () throws Exception {
      NodeConfig config = NodeConfig.builder()
          .shortName(TRUE)
          .server(ServerConfig.builder()
              .bossThreads(1)
              .workerThreads(1)
              .build()
          )
          .build();

      serverNode = Nodes.singleNode("node-server-" + System.nanoTime(), config);
      serverMailbox = serverNode.mailbox().build();
      serverMailboxPid = serverMailbox.getPid();

      serverThread = new Thread(() -> {
          try {
            while (!java.lang.Thread.interrupted()) {
              serverMailbox.receive();
            }
          } catch (Throwable ex) {
          }
      });
      serverThread.start();

      clientNode = Nodes.singleNode("node-client-" + System.nanoTime(), config);
      clientMailbox = clientNode.mailbox().build();

      data = binary(new byte[] { 1, 2, 3, 4, 5 });
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
