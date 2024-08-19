/*
 * Copyright 2016 circe
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

import cats.Applicative
import cats.Foldable
import cats.Monoid
import cats.Traverse
import cats.instances.ListInstances
import io.circe.Json
import io.circe.JsonObject
import monocle.Fold
import monocle.Lens
import monocle.Traversal
import monocle.function.At
import monocle.function.Each
import monocle.function.FilterIndex
import monocle.function.Index

/**
 * Optics instances for [[io.circe.JsonObject]].
 *
 * @author Sean Parsons
 * @author Travis Brown
 */
trait JsonObjectOptics extends ListInstances {
  final lazy val jsonObjectFields: Fold[JsonObject, (String, Json)] = new Fold[JsonObject, (String, Json)] {
    def foldMap[M: Monoid](f: ((String, Json)) => M)(obj: JsonObject): M = Foldable[List].foldMap(obj.toList)(f)
  }

  implicit final lazy val jsonObjectEach: Each[JsonObject, Json] = new Each[JsonObject, Json] {
    final def each: Traversal[JsonObject, Json] = new Traversal[JsonObject, Json] {
      final def modifyA[F[_]](f: Json => F[Json])(from: JsonObject)(implicit
        F: Applicative[F]
      ): F[JsonObject] = from.traverse(f)
    }
  }

  implicit final lazy val jsonObjectAt: At[JsonObject, String, Option[Json]] =
    new At[JsonObject, String, Option[Json]] {
      final def at(field: String): Lens[JsonObject, Option[Json]] =
        Lens[JsonObject, Option[Json]](_.apply(field))(optVal =>
          obj => optVal.fold(obj.remove(field))(value => obj.add(field, value))
        )
    }

  implicit final lazy val jsonObjectFilterIndex: FilterIndex[JsonObject, String, Json] =
    new FilterIndex[JsonObject, String, Json] {
      final def filterIndex(p: String => Boolean): Traversal[JsonObject, Json] = new Traversal[JsonObject, Json] {
        final def modifyA[F[_]](f: Json => F[Json])(from: JsonObject)(implicit
          F: Applicative[F]
        ): F[JsonObject] =
          F.map(
            Traverse[List].traverse(from.toList) { case (field, json) =>
              F.map(if (p(field)) f(json) else F.point(json))((field, _))
            }
          )(JsonObject.fromFoldable(_))
      }
    }

  implicit final lazy val jsonObjectIndex: Index[JsonObject, String, Json] = Index.fromAt
}

object JsonObjectOptics extends JsonObjectOptics
