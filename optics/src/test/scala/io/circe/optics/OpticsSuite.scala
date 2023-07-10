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

import cats.kernel.Hash
import cats.kernel.Order
import io.circe.Json
import io.circe.JsonNumber
import io.circe.JsonObject
import io.circe.optics.all._
import monocle.function.Plated.plate
import monocle.syntax.all._

class OpticsSuite extends CirceSuite {

  /**
   * For the purposes of these tests we consider `Double.NaN` to be equal to
   * itself.
   */
  implicit override val catsKernelStdOrderForDouble: Order[Double] with Hash[Double] =
    new cats.kernel.instances.DoubleOrder {
      override def eqv(x: Double, y: Double): Boolean =
        (x.isNaN && y.isNaN) || x == y
    }

  checkAll("Json to Unit", LawsTests.prismTests(jsonNull))
  checkAll("Json to Boolean", LawsTests.prismTests(jsonBoolean))
  checkAll("Json to BigDecimal", LawsTests.prismTests(jsonBigDecimal))
  checkAll("Json to Double", LawsTests.prismTests(jsonDouble))
  checkAll("Json to BigInt", LawsTests.prismTests(jsonBigInt))
  checkAll("Json to Long", LawsTests.prismTests(jsonLong))
  checkAll("Json to Int", LawsTests.prismTests(jsonInt))
  checkAll("Json to Short", LawsTests.prismTests(jsonShort))
  checkAll("Json to Byte", LawsTests.prismTests(jsonByte))
  checkAll("Json to String", LawsTests.prismTests(jsonString))
  checkAll("Json to JsonNumber", LawsTests.prismTests(jsonNumber))
  checkAll("Json to JsonObject", LawsTests.prismTests(jsonObject))
  checkAll("Json to Vector[Json]", LawsTests.prismTests(jsonArray))

  checkAll("JsonNumber to BigDecimal", LawsTests.prismTests(jsonNumberBigDecimal))
  checkAll("JsonNumber to BigInt", LawsTests.prismTests(jsonNumberBigInt))
  checkAll("JsonNumber to Long", LawsTests.prismTests(jsonNumberLong))
  checkAll("JsonNumber to Int", LawsTests.prismTests(jsonNumberInt))
  checkAll("JsonNumber to Short", LawsTests.prismTests(jsonNumberShort))
  checkAll("JsonNumber to Byte", LawsTests.prismTests(jsonNumberByte))

  checkAll("plated Json", LawsTests.traversalTests(plate[Json]))

  checkAll("jsonObjectEach", LawsTests.eachTests[JsonObject, Json])
  checkAll("jsonObjectAt", LawsTests.atTests[JsonObject, String, Option[Json]])
  checkAll("jsonObjectIndex", LawsTests.indexTests[JsonObject, String, Json])
  checkAll("jsonObjectFilterIndex", LawsTests.filterIndexTests[JsonObject, String, Json])

  "jsonDouble" should "round-trip in reverse with Double.NaN" in {
    assert(jsonDouble.getOption(jsonDouble.reverseGet(Double.NaN)) === Some(Double.NaN))
  }

  it should "partial round-trip with numbers larger than Double.MaxValue" in {
    val json = Json.fromJsonNumber(JsonNumber.fromString((BigDecimal(Double.MaxValue) + 1).toString).get)

    assert(jsonDouble.getOrModify(json).fold(identity, jsonDouble.reverseGet) === json)
  }

  "jsonObjectFields" should "fold over all fields" in forAll { (obj: JsonObject) =>
    assert(obj.applyFold(JsonObjectOptics.jsonObjectFields).foldMap(List(_)) === obj.toList)
  }
}
