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

* **VM version:** JDK 11.0.1, OpenJDK 64-Bit Server VM, 11.0.1+13

* **VM options:** -Xms3G -Xmx4G

* **Warmup:** 10 iterations, 10 s each

* **Measurement:** 20 iterations, 10 s each

* **Timeout:** 10 min per iteration

* **Threads:** 1 thread, will synchronize iterations

* **Benchmark mode:** Throughput, ops/time

## The results

### Multi client tests

The installation consist of a server node at separate thread, which echoes the messages and **N**-threads-clients, which pitch the messages and receive it back.

| implementation                                                                                                                                                       | clients | score       | error       | units |
|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-------:|------------:|------------:|:------|
| [encon](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/Encon_Node2NodeBenchmarks.java#L130)           |       1 | `13404.955` |   `832.219` | ops/s |
| [jinterface](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/JInterface_Node2NodeBenchmarks.java#L109) |       1 | `11382.375` |   `358.907` | ops/s |
| [encon](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/Encon_Node2NodeBenchmarks.java#L138)           |       2 | `21676.979` |   `654.934` | ops/s |
| [jinterface](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/JInterface_Node2NodeBenchmarks.java#L117) |       2 | `18982.318` |   `395.596` | ops/s |
| [encon](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/Encon_Node2NodeBenchmarks.java#L146)           |       4 | `33532.304` |   `609.782` | ops/s |
| [jinterface](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/JInterface_Node2NodeBenchmarks.java#L125) |       4 | `23027.511` |   `327.508` | ops/s |
| [encon](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/Encon_Node2NodeBenchmarks.java#L154)           |       8 | `48659.749` |   `572.108` | ops/s |
| [jinterface](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/JInterface_Node2NodeBenchmarks.java#L133) |       8 | `25457.217` |   `295.665` | ops/s |

### Mailbox to mailbox

In this test we have only one node and two mailboxes which send the message to each other.

| implementation                                                                                                                                                   | score         | error       | units |
|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------:|------------:|:------|
| [encon](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/Encon_SimpleBenchmarks.java#L57)           | `2906135.871` | `46718.600` | ops/s |
| [jinterface](https://github.com/appulse-projects/encon-java/blob/master/benchmark/src/main/java/io/appulse/encon/benchmark/JInterface_SimpleBenchmarks.java#L51) | `3388805.729` | `10400.968` | ops/s |


## How to setup the environment

1. Update and upgrade the distro:

```bash
$> sudo apt-get update --yes && sudo apt-get upgrade --yes
```

2. Install `git`, `curl`, `zip` and `unzip`:

```bash
$> sudo apt-get install --yes git curl zip unzip
```

3. Install `sdkman`:

```bash
$> curl -s "https://get.sdkman.io" | bash
$> source "$HOME/.sdkman/bin/sdkman-init.sh"
```

4. Install `Java`:

```bash
$> sdk install java 11.0.1-open
```

5. Clone the repo:

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
$> ./mvnw clean package \
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
$> nohup java -Xms3G -Xmx4G -jar benchmark/target/benchmarks.jar > job.logs 2>&1 &
```

### One-liner

```bash
$> sudo apt-get update --yes && sudo apt-get upgrade --yes && \
   sudo apt-get install --yes git curl zip unzip && \
   curl -s "https://get.sdkman.io" | bash && \
   source "$HOME/.sdkman/bin/sdkman-init.sh" && \
   sdk install java 11.0.1-open && \
   git clone https://github.com/appulse-projects/encon-java.git && \
   cd encon-java && \
   ./mvnw clean package \
     -DskipTests \
     -Dgpg.skip \
     -Dfindbugs.skip=true \
     -Dpmd.skip=true \
     -Dcheckstyle.skip \
     -Dmaven.test.skip=true \
     -pl benchmark -am && \
   nohup java -Xms3G -Xmx4G -jar benchmark/target/benchmarks.jar > job.logs 2>&1 &
```
