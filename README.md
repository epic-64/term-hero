# ScalaScape

## Description

ScalaScape is a terminal-based game inspired by RuneScape and Melvor Idle, written in Scala 3.

https://github.com/user-attachments/assets/e119b642-e4c7-401e-abf1-cfb2496c57f9

## Running the game

### On Windows
just double-click the [jar file](#getting-your-hands-on-a-jar-file) and a Java terminal emulator opens.

### On Linux
open a terminal (e.g. GNOME terminal, or [CRT](https://github.com/Swordfish90/cool-retro-term) for awesome graphics) and run the jar file with Java.
The `--terminal` flag will force the game to run in the current terminal.
The `--fps=Int` option will set the maximum frames per second. The recommended value is 30,
but if your terminal flickers, you can try a lower value.
```
java -jar scalascape-assembly-0.0.1-SNAPSHOT.jar --terminal --fps=30
```

To run the game in the packaged Java terminal emulator instead, run the file without any arguments:
```
java -jar scalascape-assembly-0.0.1-SNAPSHOT.jar
```

### Optional: Install Cool Retro Term (CRT)
https://github.com/Swordfish90/cool-retro-term is an awesome terminal and the recommended way to experience the game.
On Ubuntu or similar you may install it as easily as `apt install cool-retro-term`,
otherwise check the repo for instructions. Unfortunately it is not available for Windows.

If you want to try my personal CRT profile, you can find it in the `src/assets` folder.

## Prerequisites

### Getting your hands on a jar file
You can get a .jar file from the Releases section on the right.

If you want to know exactly what code you'll run, [build it yourself](#building-the-game)

### Installing Java
You probably already have a compatible Java version installed. If not, https://adoptium.net/ has you covered.
Version 21, 17 or 11 should all work fine.

## Building the game

Build an uber jar with all dependencies:

```
sbt assembly
``` 

This should create the following file in the project root:
```
target/scala-3.5.0/scalascape-assembly-0.0.1-SNAPSHOT.jar
```

## Shoutouts to awesome projects
- https://www.scala-lang.org/ The Scala language
- https://discord.com/invite/scala The Scala Discord community for being patient with my questions
- https://github.com/mabe02/lanterna The Java library Lanterna which provides terminal abstractions
- https://www.jetbrains.com/idea/ IntelliJ IDEA and the Scala plugin
- https://github.com/sbt/sbt-assembly The sbt-assembly plugin which packages all dependencies into a single jar
- https://www.scalatest.org/ ScalaTest for writing slick tests
- https://chatgpt.com/ ChatGPT-4o for being a great mentor and rubber duck
- https://store.steampowered.com/app/1267910/Melvor_Idle/ Melvor Idle for inspiration

## Copyright

I did not attach a license file to this project, which means it is protected by copyright law.    
By hosting it publicly on github.com, I implicitly grant you a few rights.  
The following chart is for your information:

| Action                                                      | Permitted to you |
|-------------------------------------------------------------|------------------|
| Inspect the code                                            | Yes ✅            |
| Build and run the code                                      | Yes ✅            |
| Fork the code on github.com (with pointer to the original)  | Yes ✅            |
| ----------------------------------------------------------- |------------------|
| Commercially use the code or any fork of it                 | No ❌             |
| Re-upload the code as your own                              | No ❌             |
| Distribute the product in any form                          | No ❌             |
| Add or modify a license                                     | No ❌             |
