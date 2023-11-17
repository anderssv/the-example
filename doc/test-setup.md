Tests should be easy to set up and write. Usually there are two main techniques in play:
- [Object Mother or similar patterns](https://martinfowler.com/bliki/ObjectMother.html) to set up test data in a way that is quick, but also isolates the individual tests against irrelevant changes in the domain and system.
- [Test Doubles, usually Fakes](fakes.md) to ease setup and also isloate individual tests from irrelevant changes in the system.

# Related reading
- [Easy and maintainable test data - The Kotlin way](https://anderssv.medium.com/easy-and-maintainable-test-data-the-kotlin-way-9ecbbf53d822)
- [How to decide on an architecture for automated tests](https://www.qwan.eu/2020/09/17/test-architecture.html)
- [Test scopes by Wisen Tanasa on Twitter](https://twitter.com/ceilfors/status/1687780512277069824)
