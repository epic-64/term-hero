package ScalaScape.utils

import ScalaScape.{Position, TerminalString}
import com.googlecode.lanterna.TextColor

object TerminalArt:
  def parse(artString: String, colorString: String, position: Position, colorMap: Map[Char, TextColor]): List[TerminalString] =
    // Split the art and color strings into 2D arrays (lists of lists)
    val artLines = artString.split("\n").map(_.toCharArray).toArray
    val colorLines = colorString.split("\n").map(_.toCharArray).toArray

    // Handle cases where the color array is smaller or malformed by using a default color '0' (black)
    val defaultColorChar = '0'

    // Iterate over the arrays and build TerminalString objects
    (for
      y <- artLines.indices
      x <- artLines(y).indices
    yield {
      val artChar = artLines(y)(x).toString
      // Safely retrieve the color character, defaulting to '0' (black) if the color string is too short
      val colorChar = if y < colorLines.length && x < colorLines(y).length then colorLines(y)(x) else defaultColorChar
      val color = colorMap.getOrElse(colorChar, TextColor.ANSI.BLACK)
      TerminalString(artChar, Position(x + position.x, y + position.y), color)
    }).toList
  end parse

end TerminalArt
