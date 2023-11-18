This is part of [TDD](tdd.md)

> Testing Through the Domain... The name was a kind of a joke because TTTD is really hard to pronounce. And here we are...

This one is a bit hard to see, but I do think it raises the level for tests and maintenance in the long run. Consider two tests:

> I know this example doesn't make sense now. Working on it. It is worth it I promise. :) 

X: Is threshold for reopen + 1

Data Oriented Test:
- Set up Application with status=Approved and created_time for -X days
- Add it to the data store
- Check that Service.reopen(application) is not possible because it is too old

Testing Through the Domain:
- Set up the clock to be -X days
- Set up a new Application and call Service.register(application) (with clock)
- Call Service.approve(application) (with clock)
- Check that Service.reopen(application) is not possible because it is too old

Both tests verify that the outcome is the correct one. But...

If we add the requirement that the Application can be re-opened based on the **time of approval**, they would respond differently.

DOT: Figure out what data needs to be added to set a different approval date

TTTD: Progress the clock between registration and approval

This is a contrived example, but it is just amplified in real life. When you have changes in data structures or logic how many places do you have to fix issues? This will reduce that. Object Mother helps, but this is the missing companion to that.

It will also update all the data-structures that you don't really need (or know that you need) in your tests such that you are testing a much more consistent state in your domain all the time. Especially in Kotlin this makes it much easier to have non-nullable fields with real values all over your domain.