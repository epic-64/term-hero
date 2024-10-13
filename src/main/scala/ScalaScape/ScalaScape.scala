package ScalaScape

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.screen.{Screen, TerminalScreen}
import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, Terminal}
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.graphics.TextGraphics

import java.awt.{Font, GraphicsEnvironment}
import scala.concurrent.duration.*
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object Game {
  def main(args: Array[String]): Unit = {
    val game = new Scelverna()
    game.run()
  }
}

trait Skill {
  val name: String
  var xp: Int
  var level: Int
  var actionProgress: Double        = 0.0
  val actionDurationSeconds: Double = 5.0
  def xpForNextLevel: Int           = level * 100 // Example XP progression per level
  def progressToNextLevel: Double   = xp.toDouble / xpForNextLevel
}

case class Woodcutting() extends Skill {
  val name: String = "Woodcutting"
  var xp: Int      = 0
  var level: Int   = 1
}

case class Mining() extends Skill {
  val name: String = "Mining"
  var xp: Int      = 0
  var level: Int   = 1
}

case class Woodworking() extends Skill {
  val name: String = "Woodworking"
  var xp: Int      = 0
  var level: Int   = 1
}

case class StoneCutting() extends Skill {
  val name: String = "StoneCutting"
  var xp: Int      = 0
  var level: Int   = 1
}

object SkillDisplay:
  def renderProgressBar(
      graphics: TextGraphics,
      x: Int,
      y: Int,
      width: Int,
      progress: Double,
      color: TextColor,
      leftLimiter: String,
      rightLimiter: String
  ): Unit =
    val progressBarLength = width
    val filledLength      = (progress * (progressBarLength - 2)).toInt // Reserve space for boundaries
    val fillChar          = ':'

    // Render the left boundary in gray
    graphics.setForegroundColor(TextColor.ANSI.WHITE)
    graphics.putString(x, y, leftLimiter)

    // Render the progress bar fill material
    graphics.setForegroundColor(color)

    for (i <- 1 until 1 + filledLength)
      graphics.putString(x + i, y, fillChar.toString)

    // Render the remaining empty space in default color
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    for (i <- 1 + filledLength until progressBarLength - 1)
      graphics.putString(x + i, y, " ")

    // Render the right boundary in gray
    graphics.setForegroundColor(TextColor.ANSI.WHITE)
    graphics.putString(x + progressBarLength - 1, y, rightLimiter)

    graphics.setForegroundColor(TextColor.ANSI.DEFAULT) // Reset to default color
  end renderProgressBar

  def draw(skill: Skill, graphics: TextGraphics, position: Position): Unit =
    val x = position.x
    val y = position.y

    graphics.putString(x, y, s"${skill.name} (${skill.level} / 99)")
    graphics.putString(x, y + 1, "--------------------------------------")

    graphics.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT)
    // Render ASCII Art for Woodworking
    val woodCuttingArt = """
        |              ,@@@@@@@,
        |      ,,,.   ,@@@@@@/@@,  .oo8888o.
        |   ,&%%&%&&%,@@@@@/@@@@@@,8888\88/8o
        |  ,%&\%&&%&&%,@@@\@@@/@@@88\88888/88'
        |  %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
        |  %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
        |  `&%\ ` /%&'    |.|        \ '|8'
        |      |o|        | |         | |
        |      |.|        | |         | |
        |__ \\/ ._\//_/__/  ,\_//__\\/.  \_//__
        |""".stripMargin
    woodCuttingArt.split("\n").zipWithIndex.foreach { case (line, index) =>
      graphics.putString(x, y + 1 + index, line)
    }
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)

    var offset = 12
    graphics.putString(x, y + offset, "--------------------------------------")
    offset = 13

    // Render skill XP progress bar (Blue)
    graphics.putString(x, y + offset + 1, s"XP Progress: ${skill.xp} / ${skill.xpForNextLevel}")
    renderProgressBar(graphics, x, y + offset + 2, 40, skill.progressToNextLevel, TextColor.ANSI.BLUE_BRIGHT, "[", "]")

    // Render action progress bar (Green)
    val actionProgress   = f"${skill.actionProgress * 100}%1.0f"
    val remainingSeconds = f"${skill.actionDurationSeconds * (1 - skill.actionProgress)}%1.1f"
    graphics.putString(x, y + offset + 4, s"Action Progress: ETA: ${remainingSeconds} seconds")
    renderProgressBar(graphics, x, y + offset + 5, 40, skill.actionProgress, TextColor.ANSI.GREEN_BRIGHT, "[", "]")
  end draw
end SkillDisplay

