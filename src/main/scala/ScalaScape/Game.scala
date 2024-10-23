package ScalaScape

import ScalaScape.components.*
import ScalaScape.utils.LanternaFactoryFactory
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import com.googlecode.lanterna.screen.Screen

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

@main def main(args: String*): Unit =
  val forceTerminal = args.contains("--terminal")
  val targetFps     = args.find(_.startsWith("--fps=")).map(_.split("=")(1).toInt).getOrElse(30).max(1)
  val screen        = LanternaFactoryFactory.makeScreen(forceTerminal)
  val graphics      = screen.newTextGraphics()
  val game          = new Game(screen, graphics, targetFps)
  
  game.run()
end main

class Game(private val screen: Screen, private val graphics: TextGraphics, val targetFps: Int):
  var running    = true
  val state      = new GameState(targetFps, screen)
  val fpsDisplay = new FpsDisplay(state.targetFps)
  var frameCount = 0

  def run(): Unit =
    screen.startScreen()
    screen.clear()
    state.activityLog.add("Welcome to ScalaScape!")
    state.activityLog.add("Keybinds:")
    state.activityLog.add("<UP> to navigate up")
    state.activityLog.add("<DOWN> to navigate down")
    state.activityLog.add("<ENTER> to enter")
    state.activityLog.add("<ESC> to return")

    // state.swapScene(state.scenes.oak)
    // simulateOfflineProgress(days = 1)

    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
    gameLoop // async
    inputLoop // async
  end run

  private def gameLoop(implicit executor: ExecutionContext): Unit =
    Future {
      val targetFrameDuration: Milliseconds = (1_000 / state.targetFps).toLong
      while (running) {
        val startTime = System.nanoTime()

        try {
          frameCount += 1
          update(state)
          draw(render(state), graphics)
        } catch {
          case e: Exception =>
            running = false
            e.printStackTrace()
        }

        val endTime                       = System.nanoTime()
        val actualFrameTime: Milliseconds = (endTime - startTime) / 1_000_000
        fpsDisplay.update(actualFrameTime)

        // Enforce target frame rate by sleeping for the remaining time
        val sleepTime = targetFrameDuration - actualFrameTime
        if (sleepTime > 0) {
          Thread.sleep(sleepTime)
        }
      }
    }
  end gameLoop

  private def inputLoop(implicit executor: ExecutionContext): Unit =
    Future {
      while (running) {
        val keyStroke: KeyStroke = screen.readInput()
        if keyStroke == KeyType.EOF then running = false
        else state.getScene.handleInput(keyStroke, state)
      }
    }
  end inputLoop

  def update(state: GameState): GameState = state.getScene.update(state)

  def render(state: GameState): RenderBlock = {
    state.activityLog.render(Pos(2, 1))
    ++ state.getScene.render(state, Pos(35, 1))
    ++ state.inventory.render(Pos(80, 1))
    ++ fpsDisplay.render(Pos(100, 1))
  }

  def draw(block: RenderBlock, graphics: TextGraphics): Unit =
    if frameCount % state.targetFps * 5 == 0 then screen.clear() // causes 500% more frame time

    block.draw(graphics) // block contains the ENTIRE screen state

    screen.setCursorPosition(null) // hide cursor
    screen.refresh() // draw the diff to the screen
  end draw

  def simulateOfflineProgress(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0): Unit =
    val minute = 60
    val hour   = minute * 60
    val day    = hour * 24

    val ticksPerSecond     = state.targetFps
    val elapsedTimeSeconds = days * day + hours * hour + minutes * minute + seconds
    val ticksToRun         = ticksPerSecond * elapsedTimeSeconds

    for _ <- 1 to ticksToRun do update(state)
  end simulateOfflineProgress
end Game
