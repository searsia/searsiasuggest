package org.searsia

import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.hadoop.io.LongWritable
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.StringEscapeUtils

import org.jsoup.Jsoup
import org.jwat.warc.WarcRecord
import nl.surfsara.warcutils.WarcInputFormat
import scala.collection.JavaConversions._

object AnchorExtract {

  def getContent(record: WarcRecord): String = {
    var output = ""
    try {
      var cLen = record.header.contentLength.toInt
      val buf = new Array[Byte](cLen)
      val inStream = record.getPayload.getInputStream()
      val outStream = new ByteArrayOutputStream()
      var nRead = inStream.read(buf)
      while (nRead != -1) {
        outStream.write(buf, 0, nRead)
        nRead = inStream.read(buf)
      }
      inStream.close()
      outStream.close()
      output = outStream.toString("UTF-8")
    } catch {
      case e: Exception => println("WARN: " + e)
    }
    return output
  }


  def scrapeAnchors(html: String, baseUri: String): List[String] = {
    val anchors = new scala.collection.mutable.ListBuffer[String]
    try {
      val rootNode = Jsoup.parse(html, baseUri)
      if (rootNode != null) {
        //val title = rootNode.title()
        //if (title != null && title != "") {
        //  val text = StringEscapeUtils.unescapeHtml(title)
        //  val texts = text.split("[:;\\|\\-\\?\\!\\.] ")
        //  anchors ++= texts
        //}
        val elements = rootNode.select("a")
        for (elem <- elements) {
          val rel  = elem.attr("rel")  // no nofollow
          val href = elem.attr("href")
          if (elem.hasText() && href != null && (rel == null || !rel.equalsIgnoreCase("nofollow"))) {
            val text = StringEscapeUtils.unescapeHtml(elem.text())
            val texts = text.split("[:;\\|\\-\\?\\!\\.] ")
            anchors ++= texts
          }
        }
      }
    } catch {
      case e: Exception => println("WARN: " + e);
    }
    return anchors.toList
  }


  def cleanAnchors(text: String): String = {
    text.replaceAll("\\([^\\)]+\\)|\\[[^\\]]+\\]|\\{[^\\}]+\\}", " ")
      .toLowerCase.replaceAll("[\\(\\)\\[\\]\\{\\}]", " ")
      .replaceAll("[0-9a-z]+\\.\\.\\.", " ").replaceAll("\\.\\.\\.[0-9a-z]*", " ")
      .replaceAll("https?:\\/\\/[^ ]*", " ")
      .replaceAll("@[a-z0-9\\-\\.]+", " ")
      .replaceAll(" [^a-z0-9]+ ", " ").replaceAll("[\\r\\n\\t ]+", " ")
      .replaceAll("^[^0-9a-z]+|[^0-9a-z]+$", "")
  }


  def main(args: Array[String]) {
    val appName = this.getClass.getName
    if (args.length != 2) {
      throw new IllegalArgumentException(s"Usage: $appName <in> <out>");
    }
    val inDir   = args(0)
    val outDir  = args(1)
    val conf    = new SparkConf().setAppName(s"$appName $inDir $outDir")
    val sc      = new SparkContext(conf)

    val warcf = sc.newAPIHadoopFile(
      inDir,
      classOf[WarcInputFormat],  // Input
      classOf[LongWritable],     // OutputKey
      classOf[WarcRecord]        // OutputValue
    )

    val output = warcf.map(w => (getContent(w._2), w._2.header.warcTargetUriStr)) // TODO: also contains WARC header
        .flatMap(w => scrapeAnchors(w._1, w._2))
        .map{w => (cleanAnchors(w), 1)}
        .reduceByKey((a, b) => a + b).filter(w => w._2 > 3)
        .sortBy(c => c._2, false).map(w => w._2 + "\t" + w._1)
    output.saveAsTextFile(outDir)
  }
}
