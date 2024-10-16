package ScalaScape.components

class InventoryDisplay:
  def render(state: GameState, position: Pos): TerminalParagraph =
    TerminalParagraph(
      List(
        TerminalString("Inventory", Pos(position.x, position.y)),
        TerminalString("---------", Pos(position.x, position.y + 1))
      )
        ++ state.inventory.zipWithIndex.map { case ((item, count), index) =>
          TerminalString(s"$item: $count", Pos(position.x, position.y + 2 + index))
        }
    )
  end render
end InventoryDisplay
