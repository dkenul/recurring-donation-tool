package tests

import app.Main
import app.helpers.DataManager
import java.io.File
import scala.io.Source

class IntegrationSpec extends BaseSpec {
  describe ("Integration Tests") {
    File("./integration-tests").listFiles.sorted.foreach { file =>
      val fileName = file.getPath
      it (s"handles $fileName") {
        DataManager.reInitialize()
        val input = Main.process(Array(s"$fileName/input.txt"))
        val output = Source.fromFile(s"$fileName/output.txt").mkString
        
        input shouldEqual output
      }
    }
  }
}