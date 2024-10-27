package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.{RED, WHITE_BRIGHT}

trait Requirement:
  val identifier: String
  def progress: String
  def check(): Boolean
  def resolve(state: GameState): GameState
  def getColor: TextColor = if check() then WHITE_BRIGHT else RED
  def toLabel: ColorLine = ColorLine(List(ColorWord(s" ($identifier: $progress)", getColor)))

class RequirementList(val items: List[Requirement]):
  def check(): Boolean = items.forall(_.check())
  def resolve(state: GameState): GameState = { items.foreach(_.resolve(state)); state }
  def getLabels: List[ColorLine] = items.map(_.toLabel)
end RequirementList

class ItemRequirement(val item: InventoryItem, val quantity: Int) extends Requirement:
  val identifier: String                   = item.name
  def progress: String                     = s"${item.quantity}/${quantity}"
  def check(): Boolean                     = item.quantity >= quantity

  def resolve(state: GameState): GameState = {
    item.quantity -= quantity;
    state
  }
end ItemRequirement

class SkillRequirement(val skill: Skill, val level: Int) extends Requirement:
  val identifier: String                   = skill.name
  def progress: String                     = s"${skill.level}/${level}"
  def check(): Boolean                     = skill.level >= level

  def resolve(state: GameState): GameState = state // do nothing
end SkillRequirement
