package ScalaScape.components

case class InventoryItem(name: String, quantity: Int)

class Inventory {
  var items: Map[String, InventoryItem] = Map(
    "Oak" -> InventoryItem("Oak", 0),
    "Teak" -> InventoryItem("Teak", 0),
    "Willow" -> InventoryItem("Willow", 0),
    "Maple" -> InventoryItem("Maple", 0),
    "Yew" -> InventoryItem("Yew", 0),
    "Magic" -> InventoryItem("Magic", 0)
  )

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
