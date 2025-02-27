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

Reach out:
- [Discussions](https://github.com/anderssv/the-example/discussions)
- https://x.com/anderssv
- https://bsky.app/profile/anders.f12.no
- [anders@f12.no](mailto:anders@f12.no)

A lot of this content has come out of endless discussions with fellow developers.
But special thanks goes out to Asgaut Mjølne, Ola Hast, and Terje Heen for the regular discussions we have.

# The workshop

If you're looking for the workshop, you can [find it here](doc/workshop/README.md).

# Using this code and building

## Prerequisites
Install [asdf-vm.com](https://asdf-vm.com/guide/getting-started.html)

ASDF will make sure you have the right tools and versions. If not:
- Java 21
- Git

## Build and terminal
1. Download this repository
   ```bash
   git clone https://github.com/anderssv/the-example.git
   cd the-example
   ```
2. Install Java using ASDF:
   ```bash
   asdf plugin add java    # Only needed if you haven't installed the Java plugin before
   asdf install           # This will install Java 21 as specified in .tool-versions
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
