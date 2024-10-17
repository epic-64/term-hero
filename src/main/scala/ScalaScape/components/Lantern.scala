package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics

case class Pos(x: Int, y: Int)
case class TerminalString(content: String, position: Pos, color: TextColor = DEFAULT)

case class TerminalParagraph(list: List[TerminalString]):
  def ++(other: TerminalParagraph): TerminalParagraph =
    TerminalParagraph(list ++ other.list)
  end ++
  
  def draw(graphics: TextGraphics): Unit =
    list.foreach { terminalString =>
      graphics.setForegroundColor(terminalString.color)
      graphics.putString(terminalString.position.x, terminalString.position.y, terminalString.content)
      graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    }
  end draw
end TerminalParagraph

case class ProgressBarParameters(
    width: WidthInColumns,
    progress: Between0And1,
    position: Pos,
    color: TextColor,
    leftLimiter: String = "[",
    rightLimiter: String = "]"
)

object ProgressBar:
  def from(p: ProgressBarParameters): List[TerminalString] =
    val x             = p.position.x
    val y             = p.position.y
    val innerLength   = p.width - 2 // Reserve space for boundaries
    val filledLength  = (p.progress * innerLength).toInt
    val fillChar      = ':'
    val filledSection = (1 to filledLength).map(_ => fillChar).mkString
    val emptySection  = (1 to (p.width - innerLength)).map(_ => " ").mkString

    List(
      TerminalString(p.leftLimiter, Pos(x, y), WHITE),
      TerminalString(filledSection, Pos(x + 1, y), p.color),
      TerminalString(emptySection, Pos(x + 1 + filledLength, y), TextColor.ANSI.DEFAULT),
      TerminalString(p.rightLimiter, Pos(x + p.width - 1, y), WHITE)
    )
  end from
end ProgressBar
