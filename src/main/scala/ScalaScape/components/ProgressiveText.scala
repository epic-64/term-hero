package ScalaScape.components

import com.googlecode.lanterna.{SGR, TextColor}

class ProgressiveText(content: String, framesPerCharacter: Int = 5):
  // render the text progressively, adding one character at a time

  private var frameCount = 0
  private var currentText = ""
  private var currentIndex = 0

  def update(): ProgressiveText =
      if currentIndex == content.length then return this

      frameCount += 1

      if frameCount % framesPerCharacter == 0 then
        currentText += content(currentIndex)
        currentIndex += 1
      end if

      this
  end update

  def render(pos: Pos): RenderedBlock =
      // make the LAST character bold
      if currentText.isEmpty then return RenderedBlock.empty

      val lastChar = ColorWord(currentText.last.toString, TextColor.ANSI.WHITE_BRIGHT, Some(SGR.BOLD))
      val line = ColorLine(List(ColorWord(currentText.dropRight(1)), lastChar))

      RenderedBlock(line.render(pos))
  end render
end ProgressiveText

