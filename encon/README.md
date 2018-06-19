# Overview

This project contains the general-purpose data-binding functionality.

## Usage

First of all, add encon's dependency:

**Maven**:

```xml
<dependencies>
    ...
    <dependency>
        <groupId>io.appulse.encon</groupId>
        <artifactId>encon</artifactId>
        <version>1.1.0</version>
    </dependency>
    ...
</dependencies>
```

**Gradle**:

```groovy
compile 'io.appulse.encon.java:encon:1.1.0'
```

Let's write a simple Erlang's `echo`-server:

```java

import static io.appulse.encon.java.terms.Erlang.tuple;

import io.appulse.encon.java.config.NodeConfig;
import io.appulse.encon.java.Node;
import io.appulse.encon.java.Nodes;
import io.appulse.encon.module.connection.regular.Message;
import io.appulse.encon.module.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;



// Creating node's config.
// For more details - see encon-config project
NodeConfig config = NodeConfig.builder()
    .cookie("secret")
    .build();


// Creates, registers and starts a new Erlang node
Node node = Nodes.singleNode("echo-node", config);


// Handling option #1
// You can handle incoming request via providing special handler.
Mailbox mailbox1 = node.mailbox()
    .name("echo-mailbox-1")
    .handler((self, header, body) -> {
        // expects tuple, where the first element (index - 0) is a caller Pid
        self.request()
            .body(tuple(self.getPid(), body.getUnsafe(1)))
            .send(body.getUnsafe(0).asPid());
    })
    .build();


// Handling option #2
// You can also use future API and do something like infinity loop.
Mailbox mailbox2 = node.mailbox()
    .name("echo-mailbox-2")
    .build();

while (true) {
  // 'receive' - is a blocking operation
  Message message = mailbox2.receive();
  ErlangTerm body = message.getBodyUnsafe();
  mailbox2.request()
      .body(tuple(mailbox2.getPid(), body.getUnsafe(1)))
      .send(body.getUnsafe(0).asPid());
}
```
