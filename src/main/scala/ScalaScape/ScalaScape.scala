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
  def xpForNextLevel: Int         = level * 100 // Example XP progression per level
  def progressToNextLevel: Double = xp.toDouble / xpForNextLevel
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

class GameState:
  var activeSkill: Option[Skill]  = None
  var inventory: Map[String, Int] = Map("Wood" -> 0)
end GameState

class Inventory:
  def render(graphics: TextGraphics, state: GameState): Unit =
    graphics.putString(30, 1, "Inventory")
    graphics.putString(30, 2, "---------")

    state.inventory.zipWithIndex.foreach { case ((item, count), index) =>
      graphics.putString(30, 3 + index, s"$item: $count")
    }
  end render
end Inventory

class Menu(val gatheringSkills: List[Skill], val manufacturingSkills: List[Skill]):
  private val menuItems: List[String] =
    gatheringSkills.map(_.name) ++ manufacturingSkills.map(_.name) :+ "Inventory"

  private var selectedMenuIndex: Int = 0

  def navigateMenu(direction: Int): Unit =
    // Cycle through the menu items, including skills and inventory
    selectedMenuIndex = (selectedMenuIndex + direction) match {
      case i if i < 0               => menuItems.size - 1
      case i if i >= menuItems.size => 0
      case i                        => i
    }

  def getSelectedMenuItem: String = menuItems(selectedMenuIndex)

  def render(
      graphics: TextGraphics,
      activeSkill: Option[Skill],
      spinnerChars: List[String],
      spinnerIndex: Int
  ): Unit = {
    // Render gathering skill menu
    graphics.putString(2, 1, "Gathering")
    graphics.putString(2, 2, "---------")
    gatheringSkills.zipWithIndex.foreach { case (skill, index) =>
      val color   = if (activeSkill.contains(skill)) TextColor.ANSI.GREEN_BRIGHT else TextColor.ANSI.DEFAULT
      val spinner = if (activeSkill.contains(skill)) s" ${spinnerChars(spinnerIndex)}" else ""
      graphics.setForegroundColor(color)
      graphics.putString(2, 3 + index, s" ${if (selectedMenuIndex == index) ">" else " "} ${skill.name}$spinner")
    }

    // Render manufacturing skill menu
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    graphics.putString(2, 4 + gatheringSkills.size, "Manufacturing")
    graphics.putString(2, 5 + gatheringSkills.size, "-------------")
    manufacturingSkills.zipWithIndex.foreach { case (skill, index) =>
      val color   = if (activeSkill.contains(skill)) TextColor.ANSI.GREEN_BRIGHT else TextColor.ANSI.DEFAULT
      val spinner = if (activeSkill.contains(skill)) s" ${spinnerChars(spinnerIndex)}" else ""
      graphics.setForegroundColor(color)
      graphics.putString(
        2,
        6 + gatheringSkills.size + index,
        s" ${if (selectedMenuIndex == gatheringSkills.size + index) ">" else " "} ${skill.name}$spinner"
      )
    }

    // Render Management menu
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    graphics.putString(2, 7 + gatheringSkills.size + manufacturingSkills.size, "Management")
    graphics.putString(2, 8 + gatheringSkills.size + manufacturingSkills.size, "----------")
    val inventoryIndex = gatheringSkills.size + manufacturingSkills.size
    val inventoryColor =
      if activeSkill.isEmpty && getSelectedMenuItem == "Inventory"
      then TextColor.ANSI.GREEN_BRIGHT
      else TextColor.ANSI.DEFAULT

    graphics.setForegroundColor(inventoryColor)
    graphics.putString(
      2,
      9 + gatheringSkills.size + manufacturingSkills.size,
      s" ${if (selectedMenuIndex == inventoryIndex) ">" else " "} Inventory"
    )

    graphics.setForegroundColor(TextColor.ANSI.DEFAULT) // Reset color to default
  }
end Menu

