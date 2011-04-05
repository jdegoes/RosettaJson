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
import Prop.{forAll}
import Arbitrary.arbitrary

abstract class JsonTest[Json] extends Properties("Json") with ArbitraryJson[Json] {
  import jsonImplementation._

  def invertibleSerializationForAllJson = {
    property("invertible serialization for all json values") = forAll { (json: Json) =>
      json.serialize[String].deserialize[Json] == json
    }
  }

  def invertibleSerializationForObjectsAndArrays = {
    property("invertible serialization for objects and arrays") = forAll(genJsonObjectOrJsonArray) { (json: Json) =>
      json.serialize[String].deserialize[Json] == json
    }
  }

  def getForAllObjects = {
    property("get for all objects") = forAll(genJsonObject) { (json: Json) =>
      json match {
        case JsonObject(map) =>
          map.foldLeft(true == true) { (props, tuple) =>
            val (name, value) = tuple

            props && (json.get(name) == value)
          }
      }
    }
  }

  def mapDownWithIdentity = {
    property("mapDown with identity") = forAll { (json: Json) =>
      json.mapDown(j => j) == json
    }
  }

  def mapUpWithIdentity = {
    property("mapUp with identity") = forAll { (json: Json) =>
      json.mapUp(j => j) == json
    }
  }
}