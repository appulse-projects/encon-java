# Overview

Encon's configuration classes.

## Usage

First of all, add config's dependency:

**Maven**:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>io.appulse.encon</groupId>
    <artifactId>encon-config</artifactId>
    <version>1.6.3</version>
  </dependency>
  ...
</dependencies>
```

**Gradle**:

```groovy
compile 'io.appulse.encon:encon-config:1.6.3'
```

### File based configuration

The most likely way to configure your encon instance is loading all settings from file.

Loading `YAML` configuration file:

```java

Config config = Config.load("connector.yml");
```

`YAML` config content example:

```yaml

#
# Block of different default settings for nodes.
# This block is optional.
#
defaults:
  epmd-port: 8888  # default EPMD port is 4369, but here we can override it
  type: R3_ERLANG
  short-name: true
  cookie: secret
  protocol: UDP
  low-version: R4
  high-version: R6
  distribution-flags:
    - MAP_TAG
    - BIG_CREATION
  server:
    boss-threads: 2
    worker-threads: 4

#
# Listing all needed nodes
#
nodes:
  node-1:
    epmd-port: 7373
    type: R3_HIDDEN
    cookie: non-secret
    protocol: SCTP
    low-version: R5C
    high-version: R6
    distribution-flags:
      - EXTENDED_REFERENCES
      - EXTENDED_PIDS_PORTS
      - BIT_BINARIES
      - NEW_FLOATS
      - FUN_TAGS
      - NEW_FUN_TAGS
      - UTF8_ATOMS
      - MAP_TAG
      - BIG_CREATION
    mailboxes:
      - name: net_kernel
      - name: another
      - name: another_one
    server:
      port: 8971
      boss-threads: 1
      worker-threads: 2

  node-2:
    short-name: false
    cookie: popa
    mailboxes:
      - name: net_kernel

```

### Manual configuration

You are also able to create your `Config` manually, like this, via builders:

```java
Config config = Config.builder()
    .defaults(Defaults.builder()
        .epmdPort(8888)
        .type(R3_ERLANG)
        .shortName(true)
        .cookie(secret)
        .protocol(UDP)
        .lowVersion(R4)
        .highVersion(R6)
        .distributionFlags(new HashSet<>(asList(
            MAP_TAG,
            BIG_CREATION
        )))
        .server(ServerConfig.builder()
            .bossThreads(2)
            .workerThreads(4)
            .build())
        .build()
    )
    .node("node-1", NodeConfig.builder()
        .epmdPort(7373)
        .type(R3_HIDDEN)
        .cookie("non-secret")
        .protocol(SCTP)
        .lowVersion(R5C)
        .highVersion(R6)
        .distributionFlag(EXTENDED_REFERENCES)
        .distributionFlag(EXTENDED_PIDS_PORTS)
        .distributionFlag(BIT_BINARIES)
        .distributionFlag(NEW_FLOATS)
        .distributionFlag(FUN_TAGS)
        .distributionFlag(NEW_FUN_TAGS)
        .distributionFlag(UTF8_ATOMS)
        .distributionFlag(MAP_TAG)
        .distributionFlag(BIG_CREATION)
        .mailbox(MailboxConfig.builder()
            .name("net_kernel")
            .build())
        .mailbox(MailboxConfig.builder()
            .name("another")
            .build())
        .mailbox(MailboxConfig.builder()
            .name("another")
            .build())
        .server(ServerConfig.builder()
            .port(8971)
            .bossThreads(1)
            .workerThreads(2)
            .build())
        .build()
    )
    .node("node-2", NodeConfig.builder()
        .shortName(false)
        .cookie("popa")
        .mailbox(MailboxConfig.builder()
            .name("net_kernel")
            .build())
        .build()
    )
    .build();
```
