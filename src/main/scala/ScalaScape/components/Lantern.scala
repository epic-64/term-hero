package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.{SGR, TextColor}

case class Pos(x: Int, y: Int)

case class RenderString(content: String, position: Pos, color: TextColor = WHITE, modifier: Option[SGR] = None)

case class ColorWord(content: String, color: TextColor = WHITE, modifier: Option[SGR] = None):
  def bolden(): ColorWord = ColorWord(content, color, Some(SGR.BOLD))
  def darken(): ColorWord = ColorWord(content, BLACK_BRIGHT)

class ColorLine(val words: List[ColorWord]):
  def this(content: String, color: TextColor = WHITE) = this(List(ColorWord(content, color)))

  def bolden(): ColorLine = ColorLine(words.map(word => word.bolden()))
  def darken(): ColorLine = ColorLine(words.map(word => if word.color == WHITE then word.darken() else word))

  def render(pos: Pos): List[RenderString] =
    val x = pos.x

    // create a list of TerminalString objects, each one offset by the length of the previous string
    words.zipWithIndex.map { case (word, index) =>
      val offset = words.take(index).map(_.content.length).sum
      val newPos = Pos(x + offset, pos.y)
      RenderString(word.content, newPos, word.color, word.modifier)
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

case class RenderBlock(strings: List[RenderString]):
  def ++(other: RenderBlock | List[RenderString] | RenderString): RenderBlock =
    other match
      case p: RenderBlock => RenderBlock(strings ++ p.strings)
      case l: List[RenderString] => RenderBlock(strings ++ l)
      case s: RenderString => RenderBlock(strings :+ s)
    end match
  end ++

  def draw(graphics: TextGraphics): Unit =
    strings.foreach { item =>
      graphics.setForegroundColor(item.color)
      item.modifier match
        case Some(mod) => graphics.putString(item.position.x, item.position.y, item.content, mod)
        case None => graphics.putString(item.position.x, item.position.y, item.content)

      graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    }
  end draw
  
  def hasStringLike(content: String): Boolean = strings.exists(_.content.contains(content))
end RenderBlock

object RenderBlock:
  def empty = RenderBlock(List.empty)
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
