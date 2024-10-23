package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*

import scala.reflect.ClassTag

trait CanGainXp:
  val name: String
  var xp: Int    = 0
  var level: Int = 0

  def xpForNextLevel: Int
  def progressToNextLevel: Double = xp.toDouble / xpForNextLevel

  def gainXp(amount: Int, state: GameState): Unit = {
    xp += amount

    if (xp >= xpForNextLevel) {
      level += 1
      xp = 0
    }

    state.activityLog.add(s"+ $amount XP in $name")(state)
  }
end CanGainXp

trait HasDuration:
  var actionProgress: Double = 0.0
  var actionDuration: Double = 3.0

  def remainingDuration: Double = actionDuration - (actionDuration * actionProgress)
end HasDuration

trait Mastery extends CanGainXp with HasDuration:
  val name: String
  val xpForSelf: Int
  val xpForParent: Int

  def parent(state: GameState): CanGainXp
  def onCompleteSideEffects(state: GameState, gainedXp: Int): Unit
  def requiredParentLevel: Int

  override def xpForNextLevel: Int = (level + 1) * 50

  def isUnlocked(state: GameState): Boolean = parent(state).level >= requiredParentLevel

  def update(state: GameState): GameState =
    if (actionProgress >= 1.0) {
      actionProgress = 0.0

      parent(state).gainXp(xpForParent, state)
      gainXp(xpForSelf, state)

      onCompleteSideEffects(state, xpForSelf)
    } else {
      actionProgress += 1.0 / (actionDuration * state.targetFps)
      actionProgress = actionProgress min 1.0
    }

    state
  end update

  def render(pos: Pos, state: GameState): RenderedBlock =
    val x     = pos.x
    val y     = pos.y
    val pb    = ProgressBarParameters
    val width = 40

    def skillXpBar: RenderedBlock = {
      val par: CanGainXp  = parent(state)
      val parentXpString  = s"${par.name} (${par.level} / 99)"
      val parentXpString2 = s"${par.xp} / ${par.xpForNextLevel}"

      val parts = List(
        RenderString(parentXpString, Pos(x, y), WHITE),
        RenderString(parentXpString2, Pos(x + width - parentXpString2.length, y), BLACK_BRIGHT)
      )

      RenderedBlock(parts)
        ++ ProgressBar.from(pb(width, par.progressToNextLevel, Pos(x, y + 1), BLUE_BRIGHT))
    }

    def masteryXpBar(offset: Int) = {
      val str1 = s"$name ($level / 99)"
      val str2 = s"$xp / ${xpForNextLevel}"

      RenderedBlock(
        List(
          RenderString(str1, Pos(x, y + offset), WHITE),
          RenderString(str2, Pos(x + width - str2.length, y + offset), BLACK_BRIGHT)
        )
      ) ++ ProgressBar.from(pb(width, progressToNextLevel, Pos(x, y + offset + 1), CYAN))
    }

    def actionBar(offset: Int) = {
      val line = List(
        ColorWord("Action Progress: ETA: ", WHITE),
        ColorWord(f"$remainingDuration%1.1f", CYAN_BRIGHT),
        ColorWord(" seconds", WHITE)
      )

      RenderedBlock(ColorLine(line).render(Pos(x, y + offset)))
        ++ ProgressBar.from(pb(width, actionProgress, Pos(x, y + offset + 1), GREEN_BRIGHT))
    }

    skillXpBar ++ masteryXpBar(3) ++ actionBar(6)
  end render
end Mastery

abstract class Skill extends CanGainXp:
  val masteries: List[Mastery]

  def mastery[T <: Mastery : ClassTag]: T = {
    val skill = masteries.collectFirst { case skill: T => skill }
    skill match {
      case Some(s) => s
      case None => throw new Exception(s"SubSkill ${implicitly[ClassTag[T]].runtimeClass.getSimpleName} not found.")
    }
  }
end Skill

class Woodcutting() extends Skill:
  override val name: String        = "Woodcutting"
  override def xpForNextLevel: Int = (level + 1) * 100

  override val masteries: List[Mastery] = List(
    OakMastery(),
    TeakMastery()
  )
end Woodcutting

class OakMastery() extends Mastery:
  override val name: String             = "Oak Mastery"
  override val requiredParentLevel: Int = 0
  override val xpForSelf: Int           = 10
  override val xpForParent: Int         = 10

  override def parent(state: GameState): Woodcutting = state.skills.woodcutting

  override def onCompleteSideEffects(state: GameState, gainedXp: Int): Unit =
    val key                 = "Oak"
    val item: InventoryItem = state.inventory.items(key)
    val addedQuantity       = 1

    state.inventory.items = state.inventory.items.updated(key, item.copy(quantity = item.quantity + addedQuantity))

    state.activityLog.add(s"+ $addedQuantity $key logs")(state)
  end onCompleteSideEffects
end OakMastery

class TeakMastery() extends Mastery:
  override val name: String             = "Teak Mastery"
  override val requiredParentLevel: Int = 5
  override val xpForSelf: Int           = 10
  override val xpForParent: Int         = 10

  override def parent(state: GameState): Woodcutting = state.skills.woodcutting

  override def onCompleteSideEffects(state: GameState, gainedXp: Int): Unit =
    val key                 = "Teak"
    val item: InventoryItem = state.inventory.items(key)
    val addedQuantity       = 1

    state.inventory.items = state.inventory.items.updated(key, item.copy(quantity = item.quantity + addedQuantity))

    state.activityLog.add(s"+ $addedQuantity $key logs")(state)
  end onCompleteSideEffects
end TeakMastery
