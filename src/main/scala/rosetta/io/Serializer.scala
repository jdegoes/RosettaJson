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
package rosetta.io

// AKA bijection
trait Serializer[A, B] { self =>
  def serialize(v: A): B

  def deserialize(v: B): A

  def inverse: Serializer[B, A] = new Serializer[B, A] {
    def serialize(v: B): A = self.deserialize(v)

    def deserialize(v: A): B = self.serialize(v)
  }

  def >>> [C](that: Serializer[B, C]): Serializer[A, C] = self.andThen(that)

  def andThen[C](that: Serializer[B, C]): Serializer[A, C] = new Serializer[A, C] {
    def serialize(v: A): C = that.serialize(self.serialize(v))

    def deserialize(v: C): A = self.deserialize(that.deserialize(v))
  }
}

trait Serializers {
  def StringToByteArray(charset: String) = new Serializer[String, Array[Byte]] {
    def serialize(v: String): Array[Byte] = v.getBytes(charset)

    def deserialize(v: Array[Byte]): String = new String(v, charset)
  }

  implicit val UTF8StringToByteArray = StringToByteArray("UTF-8")
}
object Serializers extends Serializers

object Serializer {
  // Implicitly invertible
  implicit def serializer2Inverse[A, B](s: Serializer[A, B]): Serializer[B, A] = s.inverse
}

trait SerializerImplicits {
  implicit final def anyToSerializable[A](a: A) = new {
    def serialize[B](implicit s: Serializer[A, B]): B = s.serialize(a)
  }

  implicit final def anyToDeserializable[B](b: B) = new {
    def deserialize[A](implicit s: Serializer[A, B]): A = s.deserialize(b)
  }
}
object SerializerImplicits extends SerializerImplicits