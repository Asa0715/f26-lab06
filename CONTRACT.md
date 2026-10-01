# Contract Worksheet

One section per milestone. Fill each one in as you go, in order. Write each
prediction before you run anything. That is the part a TA asks about.

Keep it short and specific. Point at methods, call sites, and error text.

---

## Milestone 1: The notes overload

### Prediction (write this before you run the build, and you can deliberate with your agent)

**Will the consumer, untouched, still compile and pass?** Yes.

**Why.** 
In general it depends on how the overload differs from the existing method.
When the consumer is recompiled, the compiler re-runs overload resolution at
each existing `createBooking` call site: it collects every overload the
arguments could match and picks the most specific one.

Here the new overload adds a parameter, so its number of parameters differs from every existing call site (4 args at FrontDesk.java:27 and :33). Tthe compiler still picks the original `createBooking(String, long, long, String)` at both sites. Even the `null` at line 27 is not ambiguous, since only one overload takes 4 arguments.

If an overload instead had the same number of parameters and differed only intype, e.g. `createBooking(String, long, long, Integer)`, the `null` at line 27 would match both, neither is more specific, and the compiler would fail with "reference to createBooking is ambiguous".


### What happened

**The result.** The consumer was recompiled against the changed `api` module without being
edited, and it still compiled and passed. It did not notice the new overload.

**If your prediction was wrong,** 
My prediction (Yes) was correct. Both existing call sites, FrontDesk.java:27 and :33, pass 4 arguments. The new overload takes 5 parameters, so overload resolution still picks the original createBooking(String, long, long, String).

**Is an additive change always safe in Java?** No, like I mentioned in prediction, if someone adds an overload with the same number of parameters but a different type, for example createBooking(String, long, long, Integer). Then the call api.createBooking(roomId, s, e, null) at FrontDesk.java:27 matches both versions, and neither is more specific. The consumer fails to compile with reference to createBooking is ambiguous, even though no existing method changed.

---

## Milestone 2: The request object

### Prediction (write this before you run the build)

**Will the untouched consumer still compile and pass?** 

No, `lab06-consumer` goes red at compile time, not test time. After the fold, BookingApi has only `createBooking(BookingRequest)`, so the consumer's existing 4-argument calls no longer match any method.

**Where.** 

- FrontDesk.java:27 (bookWalkIn)
- FrontDesk.java:33 (joinWaitlist)

**What about the tests in `api/`, after you update them?** And whether their
result is evidence about the consumer.

After update, they should pass: all five tests in InMemoryBookingServiceTest should pass, and lab06-api stays green. 

That result is not evidence about the consumer. The api/ tests are written by the API owner against the new contract, so they only show that the new method is implemented correctly. They never compile or run any consumer code, so they cannot see that FrontDesk.java:27 and :33 still use the old 4-argument signature. Only the consumer's own build, when it recompiles against the new API, can detect that break. 

### Step 1: after the fold

**What the build printed.**

`lab06-api`
```
[INFO] --- compiler:3.13.0:compile (default-compile) @ lab06-api ---
[INFO] Compiling 5 source files with javac [debug deprecation release 21] to target/classes
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ lab06-api ---
[INFO] Compiling 1 source file with javac [debug deprecation release 21] to target/test-classes
[INFO] Running edu.cmu.cs214.booking.InMemoryBookingServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

`lab06-consumer`
```
[INFO] --- compiler:3.13.0:compile (default-compile) @ lab06-consumer ---
[INFO] Recompiling the module because of changed dependency.
[ERROR] COMPILATION ERROR :
[ERROR] consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[27,19] method createBooking in interface edu.cmu.cs214.booking.BookingApi cannot be applied to given types;
  required: edu.cmu.cs214.booking.BookingRequest
  found:    java.lang.String,long,long,<nulltype>
  reason: actual and formal argument lists differ in length
[ERROR] consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[33,19] method createBooking in interface edu.cmu.cs214.booking.BookingApi cannot be applied to given types;
  required: edu.cmu.cs214.booking.BookingRequest
  found:    java.lang.String,long,long,java.lang.String
  reason: actual and formal argument lists differ in length
