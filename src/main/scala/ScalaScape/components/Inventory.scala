package ScalaScape.components

case class InventoryItem(name: String, quantity: Int)

class Inventory(var items: Map[String, InventoryItem]) {
  def render(p: Pos): TerminalParagraph =
    TerminalParagraph(
      List(
        TerminalString("Inventory", Pos(p.x, p.y)),
        TerminalString("---------", Pos(p.x, p.y + 1))
      )
        ++ items.zipWithIndex.flatMap { case ((name, item), i) =>
          List(TerminalString(s"$name: ${item.quantity}", Pos(p.x, p.y + 2 + i)))
        }
    )
  end render
}
