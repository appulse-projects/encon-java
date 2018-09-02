# Overview

This project contains the general-purpose data-binding functionality.

## Usage

First of all, add encon's dependency to your `JVM` app:

**Maven**:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>io.appulse.encon</groupId>
    <artifactId>encon</artifactId>
    <version>1.6.0</version>
  </dependency>
  ...
</dependencies>
```

**Gradle**:

```groovy
compile 'io.appulse.encon:encon:1.6.0'
```

Then, create a new `Erlang` node, like this:

```java

import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.Node;
import io.appulse.encon.Nodes;


// Creating node's config.
// For more details - see encon-config project
NodeConfig config = NodeConfig.builder()
    .shortNamed(true) // true - for local nodes, false (default) - for remote accessable
    .cookie("secret")
    .build();

// Creates, registers in EPMD and starts a new Erlang node
Node node = Nodes.singleNode("echo-node", config);

```

After `node` creation, we could register several mailboxes:

```java

import static io.appulse.encon.terms.Erlang.tuple;

import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;


// Mailbox #1
// ----------
// By default Mailbox uses `java.util.concurrent.LinkedBlockingQueue`
// under the hood.
//
Mailbox mailbox1 = node.mailbox()
    .name("echo-mailbox-1")
    .build();

for (int count = 0; count < 3; count++) {
  // Mailbox.receive() is a blocking operation
  Message message = mailbox.receive();

  ErlangTerm body = message.getBody();
  mailbox1.send("another-node", "another-mailbox", tuple(
    mailbox1.getPid(), body.getUnsafe(1)
  ));
}


// Mailbox #2
// ----------
// You can set your own `java.util.concurrent.BlockingQueue` instance.
// `java.util.concurrent.SynchronousQueue` instance in that case.
//
Mailbox mailbox2 = node.mailbox()
    .name("echo-mailbox-2")
    .queue(new SynchronousQueue<>())
    .build();

while (true) {
  Message message = mailbox2.receive();

  ErlangTerm body = message.getBody();
  mailbox2.send("another-node", "another-mailbox", tuple(
    mailbox2.getPid(), body.getUnsafe(1)
  ));
}

```
