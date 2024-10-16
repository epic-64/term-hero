package ScalaScape.components

import ScalaScape.game.skills.Skill
import ScalaScape.ui.lantern.Position
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.GREEN_BRIGHT
import com.googlecode.lanterna.graphics.TextGraphics

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
