This is part of [TDD](tdd.md)

> Testing Through the Domain... The name was a kind of a joke because TTTD is really hard to pronounce. And here we are...

This one is a bit hard to see, but I do think it raises the level for tests and maintenance in the long run. Consider two tests:

> Working on an example... ;) 

When you have changes in data structures or logic how many places do you have to fix issues? TTTD will reduce that. Object Mother helps, but this is the missing companion to that.

It will also update all the data-structures that you don't really need (or know that you need) in your tests such that you are testing a much more consistent state in your domain all the time. Especially in Kotlin this makes it much easier to have non-nullable fields with real values all over your domain.