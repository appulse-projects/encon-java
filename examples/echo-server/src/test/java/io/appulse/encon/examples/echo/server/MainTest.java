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

package io.appulse.encon.examples.echo.server;

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.number;
import static io.appulse.encon.terms.Erlang.string;
import static io.appulse.encon.terms.Erlang.tuple;
import static org.junit.Assert.assertEquals;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.Set;

import io.appulse.encon.databind.annotation.AsErlangAtom;
import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangTuple;
import io.appulse.encon.databind.annotation.IgnoreField;
import io.appulse.encon.terms.ErlangTerm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
public class MainTest {

  final String SERVER_NODE_NAME = "echo-server";
  final String SERVER_MAILBOX_NAME = "echo";
  final String COOKIE = "secret";

  ExecutorService executor;

  EchoServerNode server;
  EchoClientNode client;

  @Before
  public void before () {
    server = new EchoServerNode(SERVER_NODE_NAME, COOKIE, SERVER_MAILBOX_NAME);
    client = new EchoClientNode(COOKIE);

    executor = Executors.newSingleThreadExecutor();
    executor.execute(server);
  }

  @After
  public void after () {
    client.close();
    server.close();
    executor.shutdown();
  }

  @Test
  public void term () {
    ErlangTerm request = tuple(
        atom("ok"),
        number(42),
        string("Hello world")
    );

    client.send(SERVER_NODE_NAME, SERVER_MAILBOX_NAME, request);

    ErlangTerm response = client.receive();

    assertEquals(request, response);
  }

  @Test
  public void pojo () {
    MyPojo request = new MyPojo(
        "Artem",
        27,
        true,
        539,
        asList("java", "nim", "elixir"),
        "developer",
        singleton("popa"),
        "some long string...or not",
        new Boolean[] { true, false, true }
    );

    client.send(SERVER_NODE_NAME, SERVER_MAILBOX_NAME, request);

    MyPojo response = client.receive(MyPojo.class);

    assertEquals(request, response);
  }

  @Data
  @AsErlangTuple
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode(exclude = "ignored")
  public static class MyPojo {

    String name;

    int age;

    boolean male;

    @IgnoreField
    int ignored;

    List<String> languages;

    @AsErlangAtom
    String position;

    @AsErlangList
    Set<String> set;

    @AsErlangList
    String listString;

    @AsErlangList
    Boolean[] bools;
  }
}
