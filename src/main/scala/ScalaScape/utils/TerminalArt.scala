package ScalaScape.utils

import ScalaScape.ui.lantern.{Position, TerminalString}
import com.googlecode.lanterna.TextColor

object TerminalArt:
  def parse(
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
    val list = for
      y <- artLines.indices
      x <- artLines(y).indices
    yield {
      val artChar   = artLines(y)(x).toString
      val colorChar = colorLines(y)(x)

      // Use the color from the map if present, otherwise default to WHITE
      val color = colorMap.getOrElse(colorChar, TextColor.ANSI.WHITE)

      TerminalString(artChar, Position(x + position.x, y + position.y), color)
    }

    list.toList
  end parse
end TerminalArt
