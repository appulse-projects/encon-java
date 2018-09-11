# Overview

## Benchmark host machine

* **JMH version:** 1.21

* **VM version:** JDK 1.8.0_181, Java HotSpot(TM) 64-Bit Server VM, 25.181-b13

* **VM options:** -Xms1G -Xmx2G

* **Warmup:** 10 iterations, 10 s each

* **Measurement:** 20 iterations, 10 s each

* **Timeout:** 10 min per iteration

* **Threads:** 1 thread, will synchronize iterations

* **Benchmark mode:** Throughput, ops/time

## The results

| Benchmark                                                                                                      | Mode  | Cnt | Score       | Error       | Units |
|----------------------------------------------------------------------------------------------------------------|-------|-----|------------:|------------:|-------|
| [EnconBenchmark.mailbox2mailbox](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L103)          | thrpt | 25  | 4562837.232 | ± 48730.020 | ops/s |
| [EnconBenchmark.node2node](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L177)                | thrpt | 25  |   13744.084 |   ± 160.906 | ops/s |
| [EnconBenchmark.oneDirection](./src/main/java/io/appulse/encon/benchmark/EnconBenchmark.java#L167)             | thrpt | 25  |   27665.670 |   ± 230.607 | ops/s |
| [JInterfaceBenchmark.mailbox2mailbox](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L99) | thrpt | 25  | 4345167.985 | ± 22392.570 | ops/s |
| [JInterfaceBenchmark.node2node](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L175)      | thrpt | 25  |   13850.978 |   ± 126.660 | ops/s |
| [JInterfaceBenchmark.oneDirection](./src/main/java/io/appulse/encon/benchmark/JInterfaceBenchmark.java#L165)   | thrpt | 25  |   27590.545 |   ± 253.874 | ops/s |

## How to setup the environment

1. Add Java repository:

```bash
$> sudo add-apt-repository ppa:webupd8team/java
```

2. Update and upgrade the distro:

```bash
$> sudo apt-get update && sudo apt-get upgrade
```

3. Install `Git`, `Java 8` and `Maven`:

```bash
$> sudo apt-get install oracle-java8-installer git maven
```

4. Clone the repo:

```bash
$> git clone https://github.com/appulse-projects/encon-java.git
```

## How to run the benchmarks

1. Go to the project's root:

```bash
$> cd encon-java
```

2. Build the project with only needed dependencies:

```bash
$> mvn clean package \
     -DskipTests \
     -Dgpg.skip \
     -Dfindbugs.skip=true \
     -Dpmd.skip=true \
     -Dcheckstyle.skip \
     -Dmaven.test.skip=true \
     -pl benchmark -am
```

3. Run the tests

```bash
$> nohup java -Xms1G -Xmx2G -jar benchmark/target/benchmarks.jar > job.logs 2>&1 &
```
