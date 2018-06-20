

@ErlangController
public class MyController {

  /**
   * Default listener parameters with:
   * - default mailbox (primary one)
   * - default node (primary bean)
   * - no wrapper
   */
  @MailboxListener
  public void doSome1 (ErlangTerm term) {
    // ...
  }

  /**
   * The same as previous, but with custom user POJO
   */
  @MailboxListener
  public void doSome2 (MyPojo pojo) {
    // ...
  }

  @MailboxListener(
    node = "my-node-name", // not default node
    mailbox = "the-first-one", // specify mailbox, not default one
    wrapper = TUPLE // request wrapper tupe, all args below wraped in this
  )
  public void doSome3 (@AsEralngAtom String myAtom, Integer age) {
    // ...
  }
}

@ErlangController
public class MyController {

  @MailboxListener
  public void doSome1 (@AsErlangAtom String atom) {
    System.out.println("hello!");


    builder
        .case(service).myHandler1(42, anyInt(), anyString())
        .case(service).myHandler2(3, anyInt(), anyString())
        .case(service).myHandler3(anyInt(), anyInt(), anyString());

    builder
      .case()
        .tuple(int(3), any(), any())
        .handler(...)
      .case()
        .tuple(any(), any(), any())
        .handler(...)
      .case()
        .tuple(int(42), any(), any())
        .handler(...);

  }

  @MailboxListener
  public void doSome2 (@Expected("42") Integer age) {
    System.out.println("world!");
  }

  @MailboxListener
  public void doSome3 (@Expected("3") Integer age) {
    System.out.println("popa");
  }
}
