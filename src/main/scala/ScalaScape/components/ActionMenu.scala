package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

case class ActionItem(isSelectable: Boolean, label: ColorLine, action: (state: GameState) => GameState):
  def isLocked: Boolean = !isSelectable

class ActionMenu(val getItems: () => List[ActionItem]):
  private var selected: Int = 0

  def getSelectedItem: ActionItem = getItems()(selected)

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

  def render(pos: Pos): RenderedBlock =
    val formattedItems = getItems().zipWithIndex.map { case (item, index) =>
      val selectIndicator = if index == selected then s"> " else s"  "
      var newColorLine    = ColorLine(selectIndicator) ++ item.label

      newColorLine =
        if index == selected && item.isSelectable
        then newColorLine.bolden()
        else newColorLine

      newColorLine =
        if item.isLocked
        then newColorLine.darken()
        else newColorLine

      ActionItem(item.isSelectable, newColorLine, item.action)
    }

    val list = formattedItems.zipWithIndex.flatMap { case (item, index) =>
      item.label.render(Pos(pos.x, pos.y + index))
    }

    RenderedBlock(list)
  end render

  private def up(): ActionMenu =
    selected = (selected - 1 + getItems().size) % getItems().size
    this
  end up

  private def down(): ActionMenu =
    selected = (selected + 1) % getItems().size
    this
  end down
end ActionMenu
