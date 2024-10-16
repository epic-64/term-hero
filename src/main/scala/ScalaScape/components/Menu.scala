package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.GREEN_BRIGHT
import com.googlecode.lanterna.graphics.TextGraphics

case class Spinner(
    states: List[String] = "|/-\\".toList.map(_.toString),
    framesPerState: Int = 5,
    var index: Int = 0,
    var updateCounter: Int = 0
):
  def update(): Unit =
    if updateCounter >= framesPerState then
      index = (index + 1) % states.size
      updateCounter = 0
    else updateCounter += 1
  end update

  def currentState: String = states(index)
end Spinner

class Menu(val gatheringSkills: List[Skill], val manufacturingSkills: List[Skill]):
  private val items: List[String] =
    gatheringSkills.map(_.name)
      ++ manufacturingSkills.map(_.name)
      :+ "Inventory"

  private val spinner            = Spinner()
  private var selectedIndex: Int = 0

  def navigate(direction: Int): Unit =
    // Cycle through the menu items, including skills and inventory
    selectedIndex = selectedIndex + direction match {
      case i if i < 0           => items.size - 1
      case i if i >= items.size => 0
      case i                    => i
    }
  end navigate

  def activateItem(state: GameState): Unit =
    if (state.skills.contains(selectedItem)) {
      state.activeSkill = Some(state.skills(selectedItem))
    }
  end activateItem

  def selectedItem: String = items(selectedIndex)

  def update(): Unit =
    spinner.update()
  end update

  def render(activeSkill: Option[Skill], position: Pos): TerminalParagraph =
    val x = position.x

    def buildSkillItemText(
        skill: Skill,
        isActive: Boolean,
        isSelected: Boolean,
        position: Pos
    ): TerminalParagraph =
      val x            = position.x
      val y            = position.y
      val color        = if isActive then TextColor.ANSI.WHITE_BRIGHT else TextColor.ANSI.WHITE
      val spinnerState = if isActive then s"${spinner.currentState}" else ""

      TerminalParagraph(
        List(
          TerminalString(s"${if isSelected then ">" else " "} ${skill.name} $spinnerState", Pos(x, y), color)
        )
      )
    end buildSkillItemText

    // Build gathering skill menu
    val gatheringItems = gatheringSkills.zipWithIndex.map { case (skill, index) =>
      buildSkillItemText(skill, activeSkill.contains(skill), selectedIndex == index, Pos(x, 3 + index))
    }

    // Build manufacturing skill menu
    val manufacturingItems = manufacturingSkills.zipWithIndex.map { case (skill, index) =>
      val isSelected = selectedIndex == gatheringSkills.size + index
      val position   = Pos(x, 6 + gatheringSkills.size + index)
      buildSkillItemText(skill, activeSkill.contains(skill), isSelected, position)
    }

    // Build Management menu
    val inventoryIndex = gatheringSkills.size + manufacturingSkills.size
    val inventoryColor =
      if activeSkill.isEmpty && selectedItem == "Shop"
      then GREEN_BRIGHT
      else TextColor.ANSI.DEFAULT

    val inventoryItem = TerminalParagraph(
      List(
        TerminalString(
          s"${if (selectedIndex == inventoryIndex) ">" else " "} Shop",
          Pos(x, 9 + gatheringSkills.size + manufacturingSkills.size),
          inventoryColor
        )
      )
    )

    TerminalParagraph(
      List(
        TerminalString("Gathering", Pos(x, 1)),
        TerminalString("---------", Pos(x, 2))
      )
        ++ gatheringItems.flatMap(_.list)
        ++ List(
          TerminalString("Manufacturing", Pos(x, 4 + gatheringSkills.size)),
          TerminalString("-------------", Pos(x, 5 + gatheringSkills.size))
        )
        ++ manufacturingItems.flatMap(_.list)
        ++ List(
          TerminalString("Management", Pos(x, 7 + gatheringSkills.size + manufacturingSkills.size)),
          TerminalString("----------", Pos(x, 8 + gatheringSkills.size + manufacturingSkills.size))
        )
        ++ inventoryItem.list
    )
  end render
end Menu
