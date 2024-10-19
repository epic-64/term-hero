package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*

trait Skill:
  val name: String

  var xp: Int                      = 0
  var level: Int                   = 1
  var actionProgress: Between0And1 = 0.0
  val actionDuration: Seconds      = 3.0

  def xpForNextLevel: Int         = level * 100
  def progressToNextLevel: Double = xp.toDouble / xpForNextLevel
  def remainingDuration: Double   = actionDuration * (1 - actionProgress)

  def update(state: GameState): GameState =
    actionProgress = actionProgress min 1.0

    if (actionProgress >= 1.0) {
      actionProgress = 0.0
      val gainedXp = 10

      gainXp(gainedXp)
      onComplete(state, gainedXp)
    } else {
      actionProgress += 1.0 / (actionDuration * state.targetFps)
    }

    state
  end update

  private def gainXp(amount: Int): Unit = {
    xp += amount

    if (xp >= xpForNextLevel) {
      level += 1
      xp = 0
    }
  }

  protected def onComplete(state: GameState, gainedXp: Int): Unit = ()
end Skill

trait SubSkill extends Skill:
  def requiredParentLevel: Int
  
  override def xpForNextLevel: Int = level * 50
  
  def parent(state: GameState): Skill

  override def update(state: GameState): GameState =
    parent(state).update(state) // update parent skill first
    super.update(state) // then update sub skill (using the same logic
  end update

  def render(pos: Pos, state: GameState): TerminalParagraph =
    val x     = pos.x
    val y     = pos.y
    val pb    = ProgressBarParameters
    val width = 40

    def skillXpBar: TerminalParagraph = {
      val par: Skill      = parent(state)
      val parentXpString  = s"${par.name} (${par.level} / 99)"
      val parentXpString2 = s"${par.xp} / ${par.xpForNextLevel}"
      
      val parts = List(
        TerminalString(parentXpString, Pos(x, y), WHITE),
        TerminalString(parentXpString2, Pos(x + width - parentXpString2.length, y), BLACK_BRIGHT)
      )
      
      TerminalParagraph(parts)
        ++ ProgressBar.from(pb(width, par.progressToNextLevel, Pos(x, y + 1), BLUE_BRIGHT))
    }

    def masteryXpBar(offset: Int) = {
      val str1 = s"$name ($level / 99)"
      val str2 = s"$xp / ${xpForNextLevel}"

      TerminalParagraph(
        List(
          TerminalString(str1, Pos(x, y + offset), WHITE),
          TerminalString(str2, Pos(x + width - str2.length, y + offset), BLACK_BRIGHT)
        )
      ) ++ ProgressBar.from(pb(width, progressToNextLevel, Pos(x, y + offset + 1), CYAN))
    }

    def actionBar(offset: Int) = {
      val line = List(
        LineWord("Action Progress: ETA: ", WHITE),
        LineWord(f"$remainingDuration%1.1f", CYAN_BRIGHT),
        LineWord(" seconds", WHITE)
      )

      TerminalLine(line, Pos(x, y + offset)).toParagraph
        ++ ProgressBar.from(pb(width, actionProgress, Pos(x, y + offset + 1), GREEN_BRIGHT))
    }

    skillXpBar ++ masteryXpBar(3) ++ actionBar(6)
  end render
end SubSkill

case class Woodcutting() extends Skill:
  override val name: String = "Woodcutting"

  override def onComplete(state: GameState, gainedXp: WidthInColumns): Unit =
    state.activityLog.add(s"Got $gainedXp XP in Woodcutting.")
end Woodcutting

class WoodCuttingOak() extends SubSkill:
  override val name: String = "Oak Mastery"
  override val requiredParentLevel: Int = 0

  override def parent(state: GameState): Woodcutting = state.skills.woodcutting

  def getInventorySlot(state: GameState): InventoryItem = state.inventory.items("Oak")

  override def onComplete(state: GameState, gainedXp: Int): Unit =
    val key                 = "Oak"
    val item: InventoryItem = state.inventory.items(key)
    val addedQuantity       = 1

    state.inventory.items = state.inventory.items.updated(key, item.copy(quantity = item.quantity + addedQuantity))

    state.activityLog.add(s"Got $addedQuantity $key logs.")
    state.activityLog.add(s"Got $gainedXp XP in $name")
  end onComplete
end WoodCuttingOak

class WoodCuttingTeak() extends SubSkill:
  override val name: String = "Teak Mastery"
  override val requiredParentLevel: Int = 5

  override def parent(state: GameState): Woodcutting = state.skills.woodcutting

  def getInventorySlot(state: GameState): InventoryItem = state.inventory.items("Teak")

  override def onComplete(state: GameState, gainedXp: Int): Unit =
    val key                 = "Teak"
    val item: InventoryItem = state.inventory.items(key)
    val addedQuantity       = 1

    state.inventory.items = state.inventory.items.updated(key, item.copy(quantity = item.quantity + addedQuantity))

    state.activityLog.add(s"Got $addedQuantity $key logs.")
    state.activityLog.add(s"Got $gainedXp XP in $name")
  end onComplete
end WoodCuttingTeak
