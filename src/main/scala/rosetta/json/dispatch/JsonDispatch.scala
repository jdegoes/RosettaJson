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
package rosetta.json.dispatch

import dispatch.json._

import rosetta.json._

trait JsonDispatch extends JsonImplementation[JsValue] {
  val JsonToString: rosetta.io.Serializer[JsValue, String] = new rosetta.io.Serializer[JsValue, String] {
    def serialize(v: JsValue): String = JsValue.toJson(v)

    def deserialize(v: String): JsValue = if (v.trim.length == 0) JsNull else JsValue.fromString(v)
  }

  implicit val BooleanToJson: JsonSerializer[Boolean] = new JsonSerializer[Boolean] {
    def serialize(v: Boolean): JsValue = if (v) JsTrue else JsFalse

    def deserialize(v: JsValue): Boolean = v match {
      case JsBoolean(v) => v

      case _ => error("Expected Boolean but found: " + v)
    }
  }

  implicit val StringToJson: JsonSerializer[String] = new JsonSerializer[String] {
    def serialize(v: String): JsValue = JsString(v)

    def deserialize(v: JsValue): String = v match {
      case JsString(v) => v

      case _ => error("Expected String but found: " + v)
    }
  }

  implicit val LongToJson: JsonSerializer[Long] = new JsonSerializer[Long] {
    def serialize(v: Long): JsValue = JsNumber(v)

    def deserialize(v: JsValue): Long = v match {
      case JsNumber(v) => v.toLong

      case _ => error("Expected Long but found: " + v)
    }
  }

  implicit val DoubleToJson: JsonSerializer[Double] = new JsonSerializer[Double] {
    def serialize(v: Double): JsValue = JsNumber(v)

    def deserialize(v: JsValue): Double = v match {
      case JsNumber(v) => v.toDouble

      case _ => error("Expected Double but found: " + v)
    }
  }

  implicit def ObjectToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Iterable[(String, A)]] = new JsonSerializer[Iterable[(String, A)]] {
    def serialize(v: Iterable[(String, A)]): JsValue = JsObject(v.toList.map { field =>
      val (name, value) = field

      (JsString(name), serializer.serialize(value))
    }.toMap)

    def deserialize(v: JsValue): Iterable[(String, A)] = v match {
      case JsObject(fields) => fields.map { field =>
        val (name, value) = field

        (name.self, serializer.deserialize(value))
      }

      case _ => error("Expected Object but found: " + v)
    }
  }

  implicit def ArrayToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Iterable[A]] = new JsonSerializer[Iterable[A]] {
    def serialize(v: Iterable[A]): JsValue = JsArray(v.toList.map(serializer.serialize _))

    def deserialize(v: JsValue): Iterable[A] = v match {
      case JsArray(v) => v.map(serializer.deserialize _)

      case _ => error("Expected Array but found: " + v)
    }
  }

  implicit def OptionToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Option[A]] = new JsonSerializer[Option[A]] {
    def serialize(v: Option[A]): JsValue = v match {
      case None => JsNull

      case Some(v) => serializer.serialize(v)
    }

    def deserialize(v: JsValue): Option[A] = v match {
      case JsNull => None

      case _ => Some(serializer.deserialize(v))
    }
  }

  def foldJson[Z](json: JsValue, matcher: JsonMatcher[Z]): Z = json match {
    case JsNull => matcher._1()

    case JsBoolean(v) => matcher._2(v)
    case JsNumber(v) if (v == BigDecimal(v.toLong)) => matcher._3(v.toLong)
    case JsNumber(v)  => matcher._4(v.toDouble)
    case JsString(v)  => matcher._5(v)
    case JsArray(v)   => matcher._6(v)
    case JsObject(v)  => matcher._7(v.map(field => (field._1.self, field._2)))

    case _ => error("Cannot fold over " + json)
  }
}
object JsonDispatch extends JsonDispatch
