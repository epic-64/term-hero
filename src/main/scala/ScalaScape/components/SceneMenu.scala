package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

class SceneMenu(val items: Map[String, Scene]):
  private var selected: Int = 0

  def handleInput(key: KeyStroke): SceneMenu =
    key.getKeyType match
      case KeyType.ArrowUp                              => up()
      case KeyType.ArrowDown                            => down()
      case KeyType.Enter                                => activateItem
      case KeyType.Character if key.getCharacter == ' ' => activateItem
      case _                                            => this
  end handleInput

  def activateItem: SceneMenu =
    println(s"Activating item: ${items.keys.toList(selected)}")
    this
  end activateItem

  def render: TerminalParagraph =
    val menuItems = items.keys.toList.zipWithIndex.map { case (item, index) =>
      if index == selected then TerminalString(s"> $item", Pos(0, index), WHITE_BRIGHT)
      else TerminalString(s"  $item", Pos(0, index))
    }

    TerminalParagraph(menuItems)
  end render

  private def up(): SceneMenu =
    selected = (selected - 1) % items.size
    this
  end up

  private def down(): SceneMenu =
    selected = (selected + 1) % items.size
    this
  end down
end SceneMenu
