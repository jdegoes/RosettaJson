/* Copyright (C) 2011 by John A. De Goes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package rosetta.json

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary

trait ArbitraryJson[Json] {
  val jsonImplementation: JsonImplementation[Json]

  val MaxSize = 3

  import jsonImplementation._

  implicit def arbJson: Arbitrary[Json] = Arbitrary {
    genJson
  }

  def genJson: Gen[Json] = oneOf(genJsonNull, genJsonString, genJsonLong, genJsonDouble, genJsonArray, genJsonObject)

  def genJsonObjectOrJsonArray: Gen[Json] = oneOf(genJsonArray, genJsonObject)

  def genJsonNull: Gen[Json] = {
    value(JsonNull)
  }

  def genJsonString: Gen[Json] = {
    for {
      prefix <- identifier
      suffix <- arbitrary[String]
    } yield JsonString(prefix + suffix)
  }

  def genJsonLong: Gen[Json] = {
    for {
      long <- arbitrary[Long]
    } yield JsonLong(long)
  }

  def genJsonDouble: Gen[Json] = {
    for {
      double <- arbitrary[Double]
    } yield JsonDouble(double)
  }

  def genJsonArray: Gen[Json] = {
    for {
      size     <- chooseNum(1, MaxSize)
      elements <- listOfN(size, arbitrary[Json])
    } yield JsonArray(elements)
  }

  def genJsonField: Gen[(String, Json)] = {
    for {
      name  <- arbitrary[String]
      value <- arbitrary[Json]
    } yield (name, value)
  }

  def genJsonObject: Gen[Json] = {
    for {
      size   <- chooseNum(1, MaxSize)
      fields <- listOfN(size, genJsonField)
    } yield JsonObject(fields)
  }
}