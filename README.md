# Overview

[![build_status](https://travis-ci.org/appulse-projects/encon-java.svg?branch=master)](https://travis-ci.org/appulse-projects/encon-java)
[![maven_central](https://maven-badges.herokuapp.com/maven-central/io.appulse.encon/encon/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.appulse.encon/encon)

Java implementation of **E**rlang **N**ode **CON**nector (using Eralng Distribution Protocol). For more information, visit the [site](https://appulse.io).

Sub-modules descriptions:

- [Encon common](./encon-common/README.md) - the set of common classes for all subprojects;
- [Encon terms](./encon-terms/README.md) - Erlang's terms classes and different helpers;
- [Encon config](./encon-config/README.md) - configuration builder classes;
- [Encon](./encon/README.md) - the lib itself;
- [Encon databind](./encon-databind/README.md) - serializers/deserializers from/to Java/Eralng objects;
- [Encon handler](./encon-handler/README.md) - helpers for handling received messages in mailbox;
- [Encon Spring](./encon-spring/README.md) - Spring Boot integration;
- [Encon examples](./examples/README.md) - different encon's examples;
- [Encon benchmark](./benchmark/README.md) - encon's benchmarks;

## Development

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

For building the project you need only a [Java compiler](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

> **IMPORTANT:** the project requires Java version starting from **8**

And, of course, you need to clone the project from GitHub:

```bash
$> git clone https://github.com/appulse-projects/encon-java
$> cd encon-java
```

### Building

For building routine automation, I am using [maven](https://maven.apache.org).

To build the project, do the following:

```bash
$> mvn clean compile
...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] encon 1.6.3 ........................................ SUCCESS [  1.210 s]
[INFO] encon-common ....................................... SUCCESS [ 25.693 s]
[INFO] encon-terms ........................................ SUCCESS [ 27.517 s]
[INFO] encon-config ....................................... SUCCESS [ 18.707 s]
[INFO] encon-databind ..................................... SUCCESS [ 22.462 s]
[INFO] encon .............................................. SUCCESS [ 36.247 s]
[INFO] encon-handler ...................................... SUCCESS [ 17.436 s]
[INFO] encon-spring ....................................... SUCCESS [ 11.516 s]
[INFO] examples ........................................... SUCCESS [  0.024 s]
[INFO] simple ............................................. SUCCESS [  5.337 s]
[INFO] echo-server ........................................ SUCCESS [  5.840 s]
[INFO] echo-server-spring ................................. SUCCESS [  7.716 s]
[INFO] custom-queue ....................................... SUCCESS [  3.920 s]
[INFO] databind ........................................... SUCCESS [  5.045 s]
[INFO] handler-basic ...................................... SUCCESS [  6.443 s]
[INFO] handler-advanced ................................... SUCCESS [ 11.289 s]
[INFO] load-config ........................................ SUCCESS [  3.725 s]
[INFO] load-config-spring ................................. SUCCESS [  6.420 s]
[INFO] benchmark 1.6.3 .................................... SUCCESS [  5.594 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 01:15 min
[INFO] Finished at: 2018-08-20T18:36:04+03:00
[INFO] ------------------------------------------------------------------------
```

### Running the tests

To run the project's test, do the following:

```bash
$> mvn clean test
...
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
...
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 67, Failures: 0, Errors: 0, Skipped: 0
[INFO]
...
```

Also, if you do `package` or `install` goals, the tests launch automatically.

## Built With

* [Java](http://www.oracle.com/technetwork/java/javase) - is a systems and applications programming language

* [Lombok](https://projectlombok.org) - is a java library that spicing up your java

* [Junit](http://junit.org/junit4/) - is a simple framework to write repeatable tests

* [AssertJ](http://joel-costigliola.github.io/assertj/) - AssertJ provides a rich set of assertions, truly helpful error messages, improves test code readability

* [Maven](https://maven.apache.org) - is a software project management and comprehension tool

## Changelog

To see what has changed in recent versions of the project, see the [changelog](./CHANGELOG.md) file.

## Contributing

Please read [contributing](./CONTRIBUTING.md) file for details on my code of conduct, and the process for submitting pull requests to me.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/appulse-projects/encon-java/tags).

## Authors

* **[Artem Labazin](https://github.com/xxlabaza)** - creator and the main developer

* **[Sokol Andrey](https://github.com/SokolAndrey)** - texts corrector and mastermind

## License

This project is licensed under the Apache License 2.0 License - see the [license](./LICENSE) file for details
