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

import java.util.stream.IntStream;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.config.ServerConfig;
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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.ThreadParams;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@State(Benchmark)
@OutputTimeUnit(SECONDS)
@Warmup(iterations = 1)
@BenchmarkMode(Throughput)
@Measurement(iterations = 1)
public class Encon_Node2NodeBenchmarks {

  Node serverNode;

  Mailbox serverMailbox;

  ErlangPid serverMailboxPid;

  Thread serverThread;

  ErlangTerm data;

  Node clientNode;

  Mailbox[] clientMailboxes;

  @Setup(Trial)
  public void setup () throws Exception {
    serverNode = Nodes.singleNode("node-server-" + System.nanoTime(), NodeConfig.builder()
        .shortName(TRUE)
        .server(ServerConfig.builder()
            .bossThreads(1)
            .workerThreads(2)
            .build()
        )
        .build()
    );

    serverMailbox = serverNode.mailbox().build();
    serverMailboxPid = serverMailbox.getPid();
    data = binary(new byte[] { 1, 2, 3, 4, 5 });

    serverThread = new Thread(() -> {
        try {
          while (!java.lang.Thread.interrupted()) {
            ErlangTerm payload = serverMailbox.receive().getBody();
            serverMailbox.send(payload.asPid(), data);
          }
        } catch (Throwable ex) {
        }
    });
    serverThread.start();

    clientNode = Nodes.singleNode("node-client-" + System.nanoTime(), NodeConfig.builder().shortName(TRUE)
        .server(ServerConfig.builder()
            .bossThreads(1)
            .workerThreads(8)
            .build()
        )
        .build());

    clientMailboxes = IntStream.range(0, 8)
        .boxed()
        .map(it -> clientNode.mailbox().build())
        .toArray(Mailbox[]::new);
  }

  @TearDown(Trial)
  public void tearDown () {
    for (Mailbox mailbox : clientMailboxes) {
      mailbox.close();
    }
    clientNode.close();

    serverMailbox.close();
    serverNode.close();

    serverThread.interrupt();
  }

  @Threads(1)
  @Benchmark
  public void client_1 (ThreadParams thredParams, Blackhole blackHole) throws Exception {
    Mailbox mailbox = clientMailboxes[0];
    mailbox.send(serverMailboxPid, mailbox.getPid());
    blackHole.consume(mailbox.receive());
  }

  @Threads(2)
  @Benchmark
  public void clients_2 (ThreadParams thredParams, Blackhole blackHole) throws Exception {
    Mailbox mailbox = clientMailboxes[thredParams.getThreadIndex()];
    mailbox.send(serverMailboxPid, mailbox.getPid());
    blackHole.consume(mailbox.receive());
  }

  @Threads(4)
  @Benchmark
  public void clients_4 (ThreadParams thredParams, Blackhole blackHole) throws Exception {
    Mailbox mailbox = clientMailboxes[thredParams.getThreadIndex()];
    mailbox.send(serverMailboxPid, mailbox.getPid());
    blackHole.consume(mailbox.receive());
  }

  @Threads(8)
  @Benchmark
  public void clients_8 (ThreadParams thredParams, Blackhole blackHole) throws Exception {
    Mailbox mailbox = clientMailboxes[thredParams.getThreadIndex()];
    mailbox.send(serverMailboxPid, mailbox.getPid());
    blackHole.consume(mailbox.receive());
  }
}
