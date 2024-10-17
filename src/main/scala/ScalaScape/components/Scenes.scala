package ScalaScape.components

import com.googlecode.lanterna.input.KeyStroke

trait Scene:
  val name: String
  lazy val menu: SceneMenu

  // default methods
  def handleInput(key: KeyStroke, state: GameState): GameState =
    menu.handleInput(key, state)
  end handleInput

  def asciiArt: TerminalParagraph =
    TerminalParagraph(List(TerminalString("No ASCII art available", Pos(0, 0))))

  // abstract methods
  def update(state: GameState): GameState                   = state
  def render(state: GameState, pos: Pos): TerminalParagraph =
    val sceneName = TerminalParagraph(
      List(
        TerminalString(name, pos),
        TerminalString("-" * 40, Pos(pos.x, pos.y + 1))
      )
    )

    val menuStr: TerminalParagraph = menu.render(Pos(pos.x, pos.y + 2))
    
    TerminalParagraph(sceneName.list ++ menuStr.list)
end Scene

class WorldScene extends Scene:
  override val name      = "World"
  override lazy val menu = SceneMenu(
    Map(
      "Gathering" -> GatheringScene(),
      "Crafting"  -> CraftingScene()
    )
  )
end WorldScene

// Gathering
class GatheringScene extends Scene:
  override val name      = "Gathering"
  override lazy val menu = SceneMenu(
    Map(
      "Woodcutting" -> WoodCuttingScene(),
      "Mining"      -> MiningScene(),
      "Back"        -> WorldScene()
    )
  )
end GatheringScene

class WoodCuttingScene extends Scene:
  override val name      = "Woodcutting"
  override lazy val menu = SceneMenu(
    Map(
      "Chop" -> WoodCuttingScene(),
      "Back" -> GatheringScene()
    )
  )
end WoodCuttingScene

class MiningScene extends Scene:
  override val name      = "Mining"
  override lazy val menu = SceneMenu(
    Map(
      "Mine" -> MiningScene(),
      "Back" -> GatheringScene()
    )
  )
end MiningScene

// Crafting
class CraftingScene extends Scene:
  override val name      = "Crafting"
  override lazy val menu = SceneMenu(
    Map(
      "Woodworking"  -> WoodworkingScene(),
      "Stonecutting" -> StonecuttingScene(),
      "Back"         -> WorldScene()
    )
  )
end CraftingScene

class WoodworkingScene extends Scene:
  override val name      = "Woodworking"
  override lazy val menu = SceneMenu(
    Map(
      "Make Planks" -> WoodworkingScene(),
      "Back" -> CraftingScene()
    )
  )
end WoodworkingScene

class StonecuttingScene extends Scene:
  override val name      = "Stonecutting"
  override lazy val menu = SceneMenu(
    Map(
      "Cut Stone" -> StonecuttingScene(),
      "Back" -> CraftingScene()
    )
  )
end StonecuttingScene
