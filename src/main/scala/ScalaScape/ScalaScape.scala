package ScalaScape

import ScalaScape.components.*
import ScalaScape.utils.LanternBimbo
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import com.googlecode.lanterna.screen.{Screen, TerminalScreen}
import com.googlecode.lanterna.terminal.Terminal

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

@main def main(args: String*): Unit =
  val forceTerminal = args.contains("--terminal")
  val game          = new ScalaScape(forceTerminal)
  game.run()
end main

class ScalaScape(forceTerminal: Boolean):
  private var running                = true
  private val terminal: Terminal     = LanternBimbo.makeTerminal(forceTerminal)
  private val screen: Screen         = new TerminalScreen(terminal)
  private val graphics: TextGraphics = screen.newTextGraphics()
  val state                          = new GameState
  private val fpsDisplay             = new FpsDisplay(state.targetFps)

  def run(): Unit =
    screen.startScreen()
    screen.clear()
    state.activityLog.add("Welcome to ScalaScape!")
    state.activityLog.add("Keybinds:")
    state.activityLog.add("<UP> to navigate up")
    state.activityLog.add("<DOWN> to navigate down")
    state.activityLog.add("<ENTER> to enter")
    state.activityLog.add("<ESC> to return")

    // state.swapScene(WoodCuttingOakScene())
    // simulateOfflineProgress(days = 1)

    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
    gameLoop // async
    inputLoop // async
  end run

  def simulateOfflineProgress(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0): Unit =
    val minute = 60
    val hour   = minute * 60
    val day    = hour * 24

    val ticksPerSecond     = state.targetFps
    val elapsedTimeSeconds = days * day + hours * hour + minutes * minute + seconds
    val ticksToRun         = ticksPerSecond * elapsedTimeSeconds

    for _ <- 1 to ticksToRun do update(state)
  end simulateOfflineProgress

  private def gameLoop(implicit executor: ExecutionContext): Unit =
    Future {
      val targetFrameDuration: Milliseconds = (1_000 / state.targetFps).toLong
      while (running) {
        val startTime = System.nanoTime()

        try {
          update(state)
          draw(graphics)
        } catch {
          case e: Exception =>
            running = false
            e.printStackTrace()
        }
        screen.refresh()

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

  def draw(graphics: TextGraphics): Unit =
    screen.clear()

    state.activityLog.render(Pos(2, 1)).draw(graphics)
    state.getScene.render(state, Pos(35, 1)).draw(graphics)
    state.inventory.render(Pos(80, 1)).draw(graphics)
    fpsDisplay.render(Pos(100, 1)).draw(graphics)

    screen.setCursorPosition(null) // hide cursor
    screen.refresh() // draw the diff to the screen
  end draw
end ScalaScape
