Part of [System Design](system-design.md)

# Why?

- Increase type safety, make sure you handle all outcomes
- Simplify code "further down", as it only has to operate on known states with less nulls and more structure
- Have a unified way of doing validation that makes sure error responses are the same across multiple endpoints
- Validate at the edges of our application code, helping us have a lazer sharp focus on busniess logic

# The example

The example is a bit contrived, but I think it serves it purpose and challenges the type system enough to see how the concepts play out.

# The story

Ever since I read Domain Driven Design I have realized that there are some untapped potential to how we write code. And especially in Object Oriented languages. It always felt clunky how we do parsing/validation/values and how our code has to take this into account, or assume everything is in order really.

So when I started coding in Kotlin a few years back it felt really good to have nullability (null checks really) built into the language. It got rid of a bunch of clunky code and you could know what values are passed.

But when accepting input from users or endpoints there are always some things that end up being nullable to enable better validation and parsing.

So when I saw the Type Driven Development talk at JavaZone i saw parts of things that could help a lot.

# Inspiration
This is a start inspired by a talk at JavaZone 2022 about Type Driven Development. It s really a way to try and do sum types in (Java in the talk) Kotlin. https://2022.javazone.no/#/program/54a82962-e8f4-424c-864a-e1f987825dc7

I wanted to see how I could include parser frameworks (Jackson) in this to get a complete pipeline where types are safer and have more information straight away. Still early work, but I do think it can be valuable.

Parse don't validate by Alexis King was mentioned and is a big inspiration: https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/