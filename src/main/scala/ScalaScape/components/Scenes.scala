package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

abstract class Scene:
  val name: String
  val description: String = "No description available"

  def renderHeader(pos: Pos): TerminalParagraph =
    TerminalParagraph(List(TerminalString(description, pos, WHITE)))
      ++ TerminalParagraph(List(TerminalString("-" * 40, Pos(pos.x, pos.y + 1), WHITE)))
      ++ asciiArt(Pos(pos.x, pos.y + 2))
      ++ TerminalParagraph(List(TerminalString("-" * 40, Pos(pos.x, pos.y + 12), WHITE)))
      ++ TerminalParagraph(List(TerminalString(name, Pos(pos.x, pos.y + 13), WHITE)))
  end renderHeader

  def handleInput(key: KeyStroke, state: GameState): GameState =
    key.getKeyType match {
      case KeyType.Escape => state.swapScene(previousScene);
      case _              =>
    }

    typeHandleInput(key, state)
  end handleInput

  def update(state: GameState): GameState =
    // add shared logic for all scenes here
    typeUpdate(state) // additional scene specific logic

  def render(state: GameState, pos: Pos): TerminalParagraph =
    renderHeader(pos)
      ++ typeRender(state, Pos(pos.x, pos.y + 14))
  end render

  def asciiArt(pos: Pos): TerminalParagraph = TerminalParagraph(List(TerminalString("No ASCII art available", pos)))
  def previousScene: Scene

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

class WoodCuttingOakScene() extends Scene:
  override val name        = "World > Gathering > Woodcutting > Oak"
  override val description = "Cut down some oak trees."
  // var theSkill = state.skills.woodCuttingOak

  override def asciiArt(pos: Pos): TerminalParagraph = WoodCuttingArtwork(pos)
  override def previousScene: Scene = WoodCuttingMenuScene()
  
  override def typeUpdate(state: GameState): GameState = {
    state.skills.woodCuttingOak.update(state)
  }
  
  override def typeRender(state: GameState, pos: Pos): TerminalParagraph = {
    state.skills.woodCuttingOak.render(Pos(pos.x, pos.y + 1))
  }
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
