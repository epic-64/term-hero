package ScalaScape.utils

import ScalaScape.{Position, TerminalString}
import com.googlecode.lanterna.TextColor

object TerminalArt:
  def parse(string: String, colorMap: Map[String, TextColor], position: Position): List[TerminalString] =
    val lines = string.split("\n")
    lines.zipWithIndex.flatMap { case (line, yOffset) =>
      processLine(line, position, yOffset, colorMap)
    }.toList
  end parse

  private def processLine(line: String, position: Position, yOffset: Int, colorMap: Map[String, TextColor]): List[TerminalString] =
    val colorTagRegex = """<c(\d+):([^>]+)>""".r
    var xPos          = position.x
    var remainingLine = line

    var parsedStrings = List[TerminalString]()

    while remainingLine.nonEmpty do
      colorTagRegex.findFirstMatchIn(remainingLine) match
        case Some(m) =>
          val (preTagText, coloredString) = processMatch(m, remainingLine, position, xPos, yOffset, colorMap)
          if preTagText.nonEmpty then
            parsedStrings ::= preTagText
            xPos += preTagText.content.length
          parsedStrings ::= coloredString
          xPos += coloredString.content.length
          remainingLine = remainingLine.substring(m.end)
        case None    =>
          if remainingLine.nonEmpty then
            // Add the remaining line without trimming or shifting special characters
            parsedStrings ::= TerminalString(
              remainingLine,
              Position(xPos, position.y + yOffset),
              TextColor.ANSI.DEFAULT
            )
            xPos += remainingLine.length
            remainingLine = "" // Stop loop as we've processed the rest

    parsedStrings.reverse // Ensure correct order
  end processLine

  private def processMatch(
      m: scala.util.matching.Regex.Match,
      remainingLine: String,
      position: Position,
      xPos: Int,
      yOffset: Int,
      colorMap: Map[String, TextColor]
  ): (TerminalString, TerminalString) =
    val (preTagText, colorKey, text) = (
      remainingLine.substring(0, m.start),
      m.group(1), // Color code
      m.group(2)  // Text inside tag
    )

    // Handle text before the color tag
    val preTextString =
      if preTagText.nonEmpty then
        TerminalString(preTagText, Position(xPos, position.y + yOffset), TextColor.ANSI.DEFAULT)
      else TerminalString("", Position(xPos, position.y + yOffset), TextColor.ANSI.DEFAULT)

    // Process the colored text based on the color map
    val coloredString = TerminalString(
      text,
      Position(xPos + preTagText.length, position.y + yOffset),
      colorMap.getOrElse(s"c$colorKey", TextColor.ANSI.DEFAULT)
    )
    (preTextString, coloredString)
  end processMatch
end TerminalArt
