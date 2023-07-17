package app

import scala.io.Source
import scala.io.StdIn

import app.models.Command
import app.helpers.DataManager
import app.helpers.OutputBuilder

object Main {
  def processSingleLine (line: String): Unit = {
    (for {
      command <- Command(line)
      executed <- command.execute
    } yield executed)
      .left.map { err =>
        // optionally handle all caught errors
        // println(err.message)
      }
  }
  
  def process (args: Array[String]): String = {
    val txtFilePattern = """.*\.txt$""".r
    
    args match {
      case Array(filename) if txtFilePattern.matches(filename) => // handle a .txt file
        Source.fromFile(filename).getLines.foreach(processSingleLine)

        OutputBuilder.generateSummary(DataManager.transformData())
      case _ => { // otherwise assume we are reading from stdin
        var line = ""
        while ({line = StdIn.readLine(); line != null}) {
          processSingleLine(line)
        }

        OutputBuilder.generateSummary(DataManager.transformData())
      }
    }
  }

  def main (args: Array[String]): Unit = {
    println(process(args))
  }
}

