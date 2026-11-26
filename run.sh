#!/usr/bin/env bash
# Compiles and starts the application (requires JDK 17+).
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
javac --release 17 -encoding UTF-8 -d out $(find src/main -name "*.java")
java -cp out shop.Main
