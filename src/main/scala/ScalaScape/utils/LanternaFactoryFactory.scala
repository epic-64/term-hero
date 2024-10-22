package ScalaScape.utils

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.screen.{Screen, TerminalScreen}
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, Terminal}

import java.awt.{Font, GraphicsEnvironment}
import javax.swing.JFrame
import javax.swing.WindowConstants.EXIT_ON_CLOSE

object LanternaFactoryFactory {
  def makeScreen(forceTerminal: Boolean): Screen = {
    val terminal = makeTerminal(forceTerminal)
    new TerminalScreen(terminal)
  }
  
  def makeTerminal(forceTerminal: Boolean): Terminal = {
    def getFont(family: String, style: Int, size: Int): Font = {
      val availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
      if availableFonts.contains(family) then new Font(family, style, size)
      else new Font("Monospaced", style, size)
    }

    val terminalFactory = new DefaultTerminalFactory()
    val fontConfig = SwingTerminalFontConfiguration.newInstance(getFont("Consolas", Font.PLAIN, 20))
    terminalFactory.setInitialTerminalSize(new TerminalSize(120, 30))
    terminalFactory.setTerminalEmulatorFontConfiguration(fontConfig)
    terminalFactory.setPreferTerminalEmulator(!forceTerminal)
    terminalFactory.setForceTextTerminal(forceTerminal)
    terminalFactory.setTerminalEmulatorTitle("ScalaScape")
    
    terminalFactory.createTerminal() match {
      case frame: JFrame => frame.setDefaultCloseOperation(EXIT_ON_CLOSE); frame
      case terminal: Terminal => terminal
    }
  }
}
