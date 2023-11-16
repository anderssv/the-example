When writing tests it is often important to isolate the tests. This can be done in many ways, but most of them are described in [this classic article by Martin Fowler called Test Doubles](https://martinfowler.com/bliki/TestDouble.html).

They can all be useful, but they come with their own pros and cons. I will not describe it all here, but I find that there are certain important characteristics that drives what I use:

- Exposure to changes in data structures
- Exposure to changes in behaviour
- Exposure to temporary issues outside your control
- Speed
- Flakyness

# Fake it till you make it

I find Fakes to be my go to tool, and the following examples tries to show how to use fakes in "normal" development.