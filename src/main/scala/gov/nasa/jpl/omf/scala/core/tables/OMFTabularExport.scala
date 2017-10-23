/*
 * Copyright 2015 California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
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
 * License Terms
 */

package gov.nasa.jpl.omf.scala.core.tables

import gov.nasa.jpl.omf.scala.core._
import gov.nasa.jpl.imce._
import gov.nasa.jpl.omf.scala.core.OMFError.Throwables

import scala.collection.immutable.Seq
import scala.Predef.String
import scalaz._, Scalaz._

case class Axioms
( aspectSpecializationAxioms : Seq[oml.tables.AspectSpecializationAxiom] = Seq.empty,
  conceptSpecializationAxioms : Seq[oml.tables.ConceptSpecializationAxiom] = Seq.empty,
  reifiedRelationshipSpecializationAxioms : Seq[oml.tables.ReifiedRelationshipSpecializationAxiom] = Seq.empty,

  entityExistentialRestrictionAxioms : Seq[oml.tables.EntityExistentialRestrictionAxiom] = Seq.empty,
  entityUniversalRestrictionAxioms : Seq[oml.tables.EntityUniversalRestrictionAxiom] = Seq.empty,

  entityScalarDataPropertyExistentialRestrictionAxioms : Seq[oml.tables.EntityScalarDataPropertyExistentialRestrictionAxiom] = Seq.empty,
  entityScalarDataPropertyParticularRestrictionAxioms : Seq[oml.tables.EntityScalarDataPropertyParticularRestrictionAxiom] = Seq.empty,
  entityScalarDataPropertyUniversalRestrictionAxioms : Seq[oml.tables.EntityScalarDataPropertyUniversalRestrictionAxiom] = Seq.empty,

  scalarOneOfLiteralAxioms : Seq[oml.tables.ScalarOneOfLiteralAxiom] = Seq.empty )

object Axioms {

  def append
  (a1: Axioms, a2: Axioms)
  : Axioms
  = a1.copy(
    aspectSpecializationAxioms =
      a1.aspectSpecializationAxioms ++ a2.aspectSpecializationAxioms,
    conceptSpecializationAxioms =
      a1.conceptSpecializationAxioms ++ a2.conceptSpecializationAxioms,
    reifiedRelationshipSpecializationAxioms =
      a1.reifiedRelationshipSpecializationAxioms ++ a2.reifiedRelationshipSpecializationAxioms,

    entityExistentialRestrictionAxioms =
      a1.entityExistentialRestrictionAxioms ++ a2.entityExistentialRestrictionAxioms,
    entityUniversalRestrictionAxioms =
      a1.entityUniversalRestrictionAxioms ++ a2.entityUniversalRestrictionAxioms,

    entityScalarDataPropertyExistentialRestrictionAxioms =
      a1.entityScalarDataPropertyExistentialRestrictionAxioms ++ a2.entityScalarDataPropertyExistentialRestrictionAxioms,
    entityScalarDataPropertyParticularRestrictionAxioms =
      a1.entityScalarDataPropertyParticularRestrictionAxioms ++ a2.entityScalarDataPropertyParticularRestrictionAxioms,
    entityScalarDataPropertyUniversalRestrictionAxioms =
      a1.entityScalarDataPropertyUniversalRestrictionAxioms ++ a2.entityScalarDataPropertyUniversalRestrictionAxioms,

    scalarOneOfLiteralAxioms =
      a1.scalarOneOfLiteralAxioms ++ a2.scalarOneOfLiteralAxioms
  )

  def funAspectSpecializationAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#AspectSpecializationAxiom)
  : Axioms
  = {
    val info = ops.fromAspectSubClassAxiom(ax)
    acc.copy(aspectSpecializationAxioms = acc.aspectSpecializationAxioms :+
      oml.tables.AspectSpecializationAxiom(
        uuid = info.uuid.toString,
        tboxUUID = guuid,
        subEntityUUID = ops.getTermUUID(info.sub).toString,
        superAspectUUID = ops.getTermUUID(info.sup).toString))
  }

  def funConceptSpecializationAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#ConceptSpecializationAxiom)
  : Axioms
  = {
    val info = ops.fromConceptSpecializationAxiom(ax)
    acc.copy(conceptSpecializationAxioms = acc.conceptSpecializationAxioms :+
      oml.tables.ConceptSpecializationAxiom(
        uuid = info.uuid.toString,
        tboxUUID = guuid,
        subConceptUUID = ops.getTermUUID(info.sub).toString,
        superConceptUUID = ops.getTermUUID(info.sup).toString))
  }

  def funReifiedRelationshipSpecializationAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#ReifiedRelationshipSpecializationAxiom)
  : Axioms
  = {
    val info = ops.fromReifiedRelationshipSpecializationAxiom(ax)
    acc.copy(reifiedRelationshipSpecializationAxioms = acc.reifiedRelationshipSpecializationAxioms :+
      oml.tables.ReifiedRelationshipSpecializationAxiom(
        tboxUUID = guuid,
        uuid = info.uuid.toString,
        subRelationshipUUID = ops.getTermUUID(info.sub).toString,
        superRelationshipUUID = ops.getTermUUID(info.sup).toString))
  }

  def funEntityExistentialRestrictionAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#EntityExistentialRestrictionAxiom)
  : Axioms
  = {
    val info = ops.fromEntityRestrictionAxiom(ax)
    acc.copy(entityExistentialRestrictionAxioms = acc.entityExistentialRestrictionAxioms :+
      oml.tables.EntityExistentialRestrictionAxiom(
        tboxUUID = guuid,
        uuid = info.uuid.toString,
        restrictedDomainUUID = ops.getTermUUID(info.domain).toString,
        restrictedRangeUUID = ops.getTermUUID(info.range).toString,
        restrictedRelationUUID = ops.getTermUUID(info.restrictedRelation).toString))
  }

  def funEntityUniversalRestrictionAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#EntityUniversalRestrictionAxiom)
  : Axioms
  = {
    val info = ops.fromEntityRestrictionAxiom(ax)
    acc.copy(entityUniversalRestrictionAxioms = acc.entityUniversalRestrictionAxioms :+
      oml.tables.EntityUniversalRestrictionAxiom(
        tboxUUID = guuid,
        uuid = info.uuid.toString,
        restrictedDomainUUID = ops.getTermUUID(info.domain).toString,
        restrictedRangeUUID = ops.getTermUUID(info.range).toString,
        restrictedRelationUUID = ops.getTermUUID(info.restrictedRelation).toString))
  }

  def funEntityScalarDataPropertyExistentialRestrictionAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#EntityScalarDataPropertyExistentialRestrictionAxiom)
  : Axioms
  = {
    val info = ops.fromEntityScalarDataPropertyExistentialRestrictionAxiom(ax)
    acc.copy(entityScalarDataPropertyExistentialRestrictionAxioms = acc.entityScalarDataPropertyExistentialRestrictionAxioms :+
      oml.tables.EntityScalarDataPropertyExistentialRestrictionAxiom(
        tboxUUID = guuid,
        uuid = info.uuid.toString,
        restrictedEntityUUID = ops.getTermUUID(info.restrictedEntity).toString,
        scalarPropertyUUID = ops.getTermUUID(info.scalarDataProperty).toString,
        scalarRestrictionUUID = ops.getTermUUID(info.restrictedRange).toString))
  }

  def funEntityScalarDataPropertyParticularRestrictionAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#EntityScalarDataPropertyParticularRestrictionAxiom)
  : Axioms
  = {
    val info = ops.fromEntityScalarDataPropertyParticularRestrictionAxiom(ax)
    acc.copy(entityScalarDataPropertyParticularRestrictionAxioms = acc.entityScalarDataPropertyParticularRestrictionAxioms :+
      oml.tables.EntityScalarDataPropertyParticularRestrictionAxiom(
        tboxUUID = guuid,
        uuid = info.uuid.toString,
        restrictedEntityUUID = ops.getTermUUID(info.restrictedEntity).toString,
        scalarPropertyUUID = ops.getTermUUID(info.scalarDataProperty).toString,
        literalValue = info.literalValue,
        valueTypeUUID = info.valueType.map { vt => ops.getTermUUID(vt).toString }))
  }

  def funEntityScalarDataPropertyUniversalRestrictionAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#EntityScalarDataPropertyUniversalRestrictionAxiom)
  : Axioms
  = {
    val info = ops.fromEntityScalarDataPropertyUniversalRestrictionAxiom(ax)
    acc.copy(entityScalarDataPropertyUniversalRestrictionAxioms = acc.entityScalarDataPropertyUniversalRestrictionAxioms :+
      oml.tables.EntityScalarDataPropertyUniversalRestrictionAxiom(
        tboxUUID = guuid,
        uuid = info.uuid.toString,
        restrictedEntityUUID = ops.getTermUUID(info.restrictedEntity).toString,
        scalarPropertyUUID = ops.getTermUUID(info.scalarDataProperty).toString,
        scalarRestrictionUUID = ops.getTermUUID(info.restrictedRange).toString))
  }

