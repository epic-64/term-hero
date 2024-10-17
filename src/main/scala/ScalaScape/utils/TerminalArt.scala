package ScalaScape.utils

import ScalaScape.components.{Position, TerminalString}
import com.googlecode.lanterna.TextColor

object TerminalArt:
  def parseWithoutColorMap(artString: String, position: Position): List[TerminalString] =
    val artLines = artString.split("\n").map(_.toCharArray)

    val sequence = for
      y <- artLines.indices
      x <- artLines(y).indices
    yield TerminalString(artLines(y)(x).toString, Position(x + position.x, y + position.y), TextColor.ANSI.WHITE)

    sequence.toList
  end parseWithoutColorMap

  def parseWithColorMap(
      artString: String,
      colorString: String,
      position: Position,
      colorMap: Map[Char, TextColor]
  ): List[TerminalString] =
    // Split the art and color strings into 2D arrays (lists of lists)
    val artLines   = artString.split("\n").map(_.toCharArray)
    val colorLines = colorString.split("\n").map(_.toCharArray)

    // Ensure both arrays have the same number of rows
    require(artLines.length == colorLines.length, "Art and color map must have the same number of lines")

    // Iterate over the arrays and build TerminalString objects
    val sequence = for
      y <- artLines.indices
      x <- artLines(y).indices
    yield {
      val artChar   = artLines(y)(x).toString
      val colorChar = colorLines(y)(x)

      // Use the color from the map if present, otherwise default to WHITE
      val color = colorMap.getOrElse(colorChar, TextColor.ANSI.WHITE)

      TerminalString(artChar, Position(x + position.x, y + position.y), color)
    }

    sequence.toList
  end parseWithColorMap
end TerminalArt
