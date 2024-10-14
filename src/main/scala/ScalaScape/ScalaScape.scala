package ScalaScape

import ScalaScape.game.skills.*
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import com.googlecode.lanterna.screen.{Screen, TerminalScreen}
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, Terminal}
import com.googlecode.lanterna.{TerminalSize, TextColor}

import java.awt.{Font, GraphicsEnvironment}
import java.util.concurrent.Executors
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

@main def main(args: String*): Unit = {
  val forceTerminal = args.contains("--terminal")
  val game     = new ScalaScape(forceTerminal)
  game.run()
}

type Between0And1 = Double

case class TerminalString(content: String, position: Position, color: TextColor):
  def nonEmpty: Boolean = content.nonEmpty

case class TerminalParagraph(list: List[TerminalString]):
  def render(graphics: TextGraphics): Unit =
    list.foreach { terminalString =>
      graphics.setForegroundColor(terminalString.color)
      graphics.putString(terminalString.position.x, terminalString.position.y, terminalString.content)
      graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    }
  end render
end TerminalParagraph

case class ProgressBarParameters(
    width: Int, // width in columns
    progress: Between0And1,
    position: Position,
    color: TextColor,
    leftLimiter: String = "[",
    rightLimiter: String = "]"
)

object make:
  def ProgressBar(p: ProgressBarParameters): List[TerminalString] = {
    val x             = p.position.x
    val y             = p.position.y
    val innerLength   = p.width - 2 // Reserve space for boundaries
    val filledLength  = (p.progress * innerLength).toInt
    val fillChar      = ':'
    val filledSection = (1 to filledLength).map(_ => fillChar).mkString
    val emptySection  = (1 to (p.width - innerLength)).map(_ => " ").mkString

    List(
      TerminalString(p.leftLimiter, Position(x, y), WHITE),
      TerminalString(filledSection, Position(x + 1, y), p.color),
      TerminalString(emptySection, Position(x + 1 + filledLength, y), TextColor.ANSI.DEFAULT),
      TerminalString(p.rightLimiter, Position(x + p.width - 1, y), WHITE)
    )
  }

class SkillDisplay:
  def render(graphics: TextGraphics, state: GameState, position: Position): Unit =
    state.activeSkill match {
      case Some(skill: Woodcutting) => draw(skill, graphics, position)
      case Some(skill: Mining)      => draw(skill, graphics, position)
      case _                        => graphics.putString(position.x, 1, "No active skill")
    }
  end render

  private def draw(skill: Skill, graphics: TextGraphics, position: Position): Unit =
    val x  = position.x
    val y  = position.y
    val pb = ProgressBarParameters

    val strings = List(
      TerminalString(s"${skill.name} (${skill.level} / 99)", Position(x, y), WHITE),
      TerminalString("--------------------------------------", Position(x, y + 1), WHITE)
    )
      ++ skill.getAsciiArt(Position(x, y + 2))
      ++ List(
        TerminalString("--------------------------------------", Position(x, y + 12), WHITE),
        TerminalString(s"XP Progress: ${skill.xp} / ${skill.xpForNextLevel}", Position(x, y + 13), WHITE)
      )
      ++ make.ProgressBar(pb(40, skill.progressToNextLevel, Position(x, y + 14), BLUE_BRIGHT))
      ++ List(
        TerminalString(s"Action Progress: ETA: ", Position(x, y + 16), WHITE),
        TerminalString(f"${skill.remainingDuration}%1.1f", Position(x + 22, y + 16), CYAN_BRIGHT),
        TerminalString(" seconds", Position(x + 26, y + 16), WHITE)
      )
      ++ make.ProgressBar(pb(40, skill.actionProgress, Position(x, y + 17), GREEN_BRIGHT))

    TerminalParagraph(strings).render(graphics)
  end draw
end SkillDisplay

class GameState:
  var inventory: Map[String, Int] = Map("Wood" -> 0)
  var activeSkill: Option[Skill]  = None
  var skills: Map[String, Skill]  = Map(
    "Woodcutting"  -> Woodcutting(),
    "Mining"       -> Mining(),
    "Woodworking"  -> Woodworking(),
    "StoneCutting" -> StoneCutting()
  )
end GameState

case class Position(x: Int, y: Int)

class Inventory():
  def render(graphics: TextGraphics, state: GameState, position: Position): Unit =
    graphics.putString(position.x, 1, "Inventory")
    graphics.putString(position.x, 2, "---------")

    state.inventory.zipWithIndex.foreach { case ((item, count), index) =>
      graphics.putString(position.x, 3 + index, s"$item: $count")
    }
  end render
end Inventory

