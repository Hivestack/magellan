/**
  * Copyright 2015 Ram Sriharsha
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package magellan

import com.vividsolutions.jts.io.WKTReader
import org.scalatest.FunSuite
import fastparse.Implicits._
import fastparse.Parsed.Success
import fastparse.parse

class WKTParserSuite extends FunSuite {

  test("parse int") {
    val Success(result, _) = parse("-30", WKTParser.int(_))
    assert(result ===  "-30")
  }

  test("parse float") {
    val Success(result, _) = parse("-79.470579", WKTParser.float(_))
    assert(result === "-79.470579")
  }

  test("parse number") {
    val Success(result, _) = parse("-79.470579", WKTParser.number(_))
    assert(result === -79.470579)
  }

  test("parse point") {
    val Success(p, _) = parse("POINT (30 10)", WKTParser.point(_))
    assert(p.getX() === 30.0)
    assert(p.getY() === 10.0)
  }

  test("parse linestring") {
    val Success(p, _) = parse("LINESTRING (30 10, 10 30, 40 40)", WKTParser.linestring(_))
    assert(p.getNumRings() === 1)
    assert(p.length === 3)

    val Success(p2, _) = parse("LINESTRING (-79.470579 35.442827,-79.469465 35.444889,-79.468907 35.445829,-79.468294 35.446608,-79.46687 35.447893)", WKTParser.linestring(_))
    assert(p2.length === 5)

  }

  test("parse polygon without holes") {
    val Success(p, _) = parse("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))", WKTParser.polygonWithoutHoles(_))
    assert(p.length === 5)
  }

  test("parse polygon with holes") {
    val Success(p, _) = parse("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10), (20 30, 35 35, 30 20, 20 30))", WKTParser.polygonWithHoles(_))
    assert(p.getNumRings() == 2)
    assert(p.getRing(1) == 5)
    assert(p.getVertex(4) === Point(35.0, 10.0))
    assert(p.getVertex(5) === Point(20.0, 30.0))

  }

  test("parse Polygon without space") {
    val Success(p, _) = parse("POLYGON((35 10, 45 45, 15 40, 10 20, 35 10), (20 30, 35 35, 30 20, 20 30))", WKTParser.polygonWithHoles(_))
    assert(p.getNumRings() == 2)
    assert(p.getRing(1) == 5)
    assert(p.getVertex(4) === Point(35.0, 10.0))
    assert(p.getVertex(5) === Point(20.0, 30.0))
  }

  test("parse") {
    val shape = WKTParser.parseAll("LINESTRING (30 10, 10 30, 40 40)")
    assert(shape.isInstanceOf[PolyLine])
  }

  test("perf") {

    def time[R](block: => R): R = {
      val t0 = System.nanoTime()
      val result = block    // call-by-name
      val t1 = System.nanoTime()
      println("Elapsed time: " + (t1 - t0)/1E6 + "ms")
      result
    }

    def exec(text: String, n: Int, fn: (String) => Any) = {
      var i = 0
      while (i < n) {
        fn(text)
        i += 1
      }
    }

    val text = "LINESTRING (30 10, 10 30, 40 40)"
    val n = 100000

    time(exec(text, n, parseUsingJTS))
    time(exec(text, n, (s: String) => parse(s, WKTParser.linestring(_))))

  }

  private def parseUsingJTS(text: String): Shape = {
    val wkt = new WKTReader()
    val polyline = wkt.read(text)
    val coords = polyline.getCoordinates()
    val points = new Array[Point](coords.length)
    var i = 0
    while (i < coords.length) {
      val coord = coords(i)
      points.update(i, Point(coord.x, coord.y))
      i += 1
    }
    PolyLine(Array(0), points)
  }
}