[INFO] 2 errors
```

Reactor summary
```
[INFO] lab06-booking-parent ............................... SUCCESS
[INFO] lab06-api .......................................... SUCCESS
[INFO] lab06-consumer ..................................... FAILURE
[INFO] BUILD FAILURE
```

**Which module's tests ran, and which did not.** 

Only lab06-api's tests ran: InMemoryBookingServiceTest, 5 tests, all passed. 

lab06-consumer's tests did not run. The module failed in compiler:compile on FrontDesk.java:[27,19] and [33,19], 
so Maven never compiled its test sources or ran surefire.

This shows that the API owner cannot detect a contract break with their own tests. 
Those tests were rewritten against the new signature and are green, 
but they never compile or run the consumer's code. 
The break is visible only in the caller's build, when the consumer recompiles against the new API. 
By the time anyone sees it, the caller is already broken, and fixing it means editing code 
the API owner does not own. 

### Step 2: the deprecation path

**What you added.** 

Both old positional signatures came back on `BookingApi` as `@Deprecated`
`default` methods:

```java
@Deprecated
default Booking createBooking(String roomId, long startMinute, long endMinute,
                              String waitlistKey) {
    return createBooking(new BookingRequest(roomId, startMinute, endMinute)
            .withWaitlistKey(waitlistKey));
}

@Deprecated
default Booking createBooking(String roomId, long startMinute, long endMinute,
                              String waitlistKey, String notes) {
    return createBooking(new BookingRequest(roomId, startMinute, endMinute)
            .withWaitlistKey(waitlistKey).withNotes(notes));
}
```

Each one builds a `BookingRequest` from its arguments and delegates to the new
`createBooking(BookingRequest)`, so the booking logic exists in only one place.
Each javadoc has a `@deprecated` tag that names the replacement. Because they
are `default` methods, `InMemoryBookingService` and any other implementor of
`BookingApi` do not need to implement them.

**The warnings.** Paste one deprecation warning line from the build log (from
a `mvn -B clean test` run, since a rerun with nothing to compile prints none).

```
[WARNING] /Users/xue/CMU courses/2026 Fall/17514/labs/f26-lab06/consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[27,19] createBooking(java.lang.String,long,long,java.lang.String) in edu.cmu.cs214.booking.BookingApi has been deprecated
```

A second warning points at `FrontDesk.java:[33,19]`, the same method called
from `joinWaitlist`. Both appear during `compiler:compile` of `lab06-consumer`,
the step that failed with an error in step 1. `lab06-api` prints no
deprecation warnings, because its own code and tests already use the new
method.

**What the deprecation path resolves.** Who can now build that could not build
during step 1, and who is on which schedule.

The front desk team (`consumer/`) can build again. In step 1,
`FrontDesk.java:27` and `:33` were compile errors, so `lab06-consumer` was
FAILURE and `FrontDeskTest` never ran. Now, with no edits to `consumer/`, the
same two lines compile with only warnings, all 7 `FrontDeskTest` tests run and
pass, and the build is SUCCESS.

The schedules are now decoupled:

- **Existing callers** like the front desk keep using the old signatures and
  migrate to `createBooking(BookingRequest)` on their own schedule, guided by
  the warnings.
- **New callers** use `createBooking(BookingRequest)` from the start. The API
  owner's own code and tests already do.
- **The API owner** keeps the deprecated methods working until callers have
  migrated, and removes them only in a later, announced breaking release.
  Removing them now would recreate the step 1 failure.

**What the warnings accomplish that a README note would not.** Be concrete
about where the warning shows up and who sees it without looking for it.

The front desk developers see it without looking for it. Every time they
compile, `javac` prints `FrontDesk.java:[27,19] ... has been deprecated` in
their own build log, and their IDE strikes through `createBooking` at that line.

- **Pushed, not pulled.** A README is seen only by someone who goes and reads
  it. The warning shows up in the caller's build automatically.
- **Precise.** It names the exact file and line to change, so the caller does
  not have to search their code.
- **Self-clearing.** It repeats until the call site is migrated, then goes
  away, so the remaining warnings are a to-do list.

---

## Milestone 3: The misuse critique

Not coded. One misuse, one redesign, one cost. Discuss it with your TA.

### The misuse

**What is easy to get wrong.** One specific thing about the API surface.

**The call site.** File and line in `consumer/`, with the call. Show the
code that a reader cannot understand without opening the javadoc, or that a
caller could get wrong with the compiler still happy.

**What goes wrong when it happens.** Silent bad behavior, wrong data, a crash
somewhere far away?

### The redesign

**The proposal.** Types, enums, factories, or whatever you are proposing. Show
the new signature and the new call site.

**Why the mistake is now hard or impossible to make.** Point at the mechanism,
such as the compiler, a validating constructor, or an exhaustive switch.

### One tradeoff

**What it costs.** Something real, such as caller ceremony, migration burden
against the deprecation path you just built, or more types for a newcomer to
learn. "No real downside" does not count.

**When the price is worth paying.** A condition under which it is.
