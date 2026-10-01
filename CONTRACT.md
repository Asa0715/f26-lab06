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

**Will the untouched consumer still compile and pass?** Yes or no, and if no,
which module goes red and whether at compile time or test time.

**Where.** Name the call sites you expect to be affected, if any.

**What about the tests in `api/`, after you update them?** And whether their
result is evidence about the consumer.

### Step 1: after the fold

**What the build printed.** Paste it for each module, including file and
line for anything that failed.

**Which module's tests ran, and which did not.** And what that tells you about
who can detect a contract break.

### Step 2: the deprecation path

**What you added.** The signatures that came back, and what they delegate to.

**The warnings.** Paste one deprecation warning line from the build log (from
a `mvn -B clean test` run, since a rerun with nothing to compile prints none).

**What the deprecation path resolves.** Who can now build that could not build
during step 1, and who is on which schedule.

**What the warnings accomplish that a README note would not.** Be concrete
about where the warning shows up and who sees it without looking for it.

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
