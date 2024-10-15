# ScalaScape

## Description
ScalaScape is a terminal-based game inspired by RuneScape and Melvor Idle, written in Scala 3.

## Build and run

Build an uber jar with all dependencies:
```
sbt assembly
``` 

Run the uber jar:
```
java -jar target/scala-3.5.0/scalascape-assembly-0.1.0-SNAPSHOT.jar
```

By default, the game will always prefer to create a terminal emulator.
You can force it to run in the existing terminal by adding a flag:
```
java -jar target/scala-3.5.0/scalascape-assembly-0.1.0-SNAPSHOT.jar --terminal
```