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

package io.appulse.encon.handler.message.matcher;

import static io.appulse.encon.handler.message.matcher.Matchers.anyString;
import static io.appulse.encon.handler.message.matcher.Matchers.anyInt;
import static io.appulse.encon.handler.message.matcher.Matchers.any;
import static io.appulse.encon.handler.message.matcher.Matchers.eq;
import static io.appulse.encon.terms.Erlang.tuple;
import static io.appulse.encon.terms.Erlang.list;
import static io.appulse.encon.terms.Erlang.number;
import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.bstring;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;

import io.appulse.encon.handler.message.matcher.MethodMatcherMessageHandler;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.test.TestMethodNamePrinter;
import lombok.val;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author alabazin
 */
public class MethodMatcherMessageHandlerTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  static List<String> list = new ArrayList<>();

  @Test
  public void test () {
    MyService1 service1 = new MyService1();
    MyService2 service2 = new MyService2();

    val handler = MethodMatcherMessageHandler.builder()
        .wrap(service1)
            .list(it -> it.handler1())
            .tuple(it -> it.handler2(anyInt(), anyString(), eq(false)))
        .wrap(service2)
            .tuple(it -> it.popa1(eq(42)))
        .build();

    handler.handle(null, null, list());
    handler.handle(null, null, tuple(number(42), bstring("popa"), atom(false)));
    handler.handle(null, null, tuple(number(42)));

    assertThat(list).containsOnly(
        "handler1()",
        "handler2(42, popa, false)",
        "popa1(42)"
    );
  }

  @Test
  public void throwsAmbigousException () {
    MyService2 service = new MyService2();
    val builder = MethodMatcherMessageHandler.builder()
        .wrap(service);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> builder.tuple(it -> it.popa2(any(), null, "popa")))
        .withMessage("Ambiguous method call");
  }

  public static class MyService1 {

    public void handler1 () {
      list.add("handler1()");
    }

    public void handler2 (int a, String b, boolean c) {
      list.add("handler2(" + a + ", " + b + ", " + c + ")");
    }
  }

  public static class MyService2 {

    public void popa1 (int age) throws Exception {
      list.add("popa1(" + age + ")");
    }

    public void popa2 (ErlangTerm term, MyService1 service, String str) {
      list.add("popa2(" + term + ", " + service + ", " + str + ")");
    }
  }
}
