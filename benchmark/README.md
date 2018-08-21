# Overview

## Benchmark host machine

* **JMH version:** 1.21

* **VM version:** JDK 1.8.0_161, Java HotSpot(TM) 64-Bit Server VM, 25.161-b12

* **VM options:** <none>

* **Warmup:** 5 iterations, 10 s each

* **Measurement:** 5 iterations, 10 s each

* **Timeout:** 10 min per iteration

* **Threads:** 1 thread, will synchronize iterations

* **Benchmark mode:** Throughput, ops/time

## Results

| Benchmark                                                                                                      | Mode  | Cnt | Score       | Error       | Units |
|----------------------------------------------------------------------------------------------------------------|-------|-----|------------:|------------:|-------|
| [EnconBenchmark.mailbox2mailbox](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L103)          | thrpt | 25  | 4611737.930 | ± 54595.578 | ops/s |
| [EnconBenchmark.node2node](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L177)                | thrpt | 25  |   13969.833 |   ± 164.199 | ops/s |
| [EnconBenchmark.oneDirection](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L167)             | thrpt | 25  |   28028.764 |   ± 262.720 | ops/s |
| [JInterfaceBenchmark.mailbox2mailbox](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L99) | thrpt | 25  | 4370604.847 | ± 15769.278 | ops/s |
| [JInterfaceBenchmark.node2node](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L175)      | thrpt | 25  |   17196.521 |   ± 113.159 | ops/s |
| [JInterfaceBenchmark.oneDirection](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L165)   | thrpt | 25  |   34529.556 |   ± 207.977 | ops/s |
