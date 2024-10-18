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
  def parent(state: GameState): Skill

  override def xpForNextLevel: Int         = level * 50
  
  override def update(state: GameState): GameState =
    parent(state).update(state) // update parent skill first
    super.update(state)         // then update sub skill (using the same logic
  end update

  def render(pos: Pos, state: GameState): TerminalParagraph =
    val x = pos.x
    val y = pos.y
    val pb = ProgressBarParameters
    
    val par: Skill = parent(state)
    val parentXpString = s"${par.name} (${par.level} / 99) -> ${par.xp} / ${par.xpForNextLevel}"
    def skillXpBar = TerminalParagraph(
      List(TerminalString(parentXpString, Pos(x, y), WHITE))
        ++ ProgressBar.from(pb(40, par.progressToNextLevel, Pos(x, y + 1), BLUE_BRIGHT))
    )
    
    def masteryXpBar = TerminalParagraph(
      List(TerminalString(s"$name ($level / 99) -> $xp / $xpForNextLevel", Pos(x, y + 3), WHITE))
        ++ ProgressBar.from(pb(40, progressToNextLevel, Pos(x, y + 4), CYAN))
    )

    def actionBar = TerminalParagraph(
      List(
        TerminalString(s"Action Progress: ETA: ", Pos(x, y + 6), WHITE),
        TerminalString(f"$remainingDuration%1.1f", Pos(x + 22, y + 6), CYAN_BRIGHT),
        TerminalString(" seconds", Pos(x + 26, y + 6), WHITE)
      )
        ++ ProgressBar.from(pb(40, actionProgress, Pos(x, y + 7), GREEN_BRIGHT))
    )

    TerminalParagraph(skillXpBar.list ++ masteryXpBar.list ++ actionBar.list)
  end render
end SubSkill

case class Woodcutting() extends Skill:
  override val name: String = "Woodcutting Skill"
  
  override def onComplete(state: GameState, gainedXp: WidthInColumns): Unit = {
    state.activityLog.add(s"Got $gainedXp XP in Woodcutting.")
  }
end Woodcutting

class WoodCuttingOak() extends SubSkill:
  override val name: String = "Oak Mastery"
  
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
