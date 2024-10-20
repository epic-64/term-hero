package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics

case class Pos(x: Int, y: Int)

case class RenderString(content: String, position: Pos, color: TextColor = DEFAULT)

case class ColorWord(content: String, color: TextColor = DEFAULT)

class ColorLine(val words: List[ColorWord]):
  def this(content: String, color: TextColor = DEFAULT) = this(List(ColorWord(content, color)))

  def bolden(): ColorLine =
    val boldColor = WHITE_BRIGHT
    val boldWords = words.map { word =>
      ColorWord(word.content, boldColor)
    }

    ColorLine(boldWords)
  end bolden

  def render(pos: Pos): List[RenderString] =
    val x = pos.x
    
    // create a list of TerminalString objects, each one offset by the length of the previous string
    words.zipWithIndex.map { case (word, index) =>
      val offset = words.take(index).map(_.content.length).sum
      val newPos = Pos(x + offset, pos.y)
      RenderString(word.content, newPos, word.color)
    }
  end render

  def ++(other: ColorLine | List[ColorWord] | ColorWord): ColorLine =
    other match
      case l: ColorLine => ColorLine(words ++ l.words)
      case l: List[ColorWord] => ColorLine(words ++ l)
      case w: ColorWord => ColorLine(words :+ w)
    end match
  end ++
end ColorLine

case class RenderBlock(list: List[RenderString]):
  def ++(other: RenderBlock | List[RenderString] | RenderString): RenderBlock =
    other match
      case p: RenderBlock => RenderBlock(list ++ p.list)
      case l: List[RenderString] => RenderBlock(list ++ l)
      case s: RenderString => RenderBlock(list :+ s)
    end match
  end ++

  def draw(graphics: TextGraphics): Unit =
    list.foreach { terminalString =>
      graphics.setForegroundColor(terminalString.color)
      graphics.putString(terminalString.position.x, terminalString.position.y, terminalString.content)
      graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    }
  end draw
end RenderBlock

case class ProgressBarParameters(
    width: WidthInColumns,
    progress: Between0And1,
    position: Pos,
    color: TextColor,
    filledChar: Char = '#',
    emptyChar: Char = ':'
)

object ProgressBar:
  def from(par: ProgressBarParameters): List[RenderString] =
    val x             = par.position.x
    val y             = par.position.y
    val filledLength  = (par.progress * par.width).toInt
    val filledSection = (1 to filledLength).map(_ => par.filledChar).mkString
    val emptySection  = ((1 + filledLength) to par.width).map(_ => ":").mkString

    List(
      RenderString(filledSection, par.position, par.color),
      RenderString(emptySection, Pos(x + filledLength, y), TextColor.ANSI.BLACK_BRIGHT)
    )
  end from
end ProgressBar
