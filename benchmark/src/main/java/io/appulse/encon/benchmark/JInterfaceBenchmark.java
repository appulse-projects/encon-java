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
import static org.openjdk.jmh.annotations.Scope.Thread;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;
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
public class JInterfaceBenchmark {

 @State(Thread)
 public static class MailboxesState {

   @Setup(Trial)
   public void setup () throws Exception {
     node = new OtpNode("node-" + System.nanoTime() + "@localhost");

     mailbox1 = node.createMbox();
     pid1 = mailbox1.self();

     mailbox2 = node.createMbox();
     pid2 = mailbox2.self();

     message = new OtpErlangTuple(new OtpErlangObject[] {
         new OtpErlangAtom("ok"),
         new OtpErlangTuple(new OtpErlangObject[] {
             new OtpErlangBinary(new byte[] { 56, 16, 78 }),
             new OtpErlangInt(42),
             new OtpErlangString("Hello world"),
             new OtpErlangList(new OtpErlangObject[] {
                 new OtpErlangInt(1),
                 new OtpErlangInt(2),
                 new OtpErlangInt(3)
             })
         })
     });
   }

   @TearDown(Trial)
   public void tearDown () {
     mailbox1.close();
     mailbox2.close();
     node.close();
   }

   OtpNode node;

   OtpMbox mailbox1;

   OtpErlangPid pid1;

   OtpMbox mailbox2;

   OtpErlangPid pid2;

   OtpErlangObject message;
 }

 @Benchmark
 @BenchmarkMode(Throughput)
 @OutputTimeUnit(SECONDS)
 public OtpErlangObject mailbox2mailbox (MailboxesState state) throws Exception {
   state.mailbox1.send(state.pid2, state.message);

   OtpErlangObject one = state.mailbox2.receive();
   state.mailbox2.send(state.pid1, one);

   OtpErlangObject two = state.mailbox1.receive();
   return two;
 }

  @State(Thread)
  public static class NodesState {

    @Setup(Trial)
    public void setup () throws Exception {
      node1 = new OtpNode("node-" + System.nanoTime() + "@localhost");

      mailbox1 = node1.createMbox();
      pid1 = mailbox1.self();

      node2 = new OtpNode("node-" + System.nanoTime() + "@localhost");

      mailbox2 = node2.createMbox();
      pid2 = mailbox2.self();

      message = new OtpErlangTuple(new OtpErlangObject[] {
          new OtpErlangAtom("ok"),
          new OtpErlangTuple(new OtpErlangObject[] {
              new OtpErlangBinary(new byte[] { 56, 16, 78 }),
              new OtpErlangInt(42),
              new OtpErlangString("Hello world"),
              new OtpErlangList(new OtpErlangObject[] {
                  new OtpErlangInt(1),
                  new OtpErlangInt(2),
                  new OtpErlangInt(3)
              })
          })
      });
    }

    @TearDown(Trial)
    public void tearDown () {
      mailbox1.close();
      node1.close();
      mailbox2.close();
      node2.close();
    }

    OtpNode node1;

    OtpMbox mailbox1;

    OtpErlangPid pid1;

    OtpNode node2;

    OtpMbox mailbox2;

    OtpErlangPid pid2;

    OtpErlangObject message;
  }

 @Benchmark
 @BenchmarkMode(Throughput)
 @OutputTimeUnit(SECONDS)
 public OtpErlangObject oneDirection (NodesState nodes) throws Exception {
   nodes.mailbox1.send(nodes.pid2, nodes.message);

   OtpErlangObject request = nodes.mailbox2.receive();
   return request;
 }

  @Benchmark
  @BenchmarkMode(Throughput)
  @OutputTimeUnit(SECONDS)
  public OtpErlangObject node2node (NodesState state) throws Exception {
    state.mailbox1.send(state.pid2, state.message);

    OtpErlangObject one = state.mailbox2.receive();
    state.mailbox2.send(state.pid1, one);

    OtpErlangObject two = state.mailbox1.receive();
    return two;
  }
}
