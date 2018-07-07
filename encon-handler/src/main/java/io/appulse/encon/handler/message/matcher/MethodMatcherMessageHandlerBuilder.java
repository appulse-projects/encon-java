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

import static io.appulse.encon.handler.message.matcher.MethodArgumentsWrapper.LIST;
import static io.appulse.encon.handler.message.matcher.MethodArgumentsWrapper.MAP;
import static io.appulse.encon.handler.message.matcher.MethodArgumentsWrapper.NONE;
import static io.appulse.encon.handler.message.matcher.MethodArgumentsWrapper.TUPLE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.sf.cglib.proxy.Enhancer;

/**
 * Builder of a new {@link MethodMatcherMessageHandler} instance.
 *
 * @since 1.4.0
 * @author alabazin
 */
@NoArgsConstructor(access = PACKAGE)
@FieldDefaults(level = PRIVATE, makeFinal = true)
@SuppressWarnings("PMD.AccessorMethodGeneration")
public class MethodMatcherMessageHandlerBuilder {

  Map<MethodArgumentsWrapper, List<MethodDescriptor>> map = new ConcurrentHashMap<>();

  Map<WrapperCacheKey, Object> wrappedCache = new HashMap<>();

  /**
   * Wraps object into builder.
   *
   * @param <A> object's type
   *
   * @param object object
   *
   * @return {@link WrapperContainerBuilder} instance
   */
  public <A> WrapperContainerBuilder<A> wrap (A object) {
    return new WrapperContainerBuilder<>(object);
  }

  /**
   * Finalizes builder and creates new handler instance.
   *
   * @return a new {@link MethodMatcherMessageHandler}
   */
  public MethodMatcherMessageHandler build () {
    return new MethodMatcherMessageHandler(map);
  }

  private <T> T createProxy (T object, MethodArgumentsWrapper wrapper) {
    val cacheKey = WrapperCacheKey.of(object.getClass(), wrapper);
    val proxy = wrappedCache.get(cacheKey);
    if (proxy != null) {
      return (T) proxy;
    }

    val enhancer = new Enhancer();
    enhancer.setSuperclass(object.getClass());
    enhancer.setCallback(BuilderProxyMethodInterceptor.builder()
        .map(map)
        .wrapper(wrapper)
        .target(object)
        .build());

    T result = (T) enhancer.create();
    wrappedCache.put(cacheKey, result);
    return result;
  }

  @RequiredArgsConstructor(access = PRIVATE)
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public class WrapperContainerBuilder<T1> {

    T1 object;

    /**
     * Method will be expect a tuple container in a received message.
     *
     * @param comsumer function for calling a method
     *
     * @return builder
     */
    @SneakyThrows
    public WrapperBuilder<T1> tuple (ConsumerWithException<T1> comsumer) {
      T1 proxy = createProxy(object, TUPLE);
      comsumer.accept(proxy);
      return new WrapperBuilder<>(object);
    }

    /**
     * Method will be expect a list container in a received message.
     *
     * @param comsumer function for calling a method
     *
     * @return builder
     */
    @SneakyThrows
    public WrapperBuilder<T1> list (ConsumerWithException<T1> comsumer) {
      T1 proxy = createProxy(object, LIST);
      comsumer.accept(proxy);
      return new WrapperBuilder<>(object);
    }

    /**
     * Method will be expect a map container in a received message.
     *
     * @param comsumer function for calling a method
     *
     * @return builder
     */
    @SneakyThrows
    public WrapperBuilder<T1> map (ConsumerWithException<T1> comsumer) {
      T1 proxy = createProxy(object, MAP);
      comsumer.accept(proxy);
      return new WrapperBuilder<>(object);
    }

    /**
     * Method will be not expect any container in a received message.
     *
     * @param comsumer function for calling a method
     *
     * @return builder
     */
    @SneakyThrows
    public WrapperBuilder<T1> none (ConsumerWithException<T1> comsumer) {
      T1 proxy = createProxy(object, NONE);
      comsumer.accept(proxy);
      return new WrapperBuilder<>(object);
    }
  }

  /**
   * Wrapper.
   *
   * @param <T2> type
   */
  @RequiredArgsConstructor(access = PRIVATE)
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public class WrapperBuilder<T2> {

    T2 object;

    /**
     * Method will be expect a tuple container in a received message.
     *
     * @param comsumer function for calling a method
     *
     * @return builder
     */
    @SneakyThrows
    public WrapperBuilder<T2> tuple (ConsumerWithException<T2> comsumer) {
      T2 proxy = createProxy(object, TUPLE);
      comsumer.accept(proxy);
      return this;
    }

    /**
     * Method will be expect a list container in a received message.
     *
     * @param comsumer function for calling a method
     *
     * @return builder
     */
    @SneakyThrows
    public WrapperBuilder<T2> list (ConsumerWithException<T2> comsumer) {
      T2 proxy = createProxy(object, LIST);
      comsumer.accept(proxy);
      return this;
    }

    /**
     * Method will be expect a map container in a received message.
     *
     * @param comsumer function for calling a method
     *
     * @return builder
     */
    @SneakyThrows
    public WrapperBuilder<T2> map (ConsumerWithException<T2> comsumer) {
      T2 proxy = createProxy(object, MAP);
      comsumer.accept(proxy);
      return this;
    }

    /**
     * Method will be not expect any container in a received message.
     *
     * @param comsumer function for calling a method
     *
     * @return builder
     */
    @SneakyThrows
    public WrapperBuilder<T2> none (ConsumerWithException<T2> comsumer) {
      T2 proxy = createProxy(object, NONE);
      comsumer.accept(proxy);
      return this;
    }

    /**
     * Wraps an object.
     *
     * @param <T3> object's type
     *
     * @param obj object
     *
     * @return builder
     */
    public <T3> WrapperContainerBuilder<T3> wrap (T3 obj) {
      return MethodMatcherMessageHandlerBuilder.this.wrap(obj);
    }

    /**
     * Finish building.
     *
     * @return a new {@link MethodMatcherMessageHandler} instance
     */
    public MethodMatcherMessageHandler build () {
      return MethodMatcherMessageHandlerBuilder.this.build();
    }
  }

  /**
   * Consumer interface with checked exception in a signature.
   *
   * @param <T> wrapped object type
   */
  public interface ConsumerWithException<T> {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void accept (T object) throws Exception;
  }

  @Value(staticConstructor = "of")
  static class WrapperCacheKey {

    Class<?> type;

    MethodArgumentsWrapper wrapper;
  }
}
