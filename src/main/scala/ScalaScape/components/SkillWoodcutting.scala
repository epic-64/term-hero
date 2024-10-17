package ScalaScape.components

import ScalaScape.utils.TerminalArt
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics

sealed trait TreeType {
  val name: String
  var xp: Int
  var level: Int
  var actionProgress: Between0And1 = 0.0
  val actionDuration: Seconds      = 3.0

  // Method to retrieve cached ASCII art or parse it once
  def getAsciiArt(position: Position): List[TerminalString] = parseArt(position)

  // Abstract method to be implemented by each skill for parsing art
  protected def parseArt(position: Position): List[TerminalString] = ???
}

case class Oak() extends TreeType {
  val name: String = "Oak"
  var xp: Int      = 0
  var level: Int   = 1

  override def parseArt(position: Position): List[TerminalString] = {
    val art: String =
      """
          |#############
          |#           #
          |#  > Oak    #
          |#           #
          |#############
          |""".stripMargin

    TerminalArt.parseWithoutColorMap(art, Position(position.x, position.y - 1))
  }
}

case class Willow() extends TreeType {
  val name: String = "Willow"
  var xp: Int      = 0
  var level: Int   = 1
}

case class Teak() extends TreeType {
  val name: String = "Teak"
  var xp: Int      = 0
  var level: Int   = 1
}

case class Woodcutting() extends Skill {
  val name: String                   = "Woodcutting"
  var xp: Int                        = 0
  var level: Int                     = 1
  var selectedTree: Option[TreeType] = None
  val treeTypes: List[TreeType]      = List(Oak(), Willow(), Teak())

  def selectTree(index: Int): Woodcutting = {
    selectedTree = Some(treeTypes(index))

    this
  }

  def updateSelectedTree(state: GameState, targetFps: Int): Unit =
    selectedTree.foreach(_.actionProgress += 1.0 / (actionDuration * targetFps))
  // Add additional logic for when the action is completed

  override def onComplete(state: GameState): Unit =
    selectedTree.foreach { tree =>
      tree.xp += 10
      if (tree.xp >= tree.level * 100) {
        tree.level += 1
        tree.xp = 0
      }
      // Handle inventory update for specific tree type (e.g., "Oak Wood")
      state.inventory = state.inventory.updated(s"${tree.name} Wood", state.inventory(s"${tree.name} Wood") + 1)
    }

  override def update(state: GameState, targetFps: Int): Unit = {
    updateSelectedTree(state, targetFps)
    super.update(state, targetFps) // Update Woodcutting XP
  }

  override def parseArt(position: Position): List[TerminalString] = {
    val art: String = """
                        |              ,@@@@@@@,
                        |      ,,,.   ,@@@@@@/@@,  .oo8888o.
                        |   ,&%%&%&&%,@@@@@/@@@@@@,:8888\88/8o
                        |  ,%&\%&&%&&%,@@@\@@/@@@88\88888/88'
                        |  %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
                        |  %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
                        |  `&%\ ` /%&'    |.|        \ '|8'
                        |      |o|        | |         | |
                        |      |.|        | |         | |
                        |___ \/ ._\//_/__/  ,\_\//__\/.  \_//__
                        |""".stripMargin

    val colorMap = Map(
      '@' -> GREEN_BRIGHT,
      '&' -> GREEN,
      '%' -> GREEN,
      '8' -> GREEN,
      'o' -> GREEN,
      'G' -> GREEN,
      'B' -> GREEN_BRIGHT,
      'W' -> WHITE
    )

    val colors: String = """
                           |              ,@@@@@@@,
                           |      ,,,.   ,@@@@@@/@@,  .oo8888o.
                           |   ,&%%&%&&%,@@@@@/@@@@@@,:8888\88/8o
                           |  ,%&\%&&%&&%,@@@\@@/@@@88\88888/88'
                           |  %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
                           |  %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
                           |  `&%\ ` /%&'    |.|        \ '|8'
                           |      |W|        | |         | |
                           |      |.|        | |         | |
                           |___ B/ ._\BG_B__/  G\_BGG__B/.  \_BG__
                           |""".stripMargin

    TerminalArt.parseWithColorMap(art, colors, Position(position.x, position.y - 1), colorMap)
  }
}

class TreeSelectionMenu(val treeTypes: List[TreeType]) {
  private var selectedIndex: Int = 0

  def navigate(direction: Int): Unit = {
    selectedIndex = (selectedIndex + direction) % treeTypes.size
    if (selectedIndex < 0) selectedIndex = treeTypes.size - 1
  }

  def activateItem(woodcutting: Woodcutting): Woodcutting = {
    woodcutting.selectTree(selectedIndex)

    woodcutting
  }

  def render(graphics: TextGraphics, position: Position): Unit = {
    graphics.putString(position.x, position.y, "Select a tree type:")
    treeTypes.zipWithIndex.foreach { case (tree, index) =>
      val isSelected = index == selectedIndex
      val prefix     = if (isSelected) ">" else " "
      graphics.putString(position.x, position.y + index + 1, s"$prefix ${tree.name} (Level: ${tree.level})")
    }
  }
}
