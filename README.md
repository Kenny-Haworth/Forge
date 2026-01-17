# Forge 🛠️

The **Forge** is a lightweight collection of reusable Java components and utility methods, similar in spirit to Apache Commons.
The Forge is intended to serve as a customizable, minimal library for personal projects.

Utilities include:
 - File utilities
 - Web utilities
 - Enhanced Swing components
 - Automation helpers
 - A logger

## Why the Forge?

The Forge implements utilities from scratch to:
 - Allow for greater customization of utility functions as projects require
 - Reduce dependence on external libraries
 - Improve my own understanding and experience with Java

Many of my personal projects use the Forge as a submodule. Having a centralized place for shared utilities helps:
 - Reduce boilerplate code and code duplication
 - Encourage code reuse and consistency
 - Speed up development on new projects and ideas

Some Forge functions are specific to Windows, but most will work on any OS.

## How to Use

`git submodule add https://github.com/Kenny-Haworth/Forge.git lib/forge`

The Forge can be packaged into Forge.jar by running [build.sh](build.sh). This script has only been tested on Windows using Git bash, but it should work on Linux too.

The jar can then be linked in to your own project.

## Third Party Software

This project includes jars within the lib directory from the following third-party software:

- JNA
  - https://github.com/java-native-access/jna
  - License: Apache License 2.0

- jsoup
  - https://jsoup.org/
  - License: MIT License

Please see [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md) for third-party licenses.
