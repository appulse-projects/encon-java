# Overview

This is a Java library that can be used to convert Java Objects into their Erlang representation.

## Usage

First of all, add databind's dependency:

**Maven**:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>io.appulse.encon</groupId>
    <artifactId>encon</artifactId>
    <version>1.6.3</version>
  </dependency>
  <dependency>
    <groupId>io.appulse.encon</groupId>
    <artifactId>encon-databind</artifactId>
    <version>1.6.3</version>
  </dependency>
  ...
</dependencies>
```

**Gradle**:

```groovy
compile 'io.appulse.encon:encon:1.6.3'
compile 'io.appulse.encon:encon-databind:1.6.3'
```

Let's imagine, you have POJO like this:

```java

// ErlangList will be used for
@AsErlangList
public class Pojo {

  // it will be serialized as ErlangString
  String name;

  // it will be serialized as ErlangInteger
  int age;

  // it will be serialized as ErlangAtom
  boolean male;

  // this field will be ignored
  @IgnoreField
  int ignored;

  // it will be serialized as ErlangList with ErlangString elements
  List<String> languages;

  // this field will be serialized as ErlangAtom, obviously
  @AsErlangAtom
  String position;

  // it will be serialized as ErlangList with ErlangString elements
  @AsErlangList
  Set<String> set;

  // this string will be serialized as ErlangList with ErlangInteger elements
  @AsErlangList
  String listString;

  // it will be serialized as ErlangList with ErlangAtom elements
  @AsErlangList
  Boolean[] bools;
}
```

After that, you could serialize/deserialize it with the following code:

```java

// serialization
ErlangTerm erlangTerm = TermMapper.serialize(pojo);

// deserialization
Pojo result = TermMapper.deserialize(erlangTerm, Pojo.class);
```
