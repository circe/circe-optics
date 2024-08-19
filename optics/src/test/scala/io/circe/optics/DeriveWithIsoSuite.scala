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

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import monocle.Iso

class DeriveWithIsoSuite extends CirceSuite {

  case class UserSnake(id: Long, first_name: String, last_name: String)
  case class UserCamel(id: Long, firstName: String, lastName: String)

  val snake2camel = Iso[UserSnake, UserCamel]((s: UserSnake) => UserCamel(s.id, s.first_name, s.last_name)) {
    (s: UserCamel) => UserSnake(s.id, s.firstName, s.lastName)
  }

  val john: Json = Json.obj(
    "id" -> 1.asJson,
    "first_name" -> "John".asJson,
    "last_name" -> "Doe".asJson
  )

  "deriveDecoderWithIso[A, B]" should "create a decoder[B] with an Iso[A, B]" in {
    implicit val iso: Iso[UserSnake, UserCamel] = snake2camel
    implicit val snakeDecoder: Decoder[UserSnake] = deriveDecoder[UserSnake]

    val camelDecoder: Decoder[UserCamel] = deriveDecoderWithIso[UserSnake, UserCamel]

    assert(camelDecoder.decodeJson(john) == Right(UserCamel(1, "John", "Doe")))
  }

  "deriveEncoderWithIso[A, B]" should "create an encoder[B] with an Iso[A, B]" in {
    implicit val iso: Iso[UserSnake, UserCamel] = snake2camel
    implicit val snakeEncoder: Encoder[UserSnake] = deriveEncoder[UserSnake]

    val camelEncoder: Encoder[UserCamel] = deriveEncoderWithIso[UserSnake, UserCamel]

    assert(camelEncoder(UserCamel(1, "John", "Doe")) == john)
  }

  "deriveDecoderWithIsoReverse[B, A]" should "create a decoder[B] with an Iso[B, A]" in {
    implicit val iso: Iso[UserCamel, UserSnake] = snake2camel.reverse
    implicit val snakeDecoder: Decoder[UserSnake] = deriveDecoder[UserSnake]

    val camelDecoder: Decoder[UserCamel] = deriveDecoderWithIsoReverse[UserCamel, UserSnake]

    assert(camelDecoder.decodeJson(john) == Right(UserCamel(1, "John", "Doe")))
  }

  "deriveEncoderWithIsoReverse[B, A]" should "create an encoder[B] with an Iso[B, A]" in {
    implicit val iso: Iso[UserCamel, UserSnake] = snake2camel.reverse
    implicit val snakeEncoder: Encoder[UserSnake] = deriveEncoder[UserSnake]

    val camelEncoder: Encoder[UserCamel] = deriveEncoderWithIsoReverse[UserCamel, UserSnake]

    assert(camelEncoder(UserCamel(1, "John", "Doe")) == john)
  }
}
