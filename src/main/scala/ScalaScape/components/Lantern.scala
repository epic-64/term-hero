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

  def ++(other: ColorLine): ColorLine       = ColorLine(words ++ other.words)
  def ++(other: ColorWord): ColorLine       = ColorLine(words :+ other)
  def ++(other: List[ColorLine]): ColorLine = ColorLine(words ++ other.flatMap(_.words))
end ColorLine

class RenderedBlock(val strings: List[RenderString]):
  def this(block: RenderedBlock) = this(block.strings)

  def ++(other: RenderedBlock | List[RenderString] | RenderString): RenderedBlock =
    other match
      case p: RenderedBlock      => RenderedBlock(strings ++ p.strings)
      case l: List[RenderString] => RenderedBlock(strings ++ l)
      case s: RenderString       => RenderedBlock(strings :+ s)
    end match
  end ++

  def draw(graphics: TextGraphics): Unit =
    strings.foreach { item =>
      graphics.setForegroundColor(item.color)
      item.modifier match
        case Some(mod) => graphics.putString(item.position.x, item.position.y, item.content, mod)
        case None      => graphics.putString(item.position.x, item.position.y, item.content)

      graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    }
  end draw

  def hasStringLike(content: String): Boolean = strings.exists(_.content.contains(content))
end RenderedBlock

object RenderedBlock:
  def empty = RenderedBlock(List.empty)
end RenderedBlock

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
