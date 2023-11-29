Mainly inspired by [Onion Archtiecture](https://medium.com/@alessandro.traversi/understanding-onion-architecture-an-example-folder-structure-9c62208cc97d#:~:text=Onion%20Architecture%20is%20a%20software,easier%20to%20evolve%20over%20time.).

Distilled:
- Parse at the edges, operate on strongly typed objects after that
- Validate at the edges, have as few variations as possible "inside" the onion

See [Sum Types](sum-types.md) for a test of how to make Jackson/JSON-parsing validate and "tighten" the variance as early as possible.