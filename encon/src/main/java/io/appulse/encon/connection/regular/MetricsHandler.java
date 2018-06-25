/*
 * Copyright 2018 Appulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.appulse.encon.connection.regular;

import static java.lang.Integer.min;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author alabazin
 */
@Slf4j
@Sharable
public class MetricsHandler extends ChannelDuplexHandler {

  public static final Queue<Long> READS = new ConcurrentLinkedQueue<>();

  public static final Queue<Long> WRITES = new ConcurrentLinkedQueue<>();

  public static final Queue<Long> FLUSHES = new ConcurrentLinkedQueue<>();

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    long time = System.nanoTime();
    READS.add(time);
    ctx.fireChannelRead(msg);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    long time = System.nanoTime();
    WRITES.add(time);
    ctx.write(msg, promise);
  }

  @Override
  public void flush(ChannelHandlerContext ctx) throws Exception {
    long time = System.nanoTime();
    FLUSHES.add(time);
    ctx.flush();
  }

  public Metrics get () {
    List<Long> reads = READS.stream()
        .sorted()
        .collect(toList());
    List<Long> writes = WRITES.stream()
        .sorted()
        .collect(toList());
    List<Long> flushes = FLUSHES.stream()
        .sorted()
        .collect(toList());


    int pairs = min(min(reads.size(), writes.size()), flushes.size());

    long period1 = writes.get(0) - reads.get(0);
    long readWritePeriodSum = period1;
    long minReadWritePeriod = period1;
    long maxReadWritePeriod = period1;

    long period2 = flushes.get(0) - reads.get(0);
    long readFlushPeriodSum = period2;
    long minReadFlushPeriod = period2;
    long maxReadFlushPeriod = period2;

    long period3 = flushes.get(0) - writes.get(0);
    long writeFlushPeriodSum = period3;
    long minWriteFlushPeriod = period3;
    long maxWriteFlushPeriod = period3;

    for (int index = 1; index < pairs; index++) {
      long read = reads.get(index);
      long write = writes.get(index);
      long flush = flushes.get(index);

      long readWritePeriod = write - read;
      long readFlushPeriod = flush - read;
      long writeFlushPeriod = flush - write;

      readWritePeriodSum += readWritePeriod;
      minReadWritePeriod = Long.min(minReadWritePeriod, readWritePeriod);
      maxReadWritePeriod = Long.max(maxReadWritePeriod, readWritePeriod);

      readFlushPeriodSum += readFlushPeriod;
      minReadFlushPeriod = Long.min(minReadFlushPeriod, readFlushPeriod);
      maxReadFlushPeriod = Long.max(maxReadFlushPeriod, readFlushPeriod);

      writeFlushPeriodSum += writeFlushPeriod;
      minWriteFlushPeriod = Long.min(minWriteFlushPeriod, writeFlushPeriod);
      maxWriteFlushPeriod = Long.max(maxWriteFlushPeriod, writeFlushPeriod);
    }

    return Metrics.builder()
        .pairs(pairs)
        .avgReadWritePeriod(readWritePeriodSum / pairs)
        .avgReadFlushPeriod(readFlushPeriodSum / pairs)
        .avgWriteFlushPeriod(writeFlushPeriodSum / pairs)
        .minReadWritePeriod(minReadWritePeriod)
        .minReadFlushPeriod(minReadFlushPeriod)
        .minWriteFlushPeriod(minWriteFlushPeriod)
        .maxReadWritePeriod(maxReadWritePeriod)
        .maxReadFlushPeriod(maxReadFlushPeriod)
        .maxWriteFlushPeriod(maxWriteFlushPeriod)
        .build();
  }

  @Value
  @Builder
  public static class Metrics {

    long avgReadWritePeriod;

    long avgReadFlushPeriod;

    long avgWriteFlushPeriod;

    long minReadWritePeriod;

    long minReadFlushPeriod;

    long minWriteFlushPeriod;

    long maxReadWritePeriod;

    long maxReadFlushPeriod;

    long maxWriteFlushPeriod;

    int pairs;

    @Override
    public String toString () {
      return new StringBuilder()
          .append("  avg read-write:  ").append(avgReadWritePeriod).append(" nanos\n")
          .append("  avg read-flush:  ").append(avgReadFlushPeriod).append(" nanos\n")
          .append("  avg write-flush: ").append(avgWriteFlushPeriod).append(" nanos\n")
          .append("  min read-write:  ").append(minReadWritePeriod).append(" nanos\n")
          .append("  min read-flush:  ").append(minReadFlushPeriod).append(" nanos\n")
          .append("  min write-flush: ").append(minWriteFlushPeriod).append(" nanos\n")
          .append("  max read-write:  ").append(maxReadWritePeriod).append(" nanos\n")
          .append("  max read-flush:  ").append(maxReadFlushPeriod).append(" nanos\n")
          .append("  max write-flush: ").append(maxWriteFlushPeriod).append(" nanos\n")
          .append("  pairs: ").append(pairs)
          .toString();
    }
  }
}
