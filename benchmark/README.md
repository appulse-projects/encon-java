# Overview

## Benchmark host machine

For benchmarking we are using `DigitalOcean`'s CPU Optimized Droplets:

```bash
$> sudo lshw -short
H/W path      Device      Class      Description
================================================
                          system     Droplet
/0                        bus        Motherboard
/0/0                      memory     96KiB BIOS
/0/401                    processor  Intel(R) Xeon(R) CPU E5-2697A v4 @ 2.60GHz
/0/402                    processor  Intel(R) Xeon(R) CPU E5-2697A v4 @ 2.60GHz
/0/1000                   memory     4GiB System Memory
/0/1000/0                 memory     4GiB DIMM RAM
/0/100                    bridge     440FX - 82441FX PMC [Natoma]
/0/100/1                  bridge     82371SB PIIX3 ISA [Natoma/Triton II]
/0/100/1.1                storage    82371SB PIIX3 IDE [Natoma/Triton II]
/0/100/1.2                bus        82371SB PIIX3 USB [Natoma/Triton II]
/0/100/1.2/1  usb1        bus        UHCI Host Controller
/0/100/1.3                bridge     82371AB/EB/MB PIIX4 ACPI
/0/100/2                  display    QXL paravirtual graphic card
/0/100/3                  network    Virtio network device
/0/100/3/0    eth0        network    Ethernet interface
/0/100/4                  storage    Virtio SCSI
/0/100/4/0                generic    Virtual I/O device
/0/100/5                  storage    Virtio block device
/0/100/5/0    /dev/vda    disk       26GB Virtual I/O device
/0/100/5/0/1  /dev/vda1   volume     24GiB EXT4 volume
/0/100/5/0/e  /dev/vda14  volume     4095KiB BIOS Boot partition
/0/100/5/0/f  /dev/vda15  volume     105MiB Windows FAT volume
/0/100/6                  generic    Virtio memory balloon
/0/100/6/0                generic    Virtual I/O device
```

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
$> sudo apt-get install -y oracle-java8-installer git maven
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
