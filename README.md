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

## Notes
- Nothing surprises me anymore, and apparently it is NOT common sense for applications like this to
STOP when the window is closed. I went deeper than I wished into the rabbit hole of Swing and I *think*
I made the game NOT run like a sussy bitcoin miner long after you closed the window.
However, I can't guarantee that it won't still happen. In doubt, check your task manager for unnecessary Java processes.
- The CPU and GPU usage is currently ridiculous for this type of game. I'm trying to fix it as much as possible,
without compromising on the game's live rendering loop.
- I'm new to Scala, and even in the current simple state of the game there are memory leaks.
(RAM usage grows around 1MB per minute). I might end up ditching immutability because of this, but it would be a shame.