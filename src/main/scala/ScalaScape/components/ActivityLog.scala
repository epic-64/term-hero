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

  def render(pos: Pos): TerminalParagraph = {
    val x = pos.x
    val y = pos.y

    val logItems = items.zipWithIndex.map { case (item, index) =>
      TerminalString(item.message, Pos(x, y + index))
    }

    TerminalParagraph(logItems)
  }
}
