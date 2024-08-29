> I am an independent consultant and would love to help your team get better at continuous delivery.
> Reach out
> at [anders.sveen@mikill.no](mailto:anders.sveen@mikill.no) or go
> to [https://www.mikill.no](https://www.mikill.no/contact.html) to contact, follow on social media or to see more of
> my work.

# System Design

Mainly inspired by [Onion Architecture](https://medium.com/@alessandro.traversi/understanding-onion-architecture-an-example-folder-structure-9c62208cc97d#:~:text=Onion%20Architecture%20is%20a%20software,easier%20to%20evolve%20over%20time.).

Distilled:
- Parse at the edges, operate on strongly typed objects after that
- Validate at the edges, have as few variations as possible "inside" the onion

Sections:
- [Manual Dependency Injection](manual-dependency-injection.md)
- [Sum Types](sum-types.md) desciber how to make Jackson/JSON-parsing validate and "tighten" the variance as early as possible.
