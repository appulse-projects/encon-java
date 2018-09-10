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
| [EnconBenchmark.mailbox2mailbox](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L103)          | thrpt | 25  | 4562837.232 | ± 48730.020 | ops/s |
| [EnconBenchmark.node2node](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L177)                | thrpt | 25  |   13744.084 |   ± 160.906 | ops/s |
| [EnconBenchmark.oneDirection](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L167)             | thrpt | 25  |   27665.670 |   ± 230.607 | ops/s |
| [JInterfaceBenchmark.mailbox2mailbox](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L99) | thrpt | 25  | 4345167.985 | ± 22392.570 | ops/s |
| [JInterfaceBenchmark.node2node](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L175)      | thrpt | 25  |   13850.978 |   ± 126.660 | ops/s |
| [JInterfaceBenchmark.oneDirection](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L165)   | thrpt | 25  |   27590.545 |   ± 253.874 | ops/s |

How to run the benchmarks:

```bash
$> mvn clean package \
     -DskipTests \
     -Dgpg.skip \
     -Dfindbugs.skip=true \
     -Dpmd.skip=true \
     -Dcheckstyle.skip \
     -Dmaven.test.skip=true \
     -pl benchmark -am; and \
   java -Xms1G -Xmx2G -jar benchmark/target/benchmarks.jar
```
