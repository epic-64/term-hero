package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

abstract class Scene:
  val name: String

  def renderName(pos: Pos): TerminalParagraph = TerminalParagraph(List(TerminalString(name, pos, WHITE_BRIGHT)))

  def handleInput(key: KeyStroke, state: GameState): GameState =
    key.getKeyType match {
      case KeyType.Escape =>
        state.selectedScene = previousScene
        state
      case _              => state
    }
  end handleInput

  def update(state: GameState): GameState
  def render(state: GameState, pos: Pos): TerminalParagraph
  def asciiArt(pos: Pos): TerminalParagraph = TerminalParagraph(List(TerminalString("No ASCII art available", pos)))
  def previousScene: Scene
end Scene

abstract class MenuScene extends Scene:
  val description: String = "No description available"

  lazy val menu: SceneMenu

  override def handleInput(key: KeyStroke, state: GameState): GameState = menu.handleInput(key, state)
  override def update(state: GameState): GameState                      = state
  override def render(state: GameState, pos: Pos): TerminalParagraph    =
    val sceneName = renderName(pos).list
      ++ List(TerminalString("-" * 40, Pos(pos.x, pos.y + 1)))

    val asciiArt: List[TerminalString] = this.asciiArt(Pos(pos.x, pos.y + 2)).list
      ++ List(TerminalString("-" * 40, Pos(pos.x, pos.y + 12)))

    val descriptionStr = List(
      TerminalString(description, Pos(pos.x, pos.y + 13))
    )

    val menuStr: TerminalParagraph = menu.render(Pos(pos.x, pos.y + 15))

    TerminalParagraph(sceneName ++ asciiArt ++ descriptionStr ++ menuStr.list)
  end render
end MenuScene

class WorldMenuScene extends MenuScene:
  override val name                = "World"
  override val description: String = "The world is your oyster."

  override def previousScene: Scene                  = this
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
  override val name = "World > Gathering"

  override def previousScene: Scene = WorldMenuScene()

  override lazy val menu = SceneMenu(
    Map(
      "Woodcutting" -> WoodCuttingMenuScene(),
      "Mining"      -> MiningMenuScene(),
      "Go back"     -> WorldMenuScene()
    )
  )
end GatheringMenuScene

class WoodCuttingMenuScene extends MenuScene:
  override val name = "World > Gathering > Woodcutting"

  override def previousScene: Scene = GatheringMenuScene()

  override lazy val menu = SceneMenu(
    Map(
      "Oak"     -> WoodCuttingOakScene(),
      "Go back" -> GatheringMenuScene()
    )
  )

  override def asciiArt(pos: Pos): TerminalParagraph = WoodCuttingArtwork(pos)
end WoodCuttingMenuScene

class WoodCuttingOakScene extends Scene:
  override val name = "World > Gathering > Woodcutting > Oak"

  override def handleInput(key: KeyStroke, state: GameState): GameState =
    super.handleInput(key, state)
    state
  end handleInput

  override def previousScene: Scene = WoodCuttingMenuScene()

  override def update(state: GameState): GameState =
    state.skills("Woodcutting").update(state)
    state
  end update

  override def render(state: GameState, pos: Pos): TerminalParagraph =
    renderName(pos)
      ++ state.skills("Woodcutting").render(Pos(pos.x, pos.y + 1))
  end render
end WoodCuttingOakScene

class MiningMenuScene extends MenuScene:
  override val name = "World > Gathering > Mining"

  override def previousScene: Scene = GatheringMenuScene()

  override lazy val menu = SceneMenu(
    Map(
      "Mine"    -> MiningMenuScene(),
      "Go back" -> GatheringMenuScene()
    )
  )
end MiningMenuScene

// Crafting
class CraftingMenuScene extends MenuScene:
  override val name = "World > Crafting"

  override def previousScene: Scene = WorldMenuScene()

  override lazy val menu = SceneMenu(
    Map(
      "Woodworking"  -> WoodworkingMenuScene(),
      "Stonecutting" -> StonecuttingMenuScene(),
      "Go back"      -> WorldMenuScene()
    )
  )
end CraftingMenuScene

class WoodworkingMenuScene extends MenuScene:
  override val name = "World > Crafting > Woodworking"

  override def previousScene: Scene = CraftingMenuScene()

  override lazy val menu = SceneMenu(
    Map(
      "Make Planks" -> WoodworkingMenuScene(),
      "Go back"     -> CraftingMenuScene()
    )
  )
end WoodworkingMenuScene

class StonecuttingMenuScene extends MenuScene:
  override val name = "World > Crafting > Stonecutting"

  override def previousScene: Scene = CraftingMenuScene()

  override lazy val menu = SceneMenu(
    Map(
      "Cut Stone" -> StonecuttingMenuScene(),
      "Go back"   -> CraftingMenuScene()
    )
  )
end StonecuttingMenuScene

class DungeoningMenuScene extends MenuScene:
  override val name        = "World > Dungeoning"
  override val description = "Enter the dungeon and face the unknown."

  override def previousScene: Scene = WorldMenuScene()

  override lazy val menu = SceneMenu(
    Map(
      "Enter Dungeon" -> DungeoningMenuScene(),
      "Go back"       -> WorldMenuScene()
    )
  )
end DungeoningMenuScene