class Menu(val gatheringSkills: List[Skill], val manufacturingSkills: List[Skill]):
  private val menuItems: List[String] =
    gatheringSkills.map(_.name)
      ++ manufacturingSkills.map(_.name)
      :+ "Inventory"

  private val spinnerStates             = "|/-\\".toList.asInstanceOf[List[String]]
  private var spinnerIndex: Int         = 0
  private var spinnerUpdateCounter: Int = 0
  private var selectedMenuIndex: Int    = 0

  def navigate(direction: Int): Unit =
    // Cycle through the menu items, including skills and inventory
    selectedMenuIndex = selectedMenuIndex + direction match {
      case i if i < 0               => menuItems.size - 1
      case i if i >= menuItems.size => 0
      case i                        => i
    }

  def getSelectedItem: String = menuItems(selectedMenuIndex)

  def update(): Unit =
    if (spinnerUpdateCounter >= 10) {
      spinnerIndex = (spinnerIndex + 1) % spinnerStates.size
      spinnerUpdateCounter = 0
    } else {
      spinnerUpdateCounter += 1
    }

  def render(
      graphics: TextGraphics,
      activeSkill: Option[Skill],
      position: Position
  ): Unit =
    val x = position.x

    def drawSkillItemText(skill: Skill, isActive: Boolean, isSelected: Boolean, position: Position): Unit = {
      val x       = position.x
      val y       = position.y
      val color   = if isActive then TextColor.ANSI.WHITE_BRIGHT else TextColor.ANSI.WHITE
      val spinner = if isActive then s"${spinnerStates(spinnerIndex)}" else ""

      graphics.setForegroundColor(color)
      graphics.putString(x, y, s"${if isSelected then ">" else " "} ${skill.name} $spinner")
      graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    }

    // Render gathering skill menu
    graphics.putString(x, 1, "Gathering")
    graphics.putString(x, 2, "---------")
    gatheringSkills.zipWithIndex.foreach { case (skill, index) =>
      drawSkillItemText(skill, activeSkill.contains(skill), selectedMenuIndex == index, Position(x, 3 + index))
    }

    // Render manufacturing skill menu
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    graphics.putString(x, 4 + gatheringSkills.size, "Manufacturing")
    graphics.putString(x, 5 + gatheringSkills.size, "-------------")
    manufacturingSkills.zipWithIndex.foreach { case (skill, index) =>
      val isSelected = selectedMenuIndex == gatheringSkills.size + index
      val position   = Position(x, 6 + gatheringSkills.size + index)
      drawSkillItemText(skill, activeSkill.contains(skill), isSelected, position)
    }

    // Render Management menu
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    graphics.putString(x, 7 + gatheringSkills.size + manufacturingSkills.size, "Management")
    graphics.putString(x, 8 + gatheringSkills.size + manufacturingSkills.size, "----------")
    val inventoryIndex = gatheringSkills.size + manufacturingSkills.size
    val inventoryColor =
      if activeSkill.isEmpty && getSelectedItem == "Shop"
      then GREEN_BRIGHT
      else TextColor.ANSI.DEFAULT

    graphics.setForegroundColor(inventoryColor)
    val y = 9 + gatheringSkills.size + manufacturingSkills.size
    graphics.putString(x, y, s"${if (selectedMenuIndex == inventoryIndex) ">" else " "} Shop")

    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
  end render
end Menu

class ScalaScape(forceTerminal: Boolean):
  private val state                  = new GameState
  private val menu                   = new Menu(List(Woodcutting(), Mining()), List(Woodworking(), StoneCutting()))
  private val inventory              = new Inventory
  private val skillDisplay           = new SkillDisplay
  private val terminal: Terminal     = makeTerminal(forceTerminal)
  private val screen: Screen         = new TerminalScreen(terminal)
  private val graphics: TextGraphics = screen.newTextGraphics()
  private val fps                    = 60

  private def makeTerminal(forceTerminal: Boolean): Terminal = {
    def getFont(family: String, style: Int, size: Int): Font = {
      val availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
      if availableFonts.contains(family) then new Font(family, style, size)
      else new Font("Monospaced", style, size)
    }

    val terminalFactory = new DefaultTerminalFactory()
    val fontConfig      = SwingTerminalFontConfiguration.newInstance(getFont("Consolas", Font.PLAIN, 20))
    terminalFactory.setInitialTerminalSize(new TerminalSize(120, 30))
    terminalFactory.setTerminalEmulatorFontConfiguration(fontConfig)
    terminalFactory.setPreferTerminalEmulator(!forceTerminal)
    terminalFactory.setForceTextTerminal(forceTerminal)
    terminalFactory.createTerminal()
  }

  def run(): Unit =
    // terminal.enterPrivateMode()
    screen.startScreen()
    screen.clear()

    // Create an ExecutionContext for the game loop
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

    // Start the game loop
    Future {
      val frameDuration = (1000 / fps).millis
      while (true) {
        update(state)
        render(graphics, state)
        screen.refresh()
        Thread.sleep(frameDuration.toMillis)
      }
    }

    // Handle input for skill selection and activation
    Future {
      while (true) {
        val keyStroke: KeyStroke = screen.readInput()
        handleInput(keyStroke, state)
      }
    }
  end run

  def update(state: GameState): Unit =
    state.activeSkill match {
      case Some(skill: Woodcutting) =>
        if (skill.actionProgress >= 1.0) {
          skill.xp += 10
          skill.actionProgress = 0.0
          state.inventory = state.inventory.updated("Wood", state.inventory("Wood") + 1)
          if (skill.xp >= skill.xpForNextLevel) {
            skill.level += 1
            skill.xp = 0
          }
        } else {
          skill.actionProgress += 1.0 / (skill.actionDurationSeconds * fps)
        }
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
    inventory.render(graphics, state, Position(70, 1))

    screen.setCursorPosition(null)
    screen.refresh()
  end render

  private def activateMenuItem(state: GameState): Unit = {
    val selectedMenuItem = menu.getSelectedItem
    if (state.skills.contains(selectedMenuItem)) {
      state.activeSkill = Some(state.skills(selectedMenuItem))
    }
  }

  private def handleInput(keyStroke: KeyStroke, state: GameState): Unit =
    keyStroke.getKeyType match {
      case KeyType.ArrowDown                                  => menu.navigate(1)
      case KeyType.ArrowUp                                    => menu.navigate(-1)
      case KeyType.Enter                                      => activateMenuItem(state)
      case KeyType.Character if keyStroke.getCharacter == ' ' => activateMenuItem(state)
      case _                                                  => // Other keys can be handled here if necessary
    }
  end handleInput
end ScalaScape
