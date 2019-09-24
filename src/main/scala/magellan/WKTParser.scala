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

import fastparse._, NoWhitespace._
import fastparse.Parsed.{Success, Failure}
import fastparse.Implicits._

import scala.collection.mutable.ListBuffer

object WKTParser {

  def whitespace[_: P]: P[_] = P(" ")

  def posInt[_: P]: P[String] = P(CharIn("0-9")).rep(1).!.map(_.toString)

  def negInt[_: P]: P[String] = P("-" ~ posInt).map("-" + _).!.map(_.toString)

  def int[_: P]: P[String] = P(posInt | negInt).!.map(_.toString)

  def float[_: P]: P[String] = P(int ~ P(".") ~ posInt).map{case (x , y) => (x + "." + y)}.!.map(_.toString)

  def number[_: P]: P[Double] = P(float | int).!.map(_.toDouble)

  def point0[_: P]: P[_] = P("""POINT""").!.map(_.toString)

  def empty0[_: P]: P[_] = P("""EMPTY""").!.map(_.toString)

  def comma[_: P]: P[_] = P(",").!.map(_.toString)

  def leftBrace[_: P]: P[String] = P("(").!.map(_.toString)

  def rightBrace[_: P]: P[String] = P(")").!.map(_.toString)

  def coords[_: P]: P[Point] =  P(number ~ whitespace ~ number).map{
    case (x, _, y) => Point(x, y)
  }

  def ring[_: P]: P[Array[Point]] = P(leftBrace ~ coords.rep(1, (comma ~ whitespace | comma)) ~ rightBrace) map {
    case (_, x ,_) => x.toArray
  }

  def point[_: P]: P[Point] = P(point0 ~ whitespace.? ~ leftBrace ~ coords ~ rightBrace) map {
    case (_ , _, _, p, _) => p
  }

  def pointEmpty[_: P]: P[Shape] = P(point0 ~ whitespace ~ empty0) map {_ => NullShape}

  def linestring0[_: P]: P[String] = P("""LINESTRING""") map {_.toString}

  def linestring[_: P]: P[PolyLine] = P(linestring0 ~ whitespace.? ~ ring) map {
    case (_ , _, x) => PolyLine(Array(0), x)
  }

  def polygon0[_: P]: P[String] = P("""POLYGON""") map {_.toString}

  def polygonWithoutHoles[_: P]: P[Polygon] =
    P(polygon0 ~ whitespace.? ~ P("((") ~ coords.rep(1, (comma ~ whitespace | comma)) ~ P("))")) map {
    case (_ , _, x ) => Polygon(Array(0), x.toArray)
  }

  def polygonWithHoles[_: P]: P[Polygon] =
    P(polygon0 ~ whitespace.? ~ P("(") ~ ring.rep(1, (comma ~ whitespace | comma)) ~ P(")")) map {
    case (_ , _, x) =>
      val indices = ListBuffer[Int]()
      val points = ListBuffer[Point]()
      var prev = 0
      var i = 0
      val numRings = x.size
      while (i < numRings) {
        indices.+= (prev)
        prev += x(i).length
        points.++=(x(i))
        i += 1
      }
      Polygon(indices.toArray, points.toArray)
  }

  def expr[_: P]: P[Shape] = P(point | pointEmpty | linestring | polygonWithoutHoles | polygonWithHoles ~ End)

  def parseAll(text: String): Shape = {
//    expr.parse(text) match {
//      case Success(value: Shape, _) => value
//      case Failure(_, _, stack) => throw new RuntimeException(stack.toString)
//    }
    val Success(result, _) = parse(text, expr(_))

    result
  }

}
