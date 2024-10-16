package ScalaScape.components

class InventoryDisplay:
  def render(state: GameState, position: Position): TerminalParagraph =
    TerminalParagraph(
      List(
        TerminalString("Inventory", Position(position.x, position.y)),
        TerminalString("---------", Position(position.x, position.y + 1))
      )
        ++ state.inventory.zipWithIndex.map { case ((item, count), index) =>
          TerminalString(s"$item: $count", Position(position.x, position.y + 2 + index))
        }
    )
  end render
end InventoryDisplay
