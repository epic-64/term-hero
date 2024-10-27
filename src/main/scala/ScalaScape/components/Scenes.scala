package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

abstract class Scene(state: GameState):
  val name: String
  val description: String = "No description available"

  private def breadCrumbs: String =
    previousScene match
      case Some(scene) => scene.breadCrumbs + " / " + scene.name
      case None        => ""

  private def renderHeader(pos: Pos): RenderedBlock =
    def nameWithBreadcrumb = s"$breadCrumbs / $name"

    RenderedBlock(List(RenderString(description, pos, WHITE)))
      ++ RenderedBlock(List(RenderString("-" * 40, Pos(pos.x, pos.y + 1), WHITE)))
      ++ asciiArt(Pos(pos.x, pos.y + 2))
      ++ RenderedBlock(List(RenderString("-" * 40, Pos(pos.x, pos.y + 12), WHITE)))
      ++ RenderedBlock(List(RenderString(nameWithBreadcrumb, Pos(pos.x, pos.y + 13), WHITE)))
  end renderHeader

  def handleInput(key: KeyStroke, state: GameState): GameState =
    previousScene match
      case Some(scene) =>
        key.getKeyType match {
          case KeyType.Escape    => state.swapScene(scene)
          case KeyType.ArrowLeft => state.swapScene(scene)
          case _                 =>
        }
      case None        => ()

    typeHandleInput(key, state)
  end handleInput

  def update(state: GameState): GameState =
    // add shared logic for all scenes here
    typeUpdate(state) // additional scene specific logic

  def render(state: GameState, pos: Pos): RenderedBlock =
    renderHeader(pos) ++ typeRender(state, Pos(pos.x, pos.y + 14))
  end render

  def asciiArt(pos: Pos): RenderedBlock = RenderedBlock(List(RenderString("No ASCII art available", pos)))
  def previousScene: Option[Scene]

  def typeHandleInput(key: KeyStroke, state: GameState): GameState = state
  def typeUpdate(state: GameState): GameState                      = state
  def typeRender(state: GameState, pos: Pos): RenderedBlock
end Scene

abstract class MenuScene(state: GameState) extends Scene(state):
  lazy val menu: ActionMenu

  override def typeHandleInput(key: KeyStroke, state: GameState): GameState = menu.handleInput(key, state)
  override def typeRender(state: GameState, pos: Pos): RenderedBlock        = menu.render(Pos(pos.x, pos.y + 1))
end MenuScene

class WorldMenuScene(state: GameState) extends MenuScene(state):
  override val name                              = "W"
  override val description: String               = "The world is your oyster."
  override def previousScene: Option[Scene]      = None
  override def asciiArt(pos: Pos): RenderedBlock = WorldMapArtwork(pos)

  override lazy val menu = ActionMenu(() =>
    List(
      ActionItem(true, ColorLine("Gathering"), (state: GameState) => state.swapScene(state.scenes.gathering)),
      ActionItem(true, ColorLine("Building"), (state: GameState) => state.swapScene(state.scenes.building)),
    ),
  )
end WorldMenuScene

class BuildingMenuScene(state: GameState) extends MenuScene(state):
  override val name          = "Building"
  override def previousScene = Some(state.scenes.world)

  override lazy val menu = ActionMenu(() =>
    val requirements = RequirementList(
      List(
        ItemRequirement(state.inventory.items.oak, (state.houseLevel + 1) * 10),
        ItemRequirement(state.inventory.items.teak, (state.houseLevel + 1) * 5),
      )
    )

    val label = ColorLine(List(ColorWord("Upgrade House"))) ++ requirements.getLabels

    val actionItem = ActionItem(
      isSelectable = requirements.check(),
      label = label,
      action = (state: GameState) => {
        requirements.resolve(state)
        state.houseLevel += 1
        state.eventLog.add(s"House upgraded to Level ${state.houseLevel}")(state)
        state
      },
    )

    List(actionItem)
  )
end BuildingMenuScene

class HouseBuildingScene(state: GameState) extends Scene(state):
  override val name        = "House Building"
  override val description = "Build your dream house."

  override def previousScene: Option[BuildingMenuScene]              = Some(state.scenes.building)
  override def typeRender(state: GameState, pos: Pos): RenderedBlock =
    RenderedBlock(List(RenderString("No house building available", pos)))
end HouseBuildingScene

// Gathering
class GatheringMenuScene(state: GameState) extends MenuScene(state):
  override val name          = "Gathering"
  override def previousScene = Some(state.scenes.world)

  override lazy val menu = ActionMenu(() =>
    List(
      ActionItem(true, ColorLine("Woodcutting"), (state: GameState) => state.swapScene(state.scenes.woodcutting))
    )
  )
end GatheringMenuScene

class WoodCuttingMenuScene(state: GameState) extends MenuScene(state):
  override val name = "Woodcutting"

  override def previousScene = Some(state.scenes.gathering)

  val skill: Skill = state.skills.woodcutting

  def getLabel(mastery: Mastery): ColorLine =
    val level         = mastery.level
    val requiredLevel =
      if skill.level < mastery.requiredParentLevel
      then s"Req Lvl ${mastery.requiredParentLevel}"
      else ""
    val name          = mastery.name

    ColorLine(
      List(
        ColorWord(s"$name ($level / 99)"),
        ColorWord(s" $requiredLevel", RED),
      ),
    )

  private val getMenuItems: () => List[ActionItem] = () =>
    List(
        ActionItem(
          skill.mastery[OakMastery].isUnlocked(state),
          getLabel(skill.mastery[OakMastery]),
          (state: GameState) => state.swapScene(state.scenes.oak),
        ),
        ActionItem(
          skill.mastery[TeakMastery].isUnlocked(state),
          getLabel(skill.mastery[TeakMastery]),
          (state: GameState) => state.swapScene(state.scenes.teak),
        ),
    )

  override lazy val menu                         = ActionMenu(getMenuItems)
  override def asciiArt(pos: Pos): RenderedBlock = WoodCuttingArtwork(pos)
end WoodCuttingMenuScene

abstract class MasteryScene(state: GameState) extends Scene(state):
  def getMastery(state: GameState): Mastery

  override def typeUpdate(state: GameState): GameState               = getMastery(state).update(state)
  override def typeRender(state: GameState, pos: Pos): RenderedBlock =
    getMastery(state).render(Pos(pos.x, pos.y + 1), state)
end MasteryScene

class OakScene(state: GameState) extends MasteryScene(state):
  override val name        = "Oak"
  override val description = "Cut down some oak trees."

  override def getMastery(state: GameState): OakMastery    = state.skills.woodcutting.mastery[OakMastery]
  override def asciiArt(pos: Pos): RenderedBlock           = WoodCuttingArtwork(pos)
  override def previousScene: Option[WoodCuttingMenuScene] = Some(state.scenes.woodcutting)
end OakScene

class TeakScene(state: GameState) extends MasteryScene(state):
  override val name        = "Teak"
  override val description = "Cut down some teak trees."

  override def getMastery(state: GameState): TeakMastery   = state.skills.woodcutting.mastery[TeakMastery]
  override def asciiArt(pos: Pos): RenderedBlock           = WoodCuttingArtwork(pos)
  override def previousScene: Option[WoodCuttingMenuScene] = Some(state.scenes.woodcutting)
end TeakScene
