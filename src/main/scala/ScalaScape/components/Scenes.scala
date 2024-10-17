package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

trait Scene:
  val name: String
  lazy val menu: SceneMenu

  // default methods
  def handleInput(key: KeyStroke, state: GameState): GameState =
    menu.handleInput(key, state)
  end handleInput

  // abstract methods
  def update(state: GameState): GameState                   = state
  def render(state: GameState, pos: Pos): TerminalParagraph = menu.render
end Scene

class WorldScene extends Scene:
  override val name = "World"
  override lazy val menu = SceneMenu(
    Map(
      "Gathering" -> GatheringScene(),
      "Crafting"  -> CraftingScene()
    )
  )
end WorldScene

// Gathering
class GatheringScene extends Scene:
  override val name = "Gathering"
  override lazy val menu = SceneMenu(
    Map(
      "Woodcutting" -> WoodCuttingScene(),
      "Mining"      -> MiningScene()
    )
  )
end GatheringScene

class WoodCuttingScene extends Scene:
  override val name = "Woodcutting"
  override lazy val menu = SceneMenu(
    Map(
      "Back" -> GatheringScene()
    )
  )
end WoodCuttingScene

class MiningScene extends Scene:
  override val name = "Mining"
  override lazy val menu = SceneMenu(
    Map(
      "Back" -> GatheringScene()
    )
  )
end MiningScene

// Crafting
class CraftingScene extends Scene:
  override val name = "Crafting"
  override lazy val menu = SceneMenu(
    Map(
      "Woodworking"  -> WoodworkingScene(),
      "Stonecutting" -> StonecuttingScene()
    )
  )
end CraftingScene

class WoodworkingScene extends Scene:
  override val name = "Woodworking"
  override lazy val menu = SceneMenu(
    Map(
      "Back" -> CraftingScene()
    )
  )
end WoodworkingScene

class StonecuttingScene extends Scene:
  override val name = "Stonecutting"
  override lazy val menu = SceneMenu(
    Map(
      "Back" -> CraftingScene()
    )
  )
end StonecuttingScene
