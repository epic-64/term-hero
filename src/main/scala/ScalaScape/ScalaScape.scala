package ScalaScape

import ScalaScape.components.*
import ScalaScape.ui.lantern.*
import ScalaScape.utils.LanternBimbo
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import com.googlecode.lanterna.screen.{Screen, TerminalScreen}
import com.googlecode.lanterna.terminal.Terminal

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

@main def main(args: String*): Unit = {
  val forceTerminal = args.contains("--terminal")
  val game          = new ScalaScape(forceTerminal)
  game.run()
}

type Between0And1   = Double
type WidthInColumns = Int
type Milliseconds   = Long

class SkillDisplay:
  def render(graphics: TextGraphics, state: GameState, position: Position): Unit =
    state.activeSkill match {
      case Some(skill: Woodcutting) => draw(skill, graphics, position)
      case Some(skill: Quarrying)   => draw(skill, graphics, position)
      case _                        => graphics.putString(position.x, 1, "No active skill")
    }
  end render

  private def draw(skill: Skill, graphics: TextGraphics, position: Position): Unit =
    val x  = position.x
    val y  = position.y
    val pb = ProgressBarParameters

    def headerStart: List[TerminalString] = List(
      TerminalString(s"${skill.name} (${skill.level} / 99)", Position(x, y), WHITE),
      TerminalString("--------------------------------------", Position(x, y + 1), WHITE)
    )

    def asciiArt: List[TerminalString] = skill.getAsciiArt(Position(x, y + 2))

    def headerEnd: List[TerminalString] = List(
      TerminalString("--------------------------------------", Position(x, y + 12), WHITE),
      TerminalString(s"XP Progress: ${skill.xp} / ${skill.xpForNextLevel}", Position(x, y + 13), WHITE)
    )

    def xpBar: List[TerminalString] =
      List(TerminalString(s"XP Progress: ${skill.xp} / ${skill.xpForNextLevel}", Position(x, y + 13), WHITE))
        ++ ProgressBar.from(pb(40, skill.progressToNextLevel, Position(x, y + 14), BLUE_BRIGHT))

    def actionBar: List[TerminalString] =
      List(
        TerminalString(s"Action Progress: ETA: ", Position(x, y + 16), WHITE),
        TerminalString(f"${skill.remainingDuration}%1.1f", Position(x + 22, y + 16), CYAN_BRIGHT),
        TerminalString(" seconds", Position(x + 26, y + 16), WHITE)
      )
        ++ ProgressBar.from(pb(40, skill.actionProgress, Position(x, y + 17), GREEN_BRIGHT))

    val strings = headerStart ++ asciiArt ++ headerEnd ++ xpBar ++ actionBar

    TerminalParagraph(strings).render(graphics)
  end draw
end SkillDisplay

class InventoryDisplay:
  def render(graphics: TextGraphics, state: GameState, position: Position): Unit =
    graphics.putString(position.x, 1, "Inventory")
    graphics.putString(position.x, 2, "---------")

    state.inventory.zipWithIndex.foreach { case ((item, count), index) =>
      graphics.putString(position.x, 3 + index, s"$item: $count")
    }
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

  // fps related
  private val targetFps                    = 60
  private var currentFps: Double           = targetFps.toDouble
  private val fpsUpdateIntervalMs          = 100
  private var timeSinceLastFpsUpdate: Long = 0

  def run(): Unit =
    screen.startScreen()
    screen.clear()

    // Create an ExecutionContext for the game loop
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

    // Start the game loop
    Future {
      val targetFrameDurationNanos = (1_000_000_000 / targetFps).toLong
      while (running) {
        val startTime = System.nanoTime()

        update(state)
        render(graphics, state)
        screen.refresh()

        val endTime              = System.nanoTime()
        val actualFrameTimeNanos = endTime - startTime // Actual frame time in nanoseconds

        // Calculate FPS based on actual frame time (converted to milliseconds)
        timeSinceLastFpsUpdate += actualFrameTimeNanos
        if (timeSinceLastFpsUpdate >= 1_000_000_000) {        // 1 second in nanoseconds
          currentFps = 1_000_000_000.0 / actualFrameTimeNanos // FPS = 1 second / frame time
          timeSinceLastFpsUpdate = 0
        }

        // Enforce target frame rate by sleeping for the remaining time
        val sleepTime = targetFrameDurationNanos - actualFrameTimeNanos
        if (sleepTime > 0) {
          Thread.sleep(sleepTime / 1_000_000, (sleepTime % 1_000_000).toInt)
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
  end run

  def update(state: GameState): Unit =
    state.activeSkill match {
      case Some(skill: Woodcutting) => skill.update(state, targetFps)
      case Some(skill: Quarrying)   => skill.update(state, targetFps)
      case _                        => // Do nothing
    }

    menu.update()
  end update

  private def render(graphics: TextGraphics, state: GameState): Unit =
    screen.clear()

    // Render the left section: skill menu
    menu.render(graphics, state.activeSkill, Position(2, 1))

    // Render the middle section: skill info
    skillDisplay.render(graphics, state, Position(25, 1))

    // Render the right section: inventory
    inventoryDisplay.render(graphics, state, Position(70, 1))

    // Render FPS counter in the top-right corner
    graphics.setForegroundColor(TextColor.ANSI.YELLOW)
    graphics.putString(110, 1, f"FPS: $currentFps%.1f")
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)

    screen.setCursorPosition(null)
    screen.refresh()
  end render

  private def handleInput(keyStroke: KeyStroke, state: GameState): Unit =
    keyStroke.getKeyType match {
      case KeyType.ArrowDown                                  => menu.navigate(1)
      case KeyType.ArrowUp                                    => menu.navigate(-1)
      case KeyType.Enter                                      => menu.activateItem(state)
      case KeyType.Character if keyStroke.getCharacter == ' ' => menu.activateItem(state)
      case _                                                  => // Other keys can be handled here if necessary
    }
  end handleInput
end ScalaScape
