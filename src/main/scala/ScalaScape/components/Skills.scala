package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*

trait Skill:
  val name: String

  var xp: Int
  var level: Int

  var actionProgress: Between0And1 = 0.0
  val actionDuration: Seconds      = 3.0

  def xpForNextLevel: Int         = level * 100
  def progressToNextLevel: Double = xp.toDouble / xpForNextLevel
  def remainingDuration: Double   = actionDuration * (1 - actionProgress)

  def render(pos: Pos): TerminalParagraph =
    val x  = pos.x
    val y  = pos.y
    val pb = ProgressBarParameters

    def xpBar = TerminalParagraph(
      List(TerminalString(s"XP Progress: $xp / $xpForNextLevel", Pos(x, y + 0), WHITE))
        ++ ProgressBar.from(pb(40, progressToNextLevel, Pos(x, y + 1), BLUE_BRIGHT))
    )

    def actionBar = TerminalParagraph(
      List(
        TerminalString(s"Action Progress: ETA: ", Pos(x, y + 3), WHITE),
        TerminalString(f"$remainingDuration%1.1f", Pos(x + 22, y + 3), CYAN_BRIGHT),
        TerminalString(" seconds", Pos(x + 26, y + 3), WHITE)
      )
        ++ ProgressBar.from(pb(40, actionProgress, Pos(x, y + 4), GREEN_BRIGHT))
    )

    TerminalParagraph(xpBar.list ++ actionBar.list)
  end render

  private def gainXp(amount: Int): Unit = {
    xp += amount

    if (xp >= xpForNextLevel) {
      level += 1
      xp = 0
    }
  }

  protected def onComplete(state: GameState, gainedXp: Int): Unit = ()

  def update(state: GameState): Skill =
    actionProgress = actionProgress min 1.0

    if (actionProgress >= 1.0) {
      actionProgress = 0.0
      val gainedXp = 10
      
      gainXp(gainedXp)
      onComplete(state, gainedXp)
    } else {
      actionProgress += 1.0 / (actionDuration * state.targetFps)
    }

    this
  end update
end Skill

case class Woodcutting() extends Skill:
  val name: String = "Woodcutting"
  var xp: Int = 0
  var level: Int = 1
  val critChance = 0.5

  override def onComplete(state: GameState, gainedXp: Int): Unit =
    val key = "Wood"
    val item: InventoryItem = state.inventory.items(key)
    val isCritical = scala.util.Random.nextDouble() < critChance
    val addedQuantity = if isCritical then 2 else 1

    state.inventory.items = state.inventory.items.updated(key, item.copy(quantity = item.quantity + addedQuantity))
    
    state.activityLog.add(s"Got $addedQuantity Wood ${if isCritical then "!!" else ""}")
    state.activityLog.add(s"Got $gainedXp XP in Woodcutting.")
  end onComplete
end Woodcutting
