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
package io.appulse.encon.handler.mock;

import static io.appulse.encon.handler.mock.ArgumentsWrapper.LIST;
import static io.appulse.encon.handler.mock.ArgumentsWrapper.MAP;
import static io.appulse.encon.handler.mock.ArgumentsWrapper.TUPLE;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.appulse.encon.handler.mock.MethodReferenceMessageHandler.MethodDescriptor;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 *
 * @since 1.4.0
 * @author alabazin
 */
@NoArgsConstructor(access = PACKAGE)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MethodReferenceMessageHandlerBuilder {

  Map<ArgumentsWrapper, List<MethodDescriptor>> map = new ConcurrentHashMap<>();

  Map<WrapperCacheKey, Object> wrappedCache = new HashMap<>();

  public <A> WrapperContainerBuilder<A> wrap (A object) {
    return new WrapperContainerBuilder<>(object);
  }

  public MethodReferenceMessageHandler build () {
    return new MethodReferenceMessageHandler(map);
  }

  private <T> T createProxy (T object, ArgumentsWrapper wrapper) {
    val cacheKey = WrapperCacheKey.of(object.getClass(), wrapper);
    val proxy = wrappedCache.get(cacheKey);
    if (proxy != null) {
      return (T) proxy;
    }

    val enhancer = new Enhancer();
    enhancer.setSuperclass(object.getClass());
    enhancer.setCallback((MethodInterceptor) (obj, method, args, methodProxy) -> {
      map.compute(wrapper, (key, value) -> {
        List<MethodDescriptor> list = ofNullable(value)
            .orElse(new LinkedList<>());

        list.add(MethodDescriptor.builder()
            .proxy(object)
            .method(method)
            .args(args)
            .build());

        return list;
      });
      return null;
    });

    T result = (T) enhancer.create();
    wrappedCache.put(cacheKey, result);
    return result;
  }

  @RequiredArgsConstructor(access = PRIVATE)
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public class WrapperContainerBuilder<T1> {

    T1 object;

    @SneakyThrows
    public WrapperBuilder<T1> tuple (ConsumerWithException<T1> comsumer) {
      T1 proxy = createProxy(object, TUPLE);
      comsumer.accept(proxy);
      return new WrapperBuilder<>(object);
    }

    @SneakyThrows
    public WrapperBuilder<T1> list (ConsumerWithException<T1> comsumer) {
      T1 proxy = createProxy(object, LIST);
      comsumer.accept(proxy);
      return new WrapperBuilder<>(object);
    }

    @SneakyThrows
    public WrapperBuilder<T1> map (ConsumerWithException<T1> comsumer) {
      T1 proxy = createProxy(object, MAP);
      comsumer.accept(proxy);
      return new WrapperBuilder<>(object);
    }
  }

  @RequiredArgsConstructor(access = PRIVATE)
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public class WrapperBuilder<T2> {

    T2 object;

    @SneakyThrows
    public WrapperBuilder<T2> tuple (ConsumerWithException<T2> comsumer) {
      T2 proxy = createProxy(object, TUPLE);
      comsumer.accept(proxy);
      return this;
    }

    @SneakyThrows
    public WrapperBuilder<T2> list (ConsumerWithException<T2> comsumer) {
      T2 proxy = createProxy(object, LIST);
      comsumer.accept(proxy);
      return this;
    }

    @SneakyThrows
    public WrapperBuilder<T2> map (ConsumerWithException<T2> comsumer) {
      T2 proxy = createProxy(object, MAP);
      comsumer.accept(proxy);
      return this;
    }

    public <T3> WrapperContainerBuilder<T3> wrap (T3 object) {
      return MethodReferenceMessageHandlerBuilder.this.wrap(object);
    }

    public MethodReferenceMessageHandler build () {
      return MethodReferenceMessageHandlerBuilder.this.build();
    }
  }

  public interface ConsumerWithException<T> {

    void accept (T object) throws Exception;
  }

  @Value(staticConstructor = "of")
  private static class WrapperCacheKey {

    Class<?> type;

    ArgumentsWrapper wrapper;
  }
}
