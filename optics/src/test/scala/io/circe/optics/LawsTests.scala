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

import cats.Eq
import cats.instances.list._
import cats.instances.option._
import monocle.Lens
import monocle.Optional
import monocle.Prism
import monocle.Traversal
import monocle.function.At
import monocle.function.Each
import monocle.function.FilterIndex
import monocle.function.Index
import monocle.law.LensLaws
import monocle.law.OptionalLaws
import monocle.law.PrismLaws
import monocle.law.TraversalLaws
import monocle.law.discipline.isEqToProp
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Shrink
import org.typelevel.discipline.Laws

/**
 * We use our own implementations because Monocle's don't (currently) use non-default `Shrink`
 * instances. If Monocle changes this we will remove this code.
 */
object LawsTests extends Laws {
  def atTests[S: Arbitrary: Shrink: Eq, I: Arbitrary: Shrink, A: Arbitrary: Eq](implicit
    evAt: At[S, I, A],
    arbAA: Arbitrary[A => A]
  ): RuleSet =
    new SimpleRuleSet("At", lensTests(At.at(_: I)).props: _*)

  def indexTests[S: Arbitrary: Shrink: Eq, I: Arbitrary: Shrink, A: Arbitrary: Shrink: Eq](implicit
    evIndex: Index[S, I, A],
    arbAA: Arbitrary[A => A]
  ): RuleSet = new SimpleRuleSet("Index", optionalTests(Index.index(_: I)).props: _*)

  def filterIndexTests[S: Arbitrary: Shrink: Eq, I, A: Arbitrary: Shrink: Eq](implicit
    evFilterIndex: FilterIndex[S, I, A],
    arbAA: Arbitrary[A => A],
    arbIB: Arbitrary[I => Boolean]
  ): RuleSet = new SimpleRuleSet("FilterIndex", traversalTests(FilterIndex.filterIndex(_: I => Boolean)).props: _*)

  def eachTests[S: Arbitrary: Shrink: Eq, A: Arbitrary: Shrink: Eq](implicit
    evEach: Each[S, A],
    arbAA: Arbitrary[A => A]
  ): RuleSet = new SimpleRuleSet("Each", traversalTests(Each.each[S, A]).props: _*)

  def lensTests[S: Arbitrary: Eq, A: Arbitrary: Shrink: Eq, I: Arbitrary: Shrink](f: I => Lens[S, A])(implicit
    arbAA: Arbitrary[A => A]
  ): RuleSet = {
    def laws(i: I) = LensLaws(f(i))

    new SimpleRuleSet(
      "Lens",
      "set what you get" -> Prop.forAll((s: S, i: I) => laws(i).getReplace(s)),
      "get what you set" -> Prop.forAll((s: S, a: A, i: I) => laws(i).replaceGet(s, a)),
      "set idempotent" -> Prop.forAll((s: S, a: A, i: I) => laws(i).replaceIdempotent(s, a)),
      "modify id = id" -> Prop.forAll((s: S, i: I) => laws(i).modifyIdentity(s)),
      "compose modify" -> Prop.forAll((s: S, g: A => A, h: A => A, i: I) => laws(i).composeModify(s, g, h)),
      "consistent set with modify" -> Prop.forAll((s: S, a: A, i: I) => laws(i).consistentReplaceModify(s, a)),
      "consistent modify with modifyId" ->
        Prop.forAll((s: S, g: A => A, i: I) => laws(i).consistentModifyModifyId(s, g)),
      "consistent get with modifyId" -> Prop.forAll((s: S, i: I) => laws(i).consistentGetModifyId(s))
    )
  }

  def optionalTests[S: Arbitrary: Shrink: Eq, A: Arbitrary: Shrink: Eq, I: Arbitrary: Shrink](
    f: I => Optional[S, A]
  )(implicit arbAA: Arbitrary[A => A]): RuleSet = {
    def laws(i: I) = OptionalLaws(f(i))

    new SimpleRuleSet(
      "Optional",
      "set what you get" -> Prop.forAll((s: S, i: I) => laws(i).getOptionReplace(s)),
      "get what you set" -> Prop.forAll((s: S, a: A, i: I) => laws(i).replaceGetOption(s, a)),
      "set idempotent" -> Prop.forAll((s: S, a: A, i: I) => laws(i).replaceIdempotent(s, a)),
      "modify id = id" -> Prop.forAll((s: S, i: I) => laws(i).modifyIdentity(s)),
      "compose modify" -> Prop.forAll((s: S, g: A => A, h: A => A, i: I) => laws(i).composeModify(s, g, h)),
      "consistent set with modify" -> Prop.forAll((s: S, a: A, i: I) => laws(i).consistentReplaceModify(s, a)),
      "consistent modify with modifyId" -> Prop
        .forAll((s: S, g: A => A, i: I) => laws(i).consistentModifyModifyId(s, g)),
      "consistent getOption with modifyId" -> Prop.forAll((s: S, i: I) => laws(i).consistentGetOptionModifyId(s))
    )
  }

  def prismTests[S: Arbitrary: Shrink: Eq, A: Arbitrary: Shrink: Eq](prism: Prism[S, A])(implicit
    arbAA: Arbitrary[A => A]
  ): RuleSet = {
    val laws: PrismLaws[S, A] = new PrismLaws(prism)

    new SimpleRuleSet(
      "Prism",
      "partial round trip one way" -> Prop.forAll((s: S) => laws.partialRoundTripOneWay(s)),
      "round trip other way" -> Prop.forAll((a: A) => laws.roundTripOtherWay(a)),
      "modify id = id" -> Prop.forAll((s: S) => laws.modifyIdentity(s)),
      "compose modify" -> Prop.forAll((s: S, f: A => A, g: A => A) => laws.composeModify(s, f, g)),
      "consistent set with modify" -> Prop.forAll((s: S, a: A) => laws.consistentReplaceModify(s, a)),
      "consistent modify with modifyId" -> Prop.forAll((s: S, g: A => A) => laws.consistentModifyModifyId(s, g)),
      "consistent getOption with modifyId" -> Prop.forAll((s: S) => laws.consistentGetOptionModifyId(s))
    )
  }

  def traversalTests[S: Arbitrary: Shrink: Eq, A: Arbitrary: Shrink: Eq](traversal: Traversal[S, A])(implicit
    arbAA: Arbitrary[A => A]
  ): RuleSet = traversalTests[S, A, Unit](_ => traversal)

  def traversalTests[S: Arbitrary: Shrink: Eq, A: Arbitrary: Shrink: Eq, I: Arbitrary: Shrink](
    f: I => Traversal[S, A]
  )(implicit arbAA: Arbitrary[A => A]): RuleSet = {
    def laws(i: I): TraversalLaws[S, A] = new TraversalLaws(f(i))

    new SimpleRuleSet(
      "Traversal",
      "headOption" -> Prop.forAll((s: S, i: I) => laws(i).headOption(s)),
      "get what you set" -> Prop.forAll((s: S, f: A => A, i: I) => laws(i).modifyGetAll(s, f)),
      "set idempotent" -> Prop.forAll((s: S, a: A, i: I) => laws(i).replaceIdempotent(s, a)),
      "modify id = id" -> Prop.forAll((s: S, i: I) => laws(i).modifyIdentity(s)),
      "compose modify" -> Prop.forAll((s: S, f: A => A, g: A => A, i: I) => laws(i).composeModify(s, f, g))
    )
  }
}
