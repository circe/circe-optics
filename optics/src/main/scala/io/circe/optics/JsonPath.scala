/*
 * Copyright 2023 circe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.circe.optics

import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.JsonNumber
import io.circe.JsonObject
import io.circe.optics.JsonObjectOptics._
import io.circe.optics.JsonOptics._
import monocle.Fold
import monocle.Iso
import monocle.Optional
import monocle.Prism
import monocle.Traversal
import monocle.function.At
import monocle.function.FilterIndex
import monocle.function.Index

import scala.language.dynamics

final case class JsonPath(json: Optional[Json, Json]) extends Dynamic {
  final def `null`: Optional[Json, Unit] = json.andThen(jsonNull)
  final def boolean: Optional[Json, Boolean] = json.andThen(jsonBoolean)
  final def byte: Optional[Json, Byte] = json.andThen(jsonByte)
  final def short: Optional[Json, Short] = json.andThen(jsonShort)
  final def int: Optional[Json, Int] = json.andThen(jsonInt)
  final def long: Optional[Json, Long] = json.andThen(jsonLong)
  final def bigInt: Optional[Json, BigInt] = json.andThen(jsonBigInt)
  final def double: Optional[Json, Double] = json.andThen(jsonDouble)
  final def bigDecimal: Optional[Json, BigDecimal] = json.andThen(jsonBigDecimal)
  final def number: Optional[Json, JsonNumber] = json.andThen(jsonNumber)
  final def string: Optional[Json, String] = json.andThen(jsonString)
  final def arr: Optional[Json, Vector[Json]] = json.andThen(jsonArray)
  final def obj: Optional[Json, JsonObject] = json.andThen(jsonObject)

  final def at(field: String): Optional[Json, Option[Json]] =
    json.andThen(jsonObject).andThen(At.at(field))

  final def selectDynamic(field: String): JsonPath =
    JsonPath(json.andThen(jsonObject).andThen(Index.index(field)))

  final def applyDynamic(field: String)(index: Int): JsonPath = selectDynamic(field).index(index)

  final def apply(i: Int): JsonPath = index(i)

  final def index(i: Int): JsonPath =
    JsonPath(json.andThen(jsonArray).andThen(Index.index[Vector[Json], Int, Json](i)))

  final def each: JsonTraversalPath =
    JsonTraversalPath(json.andThen(jsonDescendants))

  final def filterByIndex(p: Int => Boolean): JsonTraversalPath =
    JsonTraversalPath(arr.andThen(FilterIndex.filterIndex[Vector[Json], Int, Json](p)))

  final def filterByField(p: String => Boolean): JsonTraversalPath =
    JsonTraversalPath(obj.andThen(FilterIndex.filterIndex(p)))

  final def filterUnsafe(p: Json => Boolean): JsonPath =
    JsonPath(json.andThen(UnsafeOptics.select(p)))

  final def filter(p: Json => Boolean): JsonFoldPath =
    JsonFoldPath(filterUnsafe(p).json.asFold)

  final def as[A](implicit decode: Decoder[A], encode: Encoder[A]): Optional[Json, A] =
    json.andThen(UnsafeOptics.parse)

  final def atAs[A](field: String)(implicit decode: Decoder[A], encode: Encoder[A]): Optional[Json, Option[A]] =
    at(field).andThen(UnsafeOptics.optionParse)
}

object JsonPath {
  final val root: JsonPath = JsonPath(Iso.id[Json])
}

final case class JsonTraversalPath(json: Traversal[Json, Json]) extends Dynamic {
  final def `null`: Traversal[Json, Unit] = json.andThen(jsonNull)
  final def boolean: Traversal[Json, Boolean] = json.andThen(jsonBoolean)
  final def byte: Traversal[Json, Byte] = json.andThen(jsonByte)
  final def short: Traversal[Json, Short] = json.andThen(jsonShort)
  final def int: Traversal[Json, Int] = json.andThen(jsonInt)
  final def long: Traversal[Json, Long] = json.andThen(jsonLong)
  final def bigInt: Traversal[Json, BigInt] = json.andThen(jsonBigInt)
  final def double: Traversal[Json, Double] = json.andThen(jsonDouble)
  final def bigDecimal: Traversal[Json, BigDecimal] = json.andThen(jsonBigDecimal)
  final def number: Traversal[Json, JsonNumber] = json.andThen(jsonNumber)
  final def string: Traversal[Json, String] = json.andThen(jsonString)
  final def arr: Traversal[Json, Vector[Json]] = json.andThen(jsonArray)
  final def obj: Traversal[Json, JsonObject] = json.andThen(jsonObject)

  final def at(field: String): Traversal[Json, Option[Json]] =
    json.andThen(jsonObject).andThen(At.at(field))

  final def selectDynamic(field: String): JsonTraversalPath =
    JsonTraversalPath(json.andThen(jsonObject).andThen(Index.index(field)))

  final def applyDynamic(field: String)(index: Int): JsonTraversalPath = selectDynamic(field).index(index)

  final def apply(i: Int): JsonTraversalPath = index(i)

  final def index(i: Int): JsonTraversalPath =
    JsonTraversalPath(json.andThen(jsonArray).andThen(Index.index(i)(Index.vectorIndex[Json])))

  final def each: JsonTraversalPath =
    JsonTraversalPath(json.andThen(jsonDescendants))

  final def filterByIndex(p: Int => Boolean): JsonTraversalPath =
    JsonTraversalPath(arr.andThen(FilterIndex.filterIndex(p)(FilterIndex.vectorFilterIndex[Json])))

  final def filterByField(p: String => Boolean): JsonTraversalPath =
    JsonTraversalPath(obj.andThen(FilterIndex.filterIndex(p)))

  final def filterUnsafe(p: Json => Boolean): JsonTraversalPath =
    JsonTraversalPath(json.andThen(UnsafeOptics.select(p)))

  final def filter(p: Json => Boolean): JsonFoldPath =
    JsonFoldPath(filterUnsafe(p).json.asFold)

  final def as[A](implicit decode: Decoder[A], encode: Encoder[A]): Traversal[Json, A] =
    json.andThen(UnsafeOptics.parse)

  final def atAs[A](field: String)(implicit decode: Decoder[A], encode: Encoder[A]): Traversal[Json, Option[A]] =
    at(field).andThen(UnsafeOptics.optionParse)
}

final case class JsonFoldPath(json: Fold[Json, Json]) extends Dynamic {
  final def `null`: Fold[Json, Unit] = json.andThen(jsonNull)
  final def boolean: Fold[Json, Boolean] = json.andThen(jsonBoolean)
  final def byte: Fold[Json, Byte] = json.andThen(jsonByte)
  final def short: Fold[Json, Short] = json.andThen(jsonShort)
  final def int: Fold[Json, Int] = json.andThen(jsonInt)
  final def long: Fold[Json, Long] = json.andThen(jsonLong)
  final def bigInt: Fold[Json, BigInt] = json.andThen(jsonBigInt)
  final def double: Fold[Json, Double] = json.andThen(jsonDouble)
  final def bigDecimal: Fold[Json, BigDecimal] = json.andThen(jsonBigDecimal)
  final def number: Fold[Json, JsonNumber] = json.andThen(jsonNumber)
  final def string: Fold[Json, String] = json.andThen(jsonString)
  final def arr: Fold[Json, Vector[Json]] = json.andThen(jsonArray)
  final def obj: Fold[Json, JsonObject] = json.andThen(jsonObject)

  final def at(field: String): Fold[Json, Option[Json]] =
    json.andThen(jsonObject).andThen(At.at(field))

  final def selectDynamic(field: String): JsonFoldPath =
    JsonFoldPath(json.andThen(jsonObject).andThen(Index.index(field)))

  final def applyDynamic(field: String)(index: Int): JsonFoldPath = selectDynamic(field).index(index)

  final def apply(i: Int): JsonFoldPath = index(i)

  final def index(i: Int): JsonFoldPath =
    JsonFoldPath(json.andThen(jsonArray).andThen(Index.index(i)))

  final def each: JsonFoldPath =
    JsonFoldPath(json.andThen(jsonDescendants))

  final def filterByIndex(p: Int => Boolean): JsonFoldPath =
    JsonFoldPath(arr.andThen(FilterIndex.filterIndex(p)))

  final def filterByField(p: String => Boolean): JsonFoldPath =
    JsonFoldPath(obj.andThen(FilterIndex.filterIndex(p)))

  final def filter(p: Json => Boolean): JsonFoldPath =
    JsonFoldPath(json.andThen(UnsafeOptics.select(p)))

  final def as[A](implicit decode: Decoder[A], encode: Encoder[A]): Fold[Json, A] =
    json.andThen(UnsafeOptics.parse)

  final def atAs[A](field: String)(implicit decode: Decoder[A], encode: Encoder[A]): Fold[Json, Option[A]] =
    at(field).andThen(UnsafeOptics.optionParse)
}

object UnsafeOptics {

  /**
   * Decode a value at the current location.
   *
   * Note that this operation is not lawful, since decoding is not injective (as noted by Julien
   * Truffaut). It is provided here for convenience, but may change in future versions.
   */
  def parse[A](implicit decode: Decoder[A], encode: Encoder[A]): Prism[Json, A] =
    Prism[Json, A](decode.decodeJson(_) match {
      case Right(a) => Some(a)
      case Left(_)  => None
    })(encode(_))

  /**
   * Decode a value at the current location.
   * But give Option[A] instead of A in order to treat non-exist field as None
   *
   * Note that this operation is not lawful, since the same reason as above
   * It is provided here for convenience, but may change in future versions.
   */
  final val keyMissingNone: Option[None.type] = Some(None)
  def optionParse[A](implicit decode: Decoder[A], encode: Encoder[A]): Prism[Option[Json], Option[A]] =
    Prism[Option[Json], Option[A]] {
      case Some(json) =>
        decode.decodeJson(json) match {
          case Right(a) => Some(Some(a))
          case Left(_)  => None
        }
      case None => keyMissingNone
    }(_.map(encode(_)))

  /**
   * Select if a value matches a predicate
   *
   * Note that this operation is not lawful because the predicate could be invalidated with set or modify.
   * However `select(_.a > 10) composeLens b` is safe because once we zoom into `b`, we cannot change `a` anymore.
   */
  def select[A](p: A => Boolean): Prism[A, A] =
    Prism[A, A](a => if (p(a)) Some(a) else None)(Predef.identity)
}
