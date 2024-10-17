package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

class SceneMenu(val items: Map[String, Scene]):
  private var selected: Int = 0

  def getSelectedScene: Scene = items.values.toList(selected)

  def handleInput(key: KeyStroke, state: GameState): GameState =
    key.getKeyType match
      case KeyType.ArrowUp                              =>
        up()
        state
      case KeyType.ArrowDown                            =>
        down()
        state
      case KeyType.Enter                                =>
        activateItem(state)
        state
      case KeyType.Character if key.getCharacter == ' ' =>
        activateItem(state)
        state
      case _                                            => state
  end handleInput

  def activateItem(state: GameState): GameState =
    state.selectedScene = getSelectedScene
    state
  end activateItem

  def render(pos: Pos): TerminalParagraph =
    val introString: List[TerminalString] = List(
      TerminalString("What would you like to do?", pos)
    )

    val menuItems = items.keys.toList.zipWithIndex.map { case (item, index) =>
      val newPos = Pos(pos.x, pos.y + 2 + index)
      val string = if index == selected then s"> $item" else s"  $item"

      TerminalString(string, newPos, if index == selected then WHITE_BRIGHT else WHITE)
    }

    TerminalParagraph(introString ++ menuItems)
  end render

  private def up(): SceneMenu =
    selected = (selected - 1 + items.size) % items.size
    this
  end up

  private def down(): SceneMenu =
    selected = (selected + 1) % items.size
    this
  end down
end SceneMenu