  def funScalarOneOfLiteralAxiom[omf <: OMF]
  (guuid: String, ops: OMFOps[omf], acc: Axioms)
  (ax: omf#ScalarOneOfLiteralAxiom)
  : Axioms
  = {
    val info = ops.fromScalarOneOfLiteralAxiom(ax)
    acc.copy(scalarOneOfLiteralAxioms = acc.scalarOneOfLiteralAxioms :+
      oml.tables.ScalarOneOfLiteralAxiom(
        tboxUUID = guuid,
        uuid = info.uuid.toString,
        axiomUUID = ops.getTermUUID(info.restriction).toString,
        value = info.value,
        valueTypeUUID = info.valueType.map { vt => ops.getTermUUID(vt).toString }))
  }

  def combine[omf <: OMF]
  (guuid: String, ops: OMFOps[omf])
  (acc: Axioms,
   ax: omf#Axiom)
  : Axioms
  = ops.foldAxiom[Axioms](
    funAspectSpecializationAxiom =
      Axioms.funAspectSpecializationAxiom(guuid, ops, acc),
    funConceptSpecializationAxiom =
      Axioms.funConceptSpecializationAxiom(guuid, ops, acc),
    funReifiedRelationshipSpecializationAxiom =
      Axioms.funReifiedRelationshipSpecializationAxiom(guuid, ops, acc),
    funEntityExistentialRestrictionAxiom =
      Axioms.funEntityExistentialRestrictionAxiom(guuid, ops, acc),
    funEntityUniversalRestrictionAxiom =
      Axioms.funEntityUniversalRestrictionAxiom(guuid, ops, acc),
    funEntityScalarDataPropertyExistentialRestrictionAxiom =
      Axioms.funEntityScalarDataPropertyExistentialRestrictionAxiom(guuid, ops, acc),
    funEntityScalarDataPropertyParticularRestrictionAxiom =
      Axioms.funEntityScalarDataPropertyParticularRestrictionAxiom(guuid, ops, acc),
    funEntityScalarDataPropertyUniversalRestrictionAxiom =
      Axioms.funEntityScalarDataPropertyUniversalRestrictionAxiom(guuid, ops, acc),
    funScalarOneOfLiteralAxiom =
      Axioms.funScalarOneOfLiteralAxiom(guuid, ops, acc)
  )(ax)
}

object OMFTabularExport {

  // @see https://github.com/milessabin/shapeless/blob/master/examples/src/main/scala/shapeless/examples/enum.scala

  import scala.Ordering

  import shapeless.{ Generic, ::, HList, HNil }
  // Derive an Ordering for an HList from the Orderings of its elements

  trait LowPriorityGenericOrdering {
    // An Ordering for any type which is isomorphic to an HList, if that HList has an Ordering

    implicit def hlistIsoOrdering[A, H <: HList]
    (implicit gen : Generic.Aux[A, H], oh : Ordering[H])
    : Ordering[A]
    = new Ordering[A] {
      def compare(a1 : A, a2 : A) = oh.compare(gen to a1, gen to a2)
    }
  }

  object GenericOrdering extends LowPriorityGenericOrdering {
    implicit def hnilOrdering : Ordering[HNil] = new Ordering[HNil] {
      def compare(a : HNil, b : HNil) = 0
    }

    implicit def hlistOrdering[H, T <: HList]
    (implicit oh : Ordering[H], ot : Ordering[T])
    : Ordering[H :: T]
    = new Ordering[H :: T] {
      def compare(a : H :: T, b : H :: T) = {
        val i = oh.compare(a.head, b.head)
        if (i == 0) ot.compare(a.tail, b.tail)
        else i
      }
    }
  }

  implicit def TerminologyKindOrdering: Ordering[oml.tables.TerminologyKind] =
  new Ordering[oml.tables.TerminologyKind] {
    def compare(x: oml.tables.TerminologyKind, y: oml.tables.TerminologyKind): scala.Int
    = if (x == y) 0
    else if (x == oml.tables.OpenWorldDefinitions) -1
    else 1
  }

  def toTables[omf <: OMF]
  (ims: Seq[omf#ImmutableModule])
  (implicit store: omf#Store, ops: OMFOps[omf])
  : Throwables \/ Seq[(omf#ImmutableModule, oml.tables.OMLSpecificationTables)]
  = ims.foldLeft(Seq.empty[(omf#ImmutableModule, oml.tables.OMLSpecificationTables)].right[Throwables]) {
    case (acc, im) =>
      ops.foldImmutableModule(
        funImmutableTerminologyGraph = OMFTabularExportFromTerminologyGraph.toTables(acc),
        funImmutableTerminologyBundle = OMFTabularExportFromBundle.toTables(acc),
        funImmutableDescriptionBox = OMFTabularExportFromDescriptionBox.toTables(acc))(im)
  }


}