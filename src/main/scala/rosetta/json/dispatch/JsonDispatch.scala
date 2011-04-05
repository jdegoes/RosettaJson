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
  type Json = JsValue

  implicit val BooleanSerializer: Serializer[Boolean] = new Serializer[Boolean] {
    def serialize(v: Boolean): Json = if (v) JsTrue else JsFalse

    def deserialize(v: Json): Boolean = v match {
      case JsBoolean(v) => v

      case _ => error("Expected Boolean but found: " + v)
    }
  }

  implicit val StringSerializer: Serializer[String] = new Serializer[String] {
    def serialize(v: String): Json = JsString(v)

    def deserialize(v: Json): String = v match {
      case JsString(v) => v

      case _ => error("Expected String but found: " + v)
    }
  }

  implicit val LongSerializer: Serializer[Long] = new Serializer[Long] {
    def serialize(v: Long): Json = JsNumber(v)

    def deserialize(v: Json): Long = v match {
      case JsNumber(v) => v.toLong

      case _ => error("Expected Long but found: " + v)
    }
  }

  implicit val DoubleSerializer: Serializer[Double] = new Serializer[Double] {
    def serialize(v: Double): Json = JsNumber(v)

    def deserialize(v: Json): Double = v match {
      case JsNumber(v) => v.toDouble

      case _ => error("Expected Double but found: " + v)
    }
  }

  implicit def ObjectSerializer[A](implicit serializer: Serializer[A]): Serializer[Iterable[(String, A)]] = new Serializer[Iterable[(String, A)]] {
    def serialize(v: Iterable[(String, A)]): Json = JsObject(Map(v.toList.map { field =>
      val (name, value) = field

      (JsString(name), serializer.serialize(value))
    }: _*))

    def deserialize(v: Json): Iterable[(String, A)] = v match {
      case JsObject(fields) => fields.map { field =>
        val (name, value) = field

        (name.self, serializer.deserialize(value))
      }

      case _ => error("Expected Object but found: " + v)
    }
  }

  implicit def ArraySerializer[A](implicit serializer: Serializer[A]): Serializer[Iterable[A]] = new Serializer[Iterable[A]] {
    def serialize(v: Iterable[A]): Json = JsArray(v.toList.map(serializer.serialize _))

    def deserialize(v: Json): Iterable[A] = v match {
      case JsArray(v) => v.map(serializer.deserialize _)

      case _ => error("Expected Array but found: " + v)
    }
  }

  implicit def OptionSerializer[A](implicit serializer: Serializer[A]): Serializer[Option[A]] = new Serializer[Option[A]] {
    def serialize(v: Option[A]): Json = v match {
      case None => JsNull

      case Some(v) => serializer.serialize(v)
    }

    def deserialize(v: Json): Option[A] = v match {
      case JsNull => None

      case _ => Some(serializer.deserialize(v))
    }
  }

  def foldJson[Z](json: Json, matcher: JsonMatcher[Z]): Z = json match {
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
