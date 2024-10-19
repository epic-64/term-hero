package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics

case class Pos(x: Int, y: Int)
case class TerminalString(content: String, position: Pos, color: TextColor = DEFAULT)


case class LineWord(content: String, color: TextColor = TextColor.ANSI.DEFAULT)

case class TerminalLine(words: List[LineWord], pos: Pos):
  def toParagraph: TerminalParagraph =
    val x = pos.x
    
    // create a list of TerminalString objects, each one offset by the length of the previous string
    val terminalStrings = words.zipWithIndex.map { case (word, index) =>
      val offset = words.take(index).map(_.content.length).sum
      val newPos = Pos(x + offset, pos.y)
      TerminalString(word.content, newPos, word.color)
    }
    
    TerminalParagraph(terminalStrings)
  end toParagraph
end TerminalLine

case class TerminalParagraph(list: List[TerminalString]):
  def ++(other: TerminalParagraph | List[TerminalString] | TerminalString): TerminalParagraph =
    other match
      case p: TerminalParagraph    => TerminalParagraph(list ++ p.list)
      case l: List[TerminalString] => TerminalParagraph(list ++ l)
      case s: TerminalString       => TerminalParagraph(list :+ s)
    end match
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
    filledChar: Char = '#',
    emptyChar: Char = ':'
)

object ProgressBar:
  def from(par: ProgressBarParameters): List[TerminalString] =
    val x             = par.position.x
    val y             = par.position.y
    val filledLength  = (par.progress * par.width).toInt
    val filledSection = (1 to filledLength).map(_ => par.filledChar).mkString
    val emptySection  = ((1 + filledLength) to par.width).map(_ => ":").mkString

    List(
      TerminalString(filledSection, par.position, par.color),
      TerminalString(emptySection, Pos(x + filledLength, y), TextColor.ANSI.BLACK_BRIGHT)
    )
  end from
end ProgressBar
