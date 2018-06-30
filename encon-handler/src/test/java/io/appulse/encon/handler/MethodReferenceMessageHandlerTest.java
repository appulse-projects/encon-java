/*
 * Copyright 2018 the original author or authors..
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
package io.appulse.encon.handler;

import static io.appulse.encon.handler.mock.Matchers.any;
import static io.appulse.encon.handler.mock.Matchers.anyInt;
import static io.appulse.encon.handler.mock.Matchers.eq;

import io.appulse.encon.handler.mock.MethodReferenceMessageHandler;

import lombok.val;
import org.junit.Test;

/**
 *
 * @author alabazin
 */
public class MethodReferenceMessageHandlerTest {

  @Test
  public void test () {
    MyService1 service1 = new MyService1();
    MyService2 service2 = new MyService2();

    val handler = MethodReferenceMessageHandler.builder()
        .wrap(service1)
            .tuple(it -> it.handler1())
            .tuple(it -> it.handler2())
            .tuple(it -> it.handler3(anyInt(), any(), eq(false)))
        .wrap(service2)
            .tuple(it -> it.popa())
        .build();
  }

  public static class MyService1 {

    public void handler1 () {

    }

    public int handler2 () throws Exception {
      return 0;
    }

    public void handler3 (int a, String b, boolean c) {

    }
  }

  public static class MyService2 {

    public void popa () throws Exception {

    }
  }
}
