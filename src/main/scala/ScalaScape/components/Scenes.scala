package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

abstract class Scene(state: GameState):
  val name: String
  val description: String = "No description available"

  private def breadCrumbs: String =
    previousScene match
      case Some(scene) => scene.breadCrumbs + " / " + scene.name
      case None => ""

  private def renderHeader(pos: Pos): RenderBlock =
    def nameWithBreadcrumb = s"$breadCrumbs / $name"

    RenderBlock(List(RenderString(description, pos, WHITE)))
      ++ RenderBlock(List(RenderString("-" * 40, Pos(pos.x, pos.y + 1), WHITE)))
      ++ asciiArt(Pos(pos.x, pos.y + 2))
      ++ RenderBlock(List(RenderString("-" * 40, Pos(pos.x, pos.y + 12), WHITE)))
      ++ RenderBlock(List(RenderString(nameWithBreadcrumb, Pos(pos.x, pos.y + 13), WHITE)))
  end renderHeader

  def handleInput(key: KeyStroke, state: GameState): GameState =
    previousScene match
      case Some(scene) =>
        key.getKeyType match {
          case KeyType.Escape => state.swapScene(scene);
          case KeyType.ArrowLeft => state.swapScene(scene)
          case _ =>
        }
      case None => ()

    typeHandleInput(key, state)
  end handleInput

  def update(state: GameState): GameState =
    // add shared logic for all scenes here
    typeUpdate(state) // additional scene specific logic

  def render(state: GameState, pos: Pos): RenderBlock =
    renderHeader(pos) ++ typeRender(state, Pos(pos.x, pos.y + 14))
  end render

  def asciiArt(pos: Pos): RenderBlock = RenderBlock(List(RenderString("No ASCII art available", pos)))

  def previousScene: Option[Scene]

  def typeHandleInput(key: KeyStroke, state: GameState): GameState = state
  def typeUpdate(state: GameState): GameState                      = state

  def typeRender(state: GameState, pos: Pos): RenderBlock
end Scene

abstract class MenuScene(state: GameState) extends Scene(state):
  lazy val menu: ActionMenu

  override def typeHandleInput(key: KeyStroke, state: GameState): GameState = menu.handleInput(key, state)

  override def typeRender(state: GameState, pos: Pos): RenderBlock = menu.render(Pos(pos.x, pos.y + 1))
end MenuScene

class WorldMenuScene(state: GameState) extends MenuScene(state):
  override val name = "W"
  override val description: String = "The world is your oyster."

  override def previousScene: Option[Scene] = None
  override def asciiArt(pos: Pos): RenderBlock = WorldMapArtwork(pos)

  override lazy val menu = ActionMenu(
    Map(
      ColorLine("Gathering") -> ActionItem(true, (state: GameState) => state.swapScene(state.scenes.gathering))
    )
  )
end WorldMenuScene

// Gathering
class GatheringMenuScene(state: GameState) extends MenuScene(state):
  override val name = "Gathering"

  override def previousScene = Some(state.scenes.world)

  override lazy val menu = ActionMenu(
    Map(
      ColorLine("Woodcutting") -> ActionItem(true, (state: GameState) => state.swapScene(state.scenes.woodcutting)),
      ColorLine("Go back") -> ActionItem(true, (state: GameState) => state.swapScene(state.scenes.world))
    )
  )
end GatheringMenuScene

class WoodCuttingMenuScene(state: GameState) extends MenuScene(state):
  override val name = "Woodcutting"

  override def previousScene = Some(state.scenes.gathering)

  val skill: Skill = state.skills.woodcutting

  def getLabel(mastery: Mastery): ColorLine =
    val level = mastery.level
    val requiredLevel =
      if skill.level < mastery.requiredParentLevel
      then s"Req Lvl ${mastery.requiredParentLevel}"
      else ""
    val name = mastery.name

    ColorLine(
      List(
        ColorWord(s"$name ($level / 99)"),
        ColorWord(s" $requiredLevel", RED)
      )
    )

  val menuMap = Map(
    getLabel(skill.mastery[OakMastery]) ->
      ActionItem(
        skill.mastery[OakMastery].isUnlocked(state),
        (state: GameState) => state.swapScene(state.scenes.oak)
      ),
    getLabel(skill.mastery[TeakMastery]) ->
      ActionItem(
        skill.mastery[TeakMastery].isUnlocked(state),
        (state: GameState) => state.swapScene(state.scenes.teak)
      ),
    ColorLine("Go back") ->
      ActionItem(true, (state: GameState) => state.swapScene(state.scenes.gathering))
  )

  override lazy val menu = ActionMenu(menuMap)
  override def asciiArt(pos: Pos): RenderBlock = WoodCuttingArtwork(pos)
end WoodCuttingMenuScene

abstract class MasteryScene(state: GameState) extends Scene(state):
  def getMastery(state: GameState): Mastery

  override def typeUpdate(state: GameState): GameState = getMastery(state).update(state)
  override def typeRender(state: GameState, pos: Pos): RenderBlock =
    getMastery(state).render(Pos(pos.x, pos.y + 1), state)
end MasteryScene

class OakScene(state: GameState) extends MasteryScene(state):
  override val name        = "Oak"
  override val description = "Cut down some oak trees."

  override def getMastery(state: GameState): OakMastery = state.skills.woodcutting.mastery[OakMastery]

  override def asciiArt(pos: Pos): RenderBlock = WoodCuttingArtwork(pos)

  override def previousScene = Some(state.scenes.woodcutting)
end OakScene

class TeakScene(state: GameState) extends MasteryScene(state):
  override val name        = "Teak"
  override val description = "Cut down some teak trees."

  override def getMastery(state: GameState): TeakMastery = state.skills.woodcutting.mastery[TeakMastery]

  override def asciiArt(pos: Pos): RenderBlock = WoodCuttingArtwork(pos)

  override def previousScene = Some(state.scenes.woodcutting)
end TeakScene
