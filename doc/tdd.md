> :white_check_mark: This is the basis for some of the code examples in this repo. To jump straight into the test related example go to: [ApplicationFakeTest.kt](../src/test/kotlin/fakes/ApplicationFakeTest.kt)

Automated testing is always important to be able to go to production reliably.
I find that I prefer to do TDD, and I think more people should too.
But there seems to be some hurdles to get over for most people, and I think I know some decent ways to do just that.
:smile:

Writing tests first has to be predictable and easy. I find that a certain combination of techniques gets me very close to that.

To write good tests, they should be:
- Predictable (not flaky)
- Readable
- Easy to write
- Maintainable
  - Resistant to irrelevant changes in the domain
  - Resistant to irrelevant changes in the systems behavior
- Fast

There are a bunch of other attributes as well, but these are the things I tend to focus the most on.

And these are the main techniques that I use:

- [Test Setup](test-setup.md) to centralise and re-use test data and system set-up. This makes it easier to find test data and write tests. It also reduces the exposure that each test has to the datastructures, and because of that it makes changes easier.
- [Fakes](fakes.md) as the main method for setting up test doubles. Fakes are more reusable than the alternatives, and gives a lower coupling to internal implementation details than, for example, mocks. Because of this they make it easier to write tests, and less exposed to irrelevant changes.
- [Testing Through the Domain](tttd.md), where you use the system actions to mutate state to the point in time you want to write assertions. This helps improve the domain code, forcing it to be clear at a high level readable steps, but also reduces the exposure tests have to changes in the logic of the system. A test that sets up assumed data is more exposed when the logic that produces those data changes.

```mermaid
flowchart LR
    TTTD[Testing Through<br/>the Domain]
    ReusableTestSetup[Reusable set up across tests]
    SequenceIsolation[Hides internal sequence of<br/> interactions from the test]
    ReusableTestData[Reusable test data set up<br/>across tests]
    ReducedCouplingData[Reduces coupling between test <br/>and data models]
    ReducedCouplingBehaviour[Reduces coupling between tests<br/>and system behavior]
    ClarityDomain[Clearer domain because it is repeatedly<br/>expressed in tests]
    ClarityTests[Clearer tests as they<br/>reflect domain operations]
    
    TDD --> Fakes
    TDD --> ObjectMother
    TDD --> TTTD
    Fakes --> ReusableTestSetup
    Fakes --> SequenceIsolation
    ObjectMother --> ReusableTestData
    ObjectMother --> ReducedCouplingData
    TTTD --> ReducedCouplingBehaviour
    TTTD --> ClarityDomain
    TTTD --> ClarityTests
```

It is not always easy to separate these levels, and they are kind of interconnected and feed into each other.

# Test everything?

The good old days of measuring test coverage is over I hope, but I still measure it though.
It is nice to see if it is trending upwards or downwards.
The tools that measure coverage can show _what_ is tested or not.
That is important information in finding areas that *should* improve.
:smile:

[How to decide on an architecture for automated tests](https://www.qwan.eu/2020/09/17/test-architecture.html) gives a really nice overview of different considerations. But I find I am less methodical when deciding and use my intuition.

I tend to write two or three types of tests:
- **IO tests** — No Fakes — Like testing HTTP calls to a third party provider. This makes sure the Client class performs as expected. Or Database repos.
- **Variation tests** — Fakes — Focus in on a part of the system. Test the available variations. Example: In generating mail content it should have this section included if the recipient has X attribute.
- **Outcome tests** — Fakes — This focuses on outcomes in interactions between components. Example: Was an email sent out with a rejected subject when the application was rejected?

Avoid verifying data in the database as much as possible,
test whether the email was sent through the `EmailClient` instead of checking if the `sent=true` in the database.
:smile:

# Switching between levels
I usually start at (almost) "the top" of the feature I am trying to solve, and start typing. There are generally two things I look for then:
- Is it easy to write the test? Why not? Is it worth fixing now? Probably...
- When I make the first call to a service what underlying repos/adapters/clients do I need?

The last point often makes me switch to a different mode: Prepare the "bottom" for what I need to fix the entire feature. Then I might switch to dedicated tests for something like "find all expired applications in the database", before I return up top again. This cycle will be repeated multiple times while developing a feature.

```mermaid
stateDiagram-v2
    [*] --> SetUp
    SetUp --> FixSetUp: Hard?
    SetUp --> HighLevelTest
    FixSetUp --> HighLevelTest: Make set up great again!
    HighLevelTest --> AddToService
    AddToService --> LowLevelTest: Oh, I need this thing
    state LowLevelTest {
      [*] --> WriteLowLevelTest
      WriteLowLevelTest --> AddToClient
      AddToClient --> AssertLowLevel
      AssertLowLevel --> [*]
    }
    LowLevelTest --> HighLevelTest: That was easy!
    AddToService --> AssertFeature: Don't forget to make<br/>assertions great too!
    AssertFeature --> [*]

```

# Related reading
- [How to decide on an architecture for automated tests](https://www.qwan.eu/2020/09/17/test-architecture.html)
- [Test scopes by Wisen Tanasa on Twitter](https://twitter.com/ceilfors/status/1687780512277069824)

Go [here to reach out for input or questions](../README.md). :smile:
