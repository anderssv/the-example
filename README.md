Hi. This is my repo for everything and nothing. It is used in [several blog posts](https://blog.f12.no) and for workshops.

# The Examples
This is a small collection of code I hope shows some good concepts. Please let me know what you think. 😄

This repo is updated continuously, so be sure to star and watch it for updates.

A few notes on things I am trying to show in this code. You can find links to the specific examples on each page (if there are any):

- [Test Driven Development](doc/tdd.md)
  - [Test Fakes](doc/fakes.md)
  - [Test Setup](doc/test-setup.md)
  - [Testing Through The Domain](doc/tttd.md)
- [System Design](doc/system-design.md)
  - [Manual Dependency Injection](doc/manual-dependency-injection.md)
  - [Sum types](doc/sum-types.md)

I used Claude to generate a visual representation of this.
It is fairly accurate, but might not make much sense before you actually read the information here.
You can find it here: [TDD Concepts Overview](doc/tdd-concepts-overview.md) - Visual guide showing how all concepts relate

Reach out:
- [Discussions](https://github.com/anderssv/the-example/discussions)
- https://x.com/anderssv
- https://bsky.app/profile/anders.f12.no
- [anders@f12.no](mailto:anders@f12.no)

A lot of this content has come out of endless discussions with fellow developers.
But special thanks goes out to Asgaut Mjølne, Ola Hast, and Terje Heen for the regular discussions we have.

# The workshop

If you're looking for the workshop, you can [find it here](doc/workshop/README.md).

# Claude Code Skills

This repository includes reusable [Claude Code skills](skills/README.md) that teach Claude the patterns and practices used here:

- **kotlin-sum-types** 🔀 - Parse, don't validate with sealed classes for type-safe validation
- **kotlin-tdd** 🧪 - Test-Driven Development with fakes, object mothers, and Testing Through The Domain
- **kotlin-context-di** 🔌 - Manual dependency injection using SystemContext and TestContext patterns

Install them to teach Claude Code about the approaches in this codebase:
```bash
npx skills add anderssv/the-example/skills
```

# Using this code and building

## Prerequisites
Install [mise](https://mise.jdx.dev/getting-started.html)

Mise will make sure you have the right tools and versions. If not:
- Java 21
- Git

## Build and terminal
1. Download this repository
   ```bash
   git clone https://github.com/anderssv/the-example.git
   cd the-example
   ```
2. Install Java using mise:
   ```bash
   mise install            # This will install Java 21 as specified in .mise.toml
   ```
3. Build the project:
   ```bash
   ./gradlew build
   ```

## Development environment

It works with most editors, but I recommend using IntelliJ IDEA.
Most stuff is good with default plugins, but I also recommend:
- [Mermaid](https://plugins.jetbrains.com/plugin/20146-mermaid) plugin
- [Markdown](https://plugins.jetbrains.com/plugin/7793-markdown) plugin
- [Supermaven](https://plugins.jetbrains.com/plugin/23893-supermaven) plugin
- [GitHub Copilot](https://plugins.jetbrains.com/plugin/17718-github-copilot) plugin
