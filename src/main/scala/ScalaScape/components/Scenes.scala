package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

abstract class Scene:
  val name: String
  val description: String = "No description available"

  private def breadCrumbs: String =
    previousScene match
      case Some(scene) => scene.breadCrumbs + " / " + scene.name
      case None => ""

  private def renderHeader(pos: Pos): TerminalParagraph =
    def nameWithBreadcrumb = s"$breadCrumbs / $name"

    TerminalParagraph(List(TerminalString(description, pos, WHITE)))
      ++ TerminalParagraph(List(TerminalString("-" * 40, Pos(pos.x, pos.y + 1), WHITE)))
      ++ asciiArt(Pos(pos.x, pos.y + 2))
      ++ TerminalParagraph(List(TerminalString("-" * 40, Pos(pos.x, pos.y + 12), WHITE)))
      ++ TerminalParagraph(List(TerminalString(nameWithBreadcrumb, Pos(pos.x, pos.y + 13), WHITE)))
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

  def render(state: GameState, pos: Pos): TerminalParagraph =
    renderHeader(pos) ++ typeRender(state, Pos(pos.x, pos.y + 14))
  end render

  def asciiArt(pos: Pos): TerminalParagraph = TerminalParagraph(List(TerminalString("No ASCII art available", pos)))

  def previousScene: Option[Scene]

  def typeHandleInput(key: KeyStroke, state: GameState): GameState = state
  def typeUpdate(state: GameState): GameState                      = state
  def typeRender(state: GameState, pos: Pos): TerminalParagraph
end Scene

abstract class MenuScene extends Scene:
  lazy val menu: SceneMenu

  override def typeHandleInput(key: KeyStroke, state: GameState): GameState =
    menu.handleInput(key, state)

  override def typeRender(state: GameState, pos: Pos): TerminalParagraph =
    val menuStr: TerminalParagraph = menu.render(Pos(pos.x, pos.y + 1))

    TerminalParagraph(menuStr.list)
  end typeRender
end MenuScene

class WorldMenuScene extends MenuScene:
  override val name = "W"
  override val description: String = "The world is your oyster."

  override def previousScene: Option[Scene] = None
  override def asciiArt(pos: Pos): TerminalParagraph = WorldMapArtwork(pos)

  override lazy val menu = SceneMenu(
    Map(
      "Gathering"  -> GatheringMenuScene(),
      "Crafting"   -> CraftingMenuScene(),
      "Dungeoning" -> DungeoningMenuScene()
    )
  )
end WorldMenuScene

// Gathering
class GatheringMenuScene extends MenuScene:
  override val name = "Gathering"

  override def previousScene = Some(WorldMenuScene())

  override lazy val menu = SceneMenu(
    Map(
      "Woodcutting" -> WoodCuttingMenuScene(),
      "Mining"      -> MiningMenuScene(),
      "Go back"     -> WorldMenuScene()
    )
  )
end GatheringMenuScene

class WoodCuttingMenuScene extends MenuScene:
  override val name = "Woodcutting"

  override def previousScene = Some(GatheringMenuScene())

  override lazy val menu = SceneMenu(
    Map(
      "Oak"     -> WoodCuttingOakScene(),
      "Teak"    -> WoodCuttingTeakScene(),
      "Go back" -> GatheringMenuScene()
    )
  )

  override def asciiArt(pos: Pos): TerminalParagraph = WoodCuttingArtwork(pos)
end WoodCuttingMenuScene

abstract class SubSkillScene extends Scene:
  def getSkill(state: GameState): Mastery

  override def typeUpdate(state: GameState): GameState =
    getSkill(state).update(state)

  override def typeRender(state: GameState, pos: Pos): TerminalParagraph =
    getSkill(state).render(Pos(pos.x, pos.y + 1), state)
end SubSkillScene

class WoodCuttingOakScene extends SubSkillScene:
  override val name        = "Oak"
  override val description = "Cut down some oak trees."

  override def getSkill(state: GameState): WoodCuttingOak = state.skills.woodcutting.mastery[WoodCuttingOak]
  override def asciiArt(pos: Pos): TerminalParagraph      = WoodCuttingArtwork(pos)

  override def previousScene = Some(WoodCuttingMenuScene())
end WoodCuttingOakScene

class WoodCuttingTeakScene extends SubSkillScene:
  override val name        = "Teak"
  override val description = "Cut down some teak trees."

  override def getSkill(state: GameState): WoodCuttingTeak = state.skills.woodcutting.mastery[WoodCuttingTeak]
  override def asciiArt(pos: Pos): TerminalParagraph       = WoodCuttingArtwork(pos)

  override def previousScene = Some(WoodCuttingMenuScene())
end WoodCuttingTeakScene

class MiningMenuScene extends MenuScene:
  override val name = "Mining"

  override def previousScene = Some(GatheringMenuScene())

  override lazy val menu = SceneMenu(
    Map(
      "Mine"    -> MiningMenuScene(),
      "Go back" -> GatheringMenuScene()
    )
  )
end MiningMenuScene

// Crafting
class CraftingMenuScene extends MenuScene:
  override val name = "Crafting"

  override def previousScene: Option[WorldMenuScene] = Some(WorldMenuScene())

  override lazy val menu = SceneMenu(
    Map(
      "Woodworking"  -> WoodworkingMenuScene(),
      "Stonecutting" -> StonecuttingMenuScene(),
      "Go back"      -> WorldMenuScene()
    )
  )
end CraftingMenuScene

class WoodworkingMenuScene extends MenuScene:
  override val name = "Woodworking"

  override def previousScene = Some(CraftingMenuScene())

  override lazy val menu = SceneMenu(
    Map(
      "Make Planks" -> WoodworkingMenuScene(),
      "Go back"     -> CraftingMenuScene()
    )
  )
end WoodworkingMenuScene

class StonecuttingMenuScene extends MenuScene:
  override val name = "Stonecutting"

  override def previousScene = Some(CraftingMenuScene())

  override lazy val menu = SceneMenu(
    Map(
      "Cut Stone" -> StonecuttingMenuScene(),
      "Go back"   -> CraftingMenuScene()
    )
  )
end StonecuttingMenuScene

class DungeoningMenuScene extends MenuScene:
  override val name        = "Dungeoning"
  override val description = "Enter the dungeon and face the unknown."

  override def previousScene = Some(WorldMenuScene())

  override lazy val menu = SceneMenu(
    Map(
      "Enter Dungeon" -> DungeoningMenuScene(),
      "Go back"       -> WorldMenuScene()
    )
  )
end DungeoningMenuScene
