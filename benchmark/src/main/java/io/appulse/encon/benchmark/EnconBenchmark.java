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

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.binary;
import static io.appulse.encon.terms.Erlang.list;
import static io.appulse.encon.terms.Erlang.number;
import static io.appulse.encon.terms.Erlang.string;
import static io.appulse.encon.terms.Erlang.tuple;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openjdk.jmh.annotations.Level.Trial;
import static org.openjdk.jmh.annotations.Mode.Throughput;
import static org.openjdk.jmh.annotations.Scope.Thread;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.val;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
public class EnconBenchmark {

 @State(Thread)
 public static class MailboxesState {

   @Setup(Trial)
   public void setup () {
     val config = NodeConfig.builder()
         .shortName(TRUE)
         .build();

     node = Nodes.singleNode("node-" + System.nanoTime(), config);

     mailbox1 = node.mailbox().build();
     pid1 = mailbox1.getPid();

     mailbox2 = node.mailbox().build();
     pid2 = mailbox2.getPid();

     message = tuple(
         atom("ok"),
         tuple(
             binary(new byte[] { 56, 16, 78 }),
             number(42),
             string("Hello world"),
             list(number(1), number(2), number(3))
         )
     );
   }

   @TearDown(Trial)
   public void tearDown () {
     node.close();
   }

   Node node;

   Mailbox mailbox1;

   ErlangPid pid1;

   Mailbox mailbox2;

   ErlangPid pid2;

   ErlangTerm message;
 }

 @Benchmark
 @BenchmarkMode(Throughput)
 @OutputTimeUnit(SECONDS)
 public Message mailbox2mailbox (MailboxesState state) {
   state.mailbox1.send(state.pid2, state.message);

   Message one = state.mailbox2.receive();
   state.mailbox2.send(state.pid1, one.getBody());

   Message two = state.mailbox1.receive();
   return two;
 }

  @State(Thread)
  public static class NodesState {

    @Setup(Trial)
    public void setup () {
      val config = NodeConfig.builder()
          .shortName(TRUE)
          .build();

      node1 = Nodes.singleNode("node-" + System.nanoTime(), config);

      mailbox1 = node1.mailbox().build();
      pid1 = mailbox1.getPid();

      node2 = Nodes.singleNode("node-" + System.nanoTime(), config);

      mailbox2 = node2.mailbox().build();
      pid2 = mailbox2.getPid();

      message = tuple(
          atom("ok"),
          tuple(
              binary(new byte[] { 56, 16, 78 }),
              number(42),
              string("Hello world"),
              list(number(1), number(2), number(3))
          )
      );
    }

    @TearDown(Trial)
    public void tearDown () {
      node1.close();
      node2.close();
    }

    Node node1;

    Mailbox mailbox1;

    ErlangPid pid1;

    Node node2;

    Mailbox mailbox2;

    ErlangPid pid2;

    ErlangTerm message;
  }

 @Benchmark
 @BenchmarkMode(Throughput)
 @OutputTimeUnit(SECONDS)
 public Message oneDirection (NodesState nodes) {
   nodes.mailbox1.send(nodes.pid2, nodes.message);

   Message request = nodes.mailbox2.receive();
   return request;
 }

  @Benchmark
  @BenchmarkMode(Throughput)
  @OutputTimeUnit(SECONDS)
  public Message node2node (NodesState nodes) {
    nodes.mailbox1.send(nodes.pid2, nodes.message);

    Message one = nodes.mailbox2.receive();
    nodes.mailbox2.send(nodes.pid1, one.getBody());

    Message two = nodes.mailbox1.receive();
    return two;
  }
}
