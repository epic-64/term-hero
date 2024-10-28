package ScalaScape.components

case class ActivityLogItem(content: ProgressiveText)

class EventLog(private val capacity: Int):
  private var items: List[ActivityLogItem] = List()

  def add(message: String)(implicit state: GameState): EventLog = {
    state.forceClearScreen = true

    val progressiveText = new ProgressiveText(message)

    items = ActivityLogItem(progressiveText) :: items

    if (items.length > capacity) {
      items = items.dropRight(1)
    }
    
    this
  }

  def render(pos: Pos): RenderedBlock = {
    val x = pos.x
    val y = pos.y

    val renderedStrings = items.reverse.zipWithIndex.map { case (item, index) =>
      item.content.render(Pos(x, y + 2 + index))
    }

    RenderedBlock(
      List(
        RenderString("Activity Log", Pos(x, y)),
        RenderString("-" * 25, Pos(x, y + 1))
      )
    ) ++ renderedStrings.foldLeft(RenderedBlock.empty)(_ ++ _)
  }
end EventLog