class Scelverna:
  private val state     = new GameState
  private val menu      = new Menu(List(Woodcutting(), Mining()), List(Woodworking(), StoneCutting()))
  private val inventory = new Inventory

  private val spinnerStates             = "|/-\\".toList.asInstanceOf[List[String]]
  private var spinnerIndex: Int         = 0
  private var spinnerUpdateCounter: Int = 0
  private var actionProgress: Double    = 0.0
  private val actionDurationSeconds     = 5.0

  def getFont(family: String, style: Int, size: Int): Font = {
    val availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
    if availableFonts.contains(family) then new Font(family, style, size)
    else new Font("Monospaced", style, size)
  }

  private def getTerminal: Terminal = {
    val terminalFactory = new DefaultTerminalFactory()
    val fontConfig      = SwingTerminalFontConfiguration.newInstance(getFont("Consolas", Font.PLAIN, 20))

    terminalFactory.setInitialTerminalSize(new TerminalSize(100, 24))
    terminalFactory.setTerminalEmulatorFontConfiguration(fontConfig)

    terminalFactory.createTerminal()
  }

  val terminal: Terminal     = getTerminal
  val screen: Screen         = new TerminalScreen(terminal)
  val graphics: TextGraphics = screen.newTextGraphics()

  def run(): Unit =
    screen.startScreen()
    screen.clear()

    // Create an ExecutionContext for the game loop
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

    // Start the game loop at 60 FPS
    Future {
      val frameDuration = (1000 / 60).millis
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
        if (actionProgress >= 1.0) {
          skill.xp += 10
          actionProgress = 0.0
          state.inventory = state.inventory.updated("Wood", state.inventory("Wood") + 1)
          if (skill.xp >= skill.xpForNextLevel) {
            skill.level += 1
            skill.xp = 0
          }
        } else {
          actionProgress += 1.0 / (actionDurationSeconds * 60)
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
    // Render the menu using the Menu class
    menu.render(graphics, state.activeSkill, spinnerStates, spinnerIndex)

    // Render the appropriate screen based on the current menu selection
    if menu.getSelectedMenuItem == "Inventory"
    then inventory.render(graphics, state)
    else renderSkillUI(graphics, state)

    screen.refresh()
  end render

  def renderSkillUI(graphics: TextGraphics, state: GameState): Unit =
    state.activeSkill match {
      case Some(skill: Woodcutting) =>
        graphics.putString(30, 1, s"${skill.name} Level: ${skill.level}")
        graphics.putString(30, 2, s"XP: ${skill.xp} / ${skill.xpForNextLevel}")

        // Render skill XP progress bar (Blue)
        graphics.putString(30, 4, "XP Progress:")
        renderProgressBar(graphics, 30, 5, skill.progressToNextLevel, TextColor.ANSI.BLUE_BRIGHT)

        // Render action progress bar (Green)
        graphics.putString(30, 7, "Action Progress:")
        renderProgressBar(graphics, 30, 8, actionProgress, TextColor.ANSI.GREEN_BRIGHT)

      case _ => graphics.putString(30, 1, "No active skill")
    }
  end renderSkillUI

  def renderProgressBar(graphics: TextGraphics, x: Int, y: Int, progress: Double, color: TextColor): Unit =
    val progressBarLength = 40
    val filledLength      = (progress * (progressBarLength - 2)).toInt // Reserve space for boundaries

    // Render the left boundary in gray
    graphics.setForegroundColor(TextColor.ANSI.WHITE)
    graphics.putString(x, y, "[")

    // Render the progress bar fill material
    graphics.setForegroundColor(color)
    val fillChar = '■' // Use a solid block character for optimal fill
    for (i <- 1 until 1 + filledLength)
      graphics.putString(x + i, y, fillChar.toString)

    // Render the remaining empty space in default color
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    for (i <- 1 + filledLength until progressBarLength - 1)
      graphics.putString(x + i, y, " ")

    // Render the right boundary in gray
    graphics.setForegroundColor(TextColor.ANSI.WHITE)
    graphics.putString(x + progressBarLength - 1, y, "]")

    graphics.setForegroundColor(TextColor.ANSI.DEFAULT) // Reset to default color
  end renderProgressBar

  def renderSkillSpinner(graphics: TextGraphics, skill: Skill): Unit =
    val spinnerChar = spinnerStates(spinnerIndex) // Get the current spinner character
    graphics.putString(30, 9, s"Spinner: $spinnerChar ${skill.name} in progress...")
  end renderSkillSpinner

  def activateMenuItem(state: GameState): Unit = {
    val selectedMenuItem = menu.getSelectedMenuItem
    if (selectedMenuItem == "Woodcutting") state.activeSkill = Some(Woodcutting())
    else if (selectedMenuItem == "Mining") state.activeSkill = Some(Mining())
    else if (selectedMenuItem == "Woodworking") state.activeSkill = Some(Woodworking())
    else if (selectedMenuItem == "StoneCutting") state.activeSkill = Some(StoneCutting())
  }

  def handleInput(keyStroke: KeyStroke, state: GameState): Unit =
    keyStroke.getKeyType match {
      case KeyType.ArrowDown                                  => menu.navigateMenu(1)
      case KeyType.ArrowUp                                    => menu.navigateMenu(-1)
      case KeyType.Enter                                      => activateMenuItem(state)
      case KeyType.Character if keyStroke.getCharacter == ' ' => activateMenuItem(state)
      case _                                                  => // Other keys can be handled here if necessary
    }
  end handleInput
end Scelverna
