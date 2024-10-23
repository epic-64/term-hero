package ScalaScape.components

case class ActivityLogItem(message: ProgressiveText)

class ActivityLog {
  private val maxItems: Int                = 20
  private var items: List[ActivityLogItem] = List()

  def add(message: String): ActivityLog = {
    val progressiveText = new ProgressiveText(message)

    items = ActivityLogItem(progressiveText) :: items

    if (items.length > maxItems) {
      items = items.dropRight(1)
    }
    
    this
  }

  def render(pos: Pos): RenderedBlock = {
    val x = pos.x
    val y = pos.y

    items.foreach(_.message.update())

    RenderedBlock(
      List(
        RenderString("Activity Log", Pos(x, y)),
        RenderString("-" * 25, Pos(x, y + 1))
      )
    ) ++ items.reverse.zipWithIndex.map { case (item, index) =>
      item.message.render(Pos(x, y + 2 + index)).strings match
        case Nil => RenderString("", Pos(x, y + 2 + index))
        case strings => RenderString(strings.map(_.content).mkString, Pos(x, y + 2 + index))
    }
  }
}
