package ScalaScape.components

import ScalaScape.utils.TerminalArt
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.KeyStroke

trait Scene:
  val name: String
  val description: String = "No description available"

  lazy val menu: SceneMenu

  // default methods
  def handleInput(key: KeyStroke, state: GameState): GameState =
    menu.handleInput(key, state)
  end handleInput

  def asciiArt(pos: Pos): TerminalParagraph =
    TerminalParagraph(List(TerminalString("No ASCII art available", pos)))

  // abstract methods
  def update(state: GameState): GameState                   = state
  def render(state: GameState, pos: Pos): TerminalParagraph =
    val sceneName = List(
      TerminalString(name, pos, WHITE_BRIGHT),
      TerminalString("-" * 40, Pos(pos.x, pos.y + 1))
    )

    val asciiArt: List[TerminalString] = this.asciiArt(Pos(pos.x, pos.y + 2)).list
      ++ List(TerminalString("-" * 40, Pos(pos.x, pos.y + 12)))

    val descriptionStr = List(
      TerminalString(description, Pos(pos.x, pos.y + 13))
    )

    val menuStr: TerminalParagraph = menu.render(Pos(pos.x, pos.y + 15))

    TerminalParagraph(sceneName ++ asciiArt ++ descriptionStr ++ menuStr.list)
end Scene

class WorldScene extends Scene:
  override val name                = "World"
  override val description: String = "The world is your oyster."

  override lazy val menu = SceneMenu(
    Map(
      "Gathering"  -> GatheringScene(),
      "Crafting"   -> CraftingScene(),
      "Dungeoning" -> DungeoningScene()
    )
  )
end WorldScene

// Gathering
class GatheringScene extends Scene:
  override val name      = "World > Gathering"
  override lazy val menu = SceneMenu(
    Map(
      "Woodcutting" -> WoodCuttingScene(),
      "Mining"      -> MiningScene(),
      "Go back"     -> WorldScene()
    )
  )
end GatheringScene

class WoodCuttingScene extends Scene:
  override val name      = "World > Gathering > Woodcutting"
  override lazy val menu = SceneMenu(
    Map(
      "Chop"    -> WoodCuttingScene(),
      "Go back" -> GatheringScene()
    )
  )

  override def asciiArt(pos: Pos): TerminalParagraph = WoodCuttingArtwork(pos)
end WoodCuttingScene

class MiningScene extends Scene:
  override val name      = "World > Gathering > Mining"
  override lazy val menu = SceneMenu(
    Map(
      "Mine"    -> MiningScene(),
      "Go back" -> GatheringScene()
    )
  )
end MiningScene

// Crafting
class CraftingScene extends Scene:
  override val name      = "World > Crafting"
  override lazy val menu = SceneMenu(
    Map(
      "Woodworking"  -> WoodworkingScene(),
      "Stonecutting" -> StonecuttingScene(),
      "Go back"      -> WorldScene()
    )
  )
end CraftingScene

class WoodworkingScene extends Scene:
  override val name      = "World > Crafting > Woodworking"
  override lazy val menu = SceneMenu(
    Map(
      "Make Planks" -> WoodworkingScene(),
      "Go back"     -> CraftingScene()
    )
  )
end WoodworkingScene

class StonecuttingScene extends Scene:
  override val name      = "World > Crafting > Stonecutting"
  override lazy val menu = SceneMenu(
    Map(
      "Cut Stone" -> StonecuttingScene(),
      "Go back"   -> CraftingScene()
    )
  )
end StonecuttingScene

class DungeoningScene extends Scene:
  override val name        = "World > Dungeoning"
  override val description = "Enter the dungeon and face the unknown."

  override lazy val menu = SceneMenu(
    Map(
      "Enter Dungeon" -> DungeoningScene(),
      "Go back"       -> WorldScene()
    )
  )
end DungeoningScene
