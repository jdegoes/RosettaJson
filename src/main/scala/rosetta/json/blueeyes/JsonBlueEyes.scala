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
package blueeyes

import _root_.blueeyes.json.JsonAST._
import _root_.blueeyes.json.Printer._
import _root_.blueeyes.json.JsonParser._
import _root_.blueeyes.json.xschema.DefaultSerialization._

object JsonBlueEyes extends JsonImplementation[JValue] {
  val JsonToString: rosetta.io.Serializer[JValue, String] = new rosetta.io.Serializer[JValue, String] {
    def serialize(v: JValue): String = compact(render(v))

    def deserialize(v: String): JValue = if (v.trim.length == 0) JNothing else parse(v)
  }

  implicit val BooleanToJson: JsonSerializer[Boolean] = new JsonSerializer[Boolean] {
    def serialize(v: Boolean): JValue = JBool(v)

    def deserialize(v: JValue): Boolean = (v --> classOf[JBool]).value
  }

  implicit val StringToJson: JsonSerializer[String] = new JsonSerializer[String] {
    def serialize(v: String): JValue = JString(v)

    def deserialize(v: JValue): String = (v --> classOf[JString]).value
  }

  implicit val LongToJson: JsonSerializer[Long] = new JsonSerializer[Long] {
    def serialize(v: Long): JValue = JInt(v)

    def deserialize(v: JValue): Long = v match {
      case JDouble(v) => v.toLong
      case JInt(v)    => v.toLong

      case _ => sys.error("Expected JDouble or JInt but found: " + v)
    }
  }

  implicit val DoubleToJson: JsonSerializer[Double] = new JsonSerializer[Double] {
    def serialize(v: Double): JValue = JDouble(v)

    def deserialize(v: JValue): Double = v match {
      case JDouble(v) => v
      case JInt(v)    => v.toDouble

      case _ => sys.error("Expected JDouble or JInt but found: " + v)
    }
  }

  implicit def ObjectToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Iterable[(String, A)]] = new JsonSerializer[Iterable[(String, A)]] {
    def serialize(v: Iterable[(String, A)]): JValue = JObject(v.toList.map { field =>
      JField(field._1, serializer.serialize(field._2))
    })

    def deserialize(v: JValue): Iterable[(String, A)] = v match {
      case JObject(fields) => fields.map { field =>
        (field.name, serializer.deserialize(field.value))
      }

      case _ => sys.error("Expected JObject but found: " + v)
    }
  }

  implicit def ArrayToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Iterable[A]] = new JsonSerializer[Iterable[A]] {
    def serialize(v: Iterable[A]): JValue = JArray(v.toList.map(serializer.serialize _))

    def deserialize(v: JValue): Iterable[A] = v match {
      case JArray(elements) => elements.map(serializer.deserialize _)

      case _ => sys.error("Expected JArray but found: " + v)
    }
  }

  implicit def OptionToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Option[A]] = new JsonSerializer[Option[A]] {
    def serialize(v: Option[A]): JValue = v match {
      case None => JNull

      case Some(v) => serializer.serialize(v)
    }

    def deserialize(v: JValue): Option[A] = v match {
      case JNull => None

      case v => Some(serializer.deserialize(v))
    }
  }

  def foldJson[Z](json: JValue, matcher: JsonMatcher[Z]): Z = json match {
    case JNull | JNothing => matcher._1()

    case JBool(v)   => matcher._2(v)
    case JInt(v)    => matcher._3(v.toLong)
    case JDouble(v) => matcher._4(v)
    case JString(v) => matcher._5(v)
    case JArray(v)  => matcher._6(v)
    case JObject(v) => matcher._7(v.map(field => (field.name, field.value)))

    case _ : JField => sys.error("Cannot fold over JField")
  }
}

