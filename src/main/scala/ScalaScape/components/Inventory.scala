package ScalaScape.components

import ScalaScape.utils.AsList

class Inventory:
  val items = new InventoryItems()

  def render(p: Pos): RenderedBlock =
    val getLabels: List[RenderString] =
      val labels = items
        .asList[InventoryItem]
        .filter(_.quantity > 0)
        .sortBy(_.quantity)
        .reverse

      labels match {
        case Nil => List(RenderString("Bag is empty", Pos(p.x, p.y + 2)))
        case _   =>
          labels.zipWithIndex.map((item, index) =>
            RenderString(s"${item.name}: ${item.quantity}", Pos(p.x, p.y + 2 + index))
          )
      }
    end getLabels

    val header = List(
      RenderString("Inventory", Pos(p.x, p.y)),
      RenderString("---------", Pos(p.x, p.y + 1))
    )

    RenderedBlock(header ++ getLabels)
  end render
end Inventory

class InventoryItem(val name: String, var quantity: Int)

class InventoryItems extends AsList:
  val oak: InventoryItem    = new InventoryItem("Oak", 0)
  val teak: InventoryItem   = new InventoryItem("Teak", 0)
  val acorn: InventoryItem  = new InventoryItem("Acorn", 0)
  val planks: InventoryItem = new InventoryItem("Planks", 0)
  val nails: InventoryItem  = new InventoryItem("Nails", 0)
end InventoryItems
