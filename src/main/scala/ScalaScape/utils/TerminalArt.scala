package ScalaScape.utils

import ScalaScape.{Position, TerminalString}
import com.googlecode.lanterna.TextColor

object TerminalArt:
  private val colorMap = Map(
    "c1" -> TextColor.ANSI.GREEN_BRIGHT,
    "c2" -> TextColor.ANSI.GREEN,
    "c3" -> TextColor.ANSI.BLUE_BRIGHT,
    "c4" -> TextColor.ANSI.BLUE,
    "c5" -> TextColor.ANSI.RED_BRIGHT,
    "c6" -> TextColor.ANSI.RED,
    "c7" -> TextColor.ANSI.YELLOW_BRIGHT,
    "c8" -> TextColor.ANSI.YELLOW,
    "c9" -> TextColor.ANSI.CYAN_BRIGHT,
    "c10" -> TextColor.ANSI.CYAN,
    "c11" -> TextColor.ANSI.MAGENTA_BRIGHT,
    "c12" -> TextColor.ANSI.MAGENTA,
  )

  def parse(string: String, position: Position): List[TerminalString] =
    val lines = string.stripMargin.split("\n")
    lines.zipWithIndex.flatMap { case (line, yOffset) => processLine(line, position, yOffset) }.toList
  end parse

  private def processLine(line: String, position: Position, yOffset: Int): List[TerminalString] =
    val colorTagRegex = """<c(\d+):([^>]+)>""".r
    var xPos          = position.x
    var remainingLine = line
    var parsedStrings = List[TerminalString]()

    while remainingLine.nonEmpty do
      colorTagRegex.findFirstMatchIn(remainingLine) match
        case Some(m) =>
          val (preTagText, coloredString) = processMatch(m, remainingLine, position, xPos, yOffset)
          if preTagText.nonEmpty then
            parsedStrings ::= preTagText
            xPos += preTagText.content.length
          parsedStrings ::= coloredString
          xPos += coloredString.content.length
          remainingLine = remainingLine.substring(m.end)
        case None    =>
          if remainingLine.nonEmpty then
            parsedStrings ::= TerminalString(
              remainingLine,
              Position(xPos, position.y + yOffset),
              TextColor.ANSI.DEFAULT
            )
            remainingLine = "" // Stop loop as we've processed the rest

    parsedStrings.reverse // Ensure correct order
  end processLine

  private def processMatch(
    m: scala.util.matching.Regex.Match,
    remainingLine: String,
    position: Position,
    xPos: Int,
    yOffset: Int
  ): (TerminalString, TerminalString) =
    val (preTagText, colorKey, text) = (
      remainingLine.substring(0, m.start),
      m.group(1), // Color code
      m.group(2)  // Text inside tag
    )

    val preTextString =
      if preTagText.nonEmpty
      then TerminalString(preTagText, Position(xPos, position.y + yOffset), TextColor.ANSI.DEFAULT)
      else TerminalString("", Position(xPos, position.y + yOffset), TextColor.ANSI.DEFAULT)

    val coloredString = TerminalString(
      text,
      Position(xPos + preTagText.length, position.y + yOffset),
      colorMap.getOrElse(s"c$colorKey", TextColor.ANSI.DEFAULT)
    )

    (preTextString, coloredString)
  end processMatch
end TerminalArt
