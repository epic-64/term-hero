package ScalaScape.components

case class ActivityLogItem(message: String)

class ActivityLog {
  private val maxItems: Int                = 20
  private var items: List[ActivityLogItem] = List()

  def add(message: String): ActivityLog = {
    items = ActivityLogItem(message) :: items

    if (items.length > maxItems) {
      items = items.dropRight(1)
    }
    
    this
  }

  def render(pos: Pos): RenderBlock = {
    val x = pos.x
    val y = pos.y

    RenderBlock(
      List(
        RenderString("Activity Log", Pos(x, y)),
        RenderString("-" * 25, Pos(x, y + 1))
      ) ++ items.reverse.zipWithIndex.map { case (item, index) =>
        RenderString(item.message, Pos(x, y + 2 + index))
      }
    )
  }
}