class GameState:
  var activeSkill: Option[Skill]  = None
  var skills: Map[String, Skill]  = Map(
    "Woodcutting"  -> Woodcutting(),
    "Mining"       -> Mining(),
    "Woodworking"  -> Woodworking(),
    "StoneCutting" -> StoneCutting()
  )
  var inventory: Map[String, Int] = Map("Wood" -> 0)
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
    gatheringSkills.map(_.name) ++ manufacturingSkills.map(_.name) :+ "Inventory"

  private var selectedMenuIndex: Int = 0

  def navigate(direction: Int): Unit =
    // Cycle through the menu items, including skills and inventory
    selectedMenuIndex = (selectedMenuIndex + direction) match {
      case i if i < 0               => menuItems.size - 1
      case i if i >= menuItems.size => 0
      case i                        => i
    }

  def getSelectedItem: String = menuItems(selectedMenuIndex)

  def render(
      graphics: TextGraphics,
      activeSkill: Option[Skill],
      spinnerChars: List[String],
      spinnerIndex: Int,
      position: Position
  ): Unit =
    val x = position.x

    def drawSkillItemText(skill: Skill, isActive: Boolean, isSelected: Boolean, position: Position): Unit = {
      val x       = position.x
      val y       = position.y
      val color   = if isActive then TextColor.ANSI.YELLOW_BRIGHT else TextColor.ANSI.DEFAULT
      val spinner = if isActive then s"${spinnerChars(spinnerIndex)}" else ""

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
      then TextColor.ANSI.GREEN_BRIGHT
      else TextColor.ANSI.DEFAULT

    graphics.setForegroundColor(inventoryColor)
    val y = 9 + gatheringSkills.size + manufacturingSkills.size
    graphics.putString(x, y, s"${if (selectedMenuIndex == inventoryIndex) ">" else " "} Shop")

    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
  end render
end Menu

class Scelverna:

  private val state     = new GameState
  private val menu      = new Menu(List(Woodcutting(), Mining()), List(Woodworking(), StoneCutting()))
  private val inventory = new Inventory

  private val fps                       = 60
  private val spinnerStates             = "|/-\\".toList.asInstanceOf[List[String]]
  private var spinnerIndex: Int         = 0
  private var spinnerUpdateCounter: Int = 0

  def getFont(family: String, style: Int, size: Int): Font = {
    val availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
    if availableFonts.contains(family) then new Font(family, style, size)
    else new Font("Monospaced", style, size)
  }

  private def getTerminal: Terminal = {
    val terminalFactory = new DefaultTerminalFactory()
    val fontConfig      = SwingTerminalFontConfiguration.newInstance(getFont("Consolas", Font.PLAIN, 20))

    terminalFactory.setInitialTerminalSize(new TerminalSize(120, 30))
    terminalFactory.setTerminalEmulatorFontConfiguration(fontConfig)
    terminalFactory.createTerminal()
  }

  private val terminal: Terminal     = getTerminal
  private val screen: Screen         = new TerminalScreen(terminal)
  private val graphics: TextGraphics = screen.newTextGraphics()

  def run(): Unit =
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

    if (spinnerUpdateCounter >= 10) {
      spinnerIndex = (spinnerIndex + 1) % spinnerStates.size
      spinnerUpdateCounter = 0
    } else {
      spinnerUpdateCounter += 1
    }
  end update

  def render(graphics: TextGraphics, state: GameState): Unit =
    screen.clear()

    // Render the left section: skill menu
    menu.render(graphics, state.activeSkill, spinnerStates, spinnerIndex, Position(2, 1))

    // Render the middle section: skill info
    renderSkillUI(graphics, state, Position(25, 1))

    // Render the right section: inventory
    inventory.render(graphics, state, Position(70, 1))

    screen.refresh()
  end render

  def renderSkillUI(graphics: TextGraphics, state: GameState, position: Position): Unit =
    val x = position.x
    val y = position.y

    state.activeSkill match {
      case Some(skill: Woodcutting) => SkillDisplay.draw(skill, graphics, Position(x, y))
      case _                        => graphics.putString(x, 1, "No active skill")
    }
  end renderSkillUI

  def activateMenuItem(state: GameState): Unit = {
    val selectedMenuItem = menu.getSelectedItem
    if (state.skills.contains(selectedMenuItem)) {
      state.activeSkill = Some(state.skills(selectedMenuItem))
    }
  }

  def handleInput(keyStroke: KeyStroke, state: GameState): Unit =
    keyStroke.getKeyType match {
      case KeyType.ArrowDown                                  => menu.navigate(1)
      case KeyType.ArrowUp                                    => menu.navigate(-1)
      case KeyType.Enter                                      => activateMenuItem(state)
      case KeyType.Character if keyStroke.getCharacter == ' ' => activateMenuItem(state)
      case _                                                  => // Other keys can be handled here if necessary
    }
  end handleInput
end Scelverna
