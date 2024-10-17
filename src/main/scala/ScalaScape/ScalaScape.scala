package ScalaScape

import ScalaScape.components.*
import ScalaScape.utils.LanternBimbo
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
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
  private val state                  = new GameState
  private val inventoryDisplay       = new InventoryDisplay
  private val fpsDisplay             = new FpsDisplay(state.targetFps)
  
  def run(): GameState =
    screen.startScreen()
    screen.clear()
    
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
    gameLoop
    inputLoop

    state
  end run

  private def gameLoop(implicit executor: ExecutionContext): Unit =
    Future {
      val targetFrameDuration: Milliseconds = (1_000 / state.targetFps).toLong
      while (running) {
        val startTime = System.nanoTime()

        update(state)
        draw(graphics)
        screen.refresh()

        val endTime = System.nanoTime()
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
        else state.selectedScene.handleInput(keyStroke, state)
      }
    }
  end inputLoop

  private def update(state: GameState): GameState = state.selectedScene.update(state)

  private def draw(graphics: TextGraphics): GameState =
    screen.clear()

    state.selectedScene.render(state, Pos(2, 1)).draw(graphics)
    inventoryDisplay.render(state, Pos(50, 1)).draw(graphics)
    fpsDisplay.render(Pos(70, 1)).draw(graphics)

    screen.setCursorPosition(null) // hide cursor
    screen.refresh()               // draw the diff to the screen

    state
  end draw
end ScalaScape
