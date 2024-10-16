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

class InventoryDisplay:
  def render(state: GameState, position: Position): TerminalParagraph =
    TerminalParagraph(
      List(
        TerminalString("Inventory", Position(position.x, position.y), WHITE),
        TerminalString("---------", Position(position.x, position.y + 1), WHITE)
      )
        ++ state.inventory.zipWithIndex.map { case ((item, count), index) =>
          TerminalString(s"$item: $count", Position(position.x, position.y + 2 + index), WHITE)
        }
    )
  end render
end InventoryDisplay

class ScalaScape(forceTerminal: Boolean):
  private var running                = true
  private val state                  = new GameState
  private val menu                   = new Menu(List(Woodcutting(), Quarrying()), List(Woodworking(), Stonecutting()))
  private val inventoryDisplay       = new InventoryDisplay
  private val skillDisplay           = new SkillDisplay
  private val terminal: Terminal     = (new LanternBimbo).makeTerminal(forceTerminal)
  private val screen: Screen         = new TerminalScreen(terminal)
  private val graphics: TextGraphics = screen.newTextGraphics()
  private val targetFps              = 30
  private val fpsDisplay             = new FpsDisplay(targetFps)

  def run(): GameState =
    screen.startScreen()
    screen.clear()

    // Create an ExecutionContext for the game loop
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

    // Start the game loop
    Future {
      val targetFrameDuration: Milliseconds = (1_000 / targetFps).toLong
      while (running) {
        val startTime = System.nanoTime()

        update(state)
        render(graphics, state)
        screen.refresh()

        val endTime                       = System.nanoTime()
        val actualFrameTime: Milliseconds = (endTime - startTime) / 1_000_000

        // Update FPS counter
        fpsDisplay.update(actualFrameTime)

        // Enforce target frame rate by sleeping for the remaining time
        val sleepTime = targetFrameDuration - actualFrameTime
        if (sleepTime > 0) {
          Thread.sleep(sleepTime)
        }
      }
    }

    // Handle input for skill selection and activation
    Future {
      while (running) {
        val keyStroke: KeyStroke = screen.readInput()
        if keyStroke == KeyType.EOF then running = false
        else handleInput(keyStroke, state)
      }
    }

    state
  end run

  def update(state: GameState): GameState =
    state.activeSkill match {
      case Some(skill: Woodcutting) => skill.update(state, targetFps)
      case Some(skill: Quarrying)   => skill.update(state, targetFps)
      case _                        => // Do nothing
    }

    menu.update()

    state
  end update

  private def render(graphics: TextGraphics, state: GameState): GameState =
    screen.clear()

    menu.render(state.activeSkill, Position(2, 1)).draw(graphics)
    skillDisplay.draw(graphics, state, Position(25, 1))
    inventoryDisplay.render(state, Position(70, 1)).draw(graphics)
    fpsDisplay.render(Position(100, 1)).draw(graphics)

    screen.setCursorPosition(null) // hide cursor
    screen.refresh()               // draw the diff to the screen

    state
  end render

  private def handleInput(keyStroke: KeyStroke, state: GameState): GameState =
    keyStroke.getKeyType match {
      case KeyType.ArrowDown                                  => menu.navigate(1)
      case KeyType.ArrowUp                                    => menu.navigate(-1)
      case KeyType.Enter                                      => menu.activateItem(state)
      case KeyType.Character if keyStroke.getCharacter == ' ' => menu.activateItem(state)
      case _                                                  => // Other keys can be handled here if necessary
    }

    state
  end handleInput
end ScalaScape
