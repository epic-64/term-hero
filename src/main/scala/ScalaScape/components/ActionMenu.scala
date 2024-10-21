package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

case class ActionItem(isSelectable: Boolean, action: (state: GameState) => GameState)

class ActionMenu(val items: Map[ColorLine, ActionItem]):
  private var selected: Int = 0

  def getSelectedItem: ActionItem = items.values.toList(selected)

  def handleInput(key: KeyStroke, state: GameState): GameState =
    key.getKeyType match
      case KeyType.ArrowUp                              => up(); state
      case KeyType.ArrowDown                            => down(); state
      case KeyType.Enter                                => activateItem(state)
      case KeyType.Character if key.getCharacter == ' ' => activateItem(state)
      case KeyType.ArrowRight                           => activateItem(state)
      case _                                            => state
  end handleInput

  def activateItem(state: GameState): GameState =
    if getSelectedItem.isSelectable
    then getSelectedItem.action(state)
    else state

  def render(pos: Pos): RenderBlock =
    val formattedItems = items.zipWithIndex.map { case (item, index) =>
      // deconstruct the tuple
      val (colorLine: ColorLine, actionItem: ActionItem) = item

      val selectIndicator = if index == selected then s"> " else s"  "
      var newColorLine    = ColorLine(selectIndicator) ++ colorLine

      newColorLine =
        if index == selected && actionItem.isSelectable
        then newColorLine.bolden()
        else newColorLine

      // reconstruct new tuple
      (newColorLine, actionItem)
    }

    val list = formattedItems.zipWithIndex.flatMap { case (line, index) =>
      val (colorLine: ColorLine, _) = line

      colorLine.render(Pos(pos.x, pos.y + index))
    }.toList

    RenderBlock(list)
  end render

  private def up(): ActionMenu =
    selected = (selected - 1 + items.size) % items.size
    this
  end up

  private def down(): ActionMenu =
    selected = (selected + 1) % items.size
    this
  end down
end ActionMenu
