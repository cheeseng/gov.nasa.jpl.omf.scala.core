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

import java.lang.System

import gov.nasa.jpl.imce.oml
import gov.nasa.jpl.imce.oml.resolver
import gov.nasa.jpl.imce.oml.resolver.ResolverUtilities.toUUIDString
import gov.nasa.jpl.imce.oml.tables.taggedTypes
import gov.nasa.jpl.omf.scala.core.OMFError.Throwables
import gov.nasa.jpl.omf.scala.core.{OMF, OMFError, OMFOps, RelationshipCharacteristics, TerminologyKind}

import scala.collection.immutable._
import scala.{StringContext, Unit}
import scala.Predef.ArrowAssoc
import scalaz._
import Scalaz._

object OMFTabularExportFromBundle {

  implicit def toIRI[omf <: OMF](iri: omf#IRI): taggedTypes.IRI = taggedTypes.iri(iri.toString)

  def toTables[omf <: OMF]
  (acc: Throwables \/ Seq[(omf#ImmutableModule, oml.tables.OMLSpecificationTables)])
  (bundle: omf#ImmutableBundle)
  (implicit store: omf#Store, ops: OMFOps[omf])
  : Throwables \/ Seq[(omf#ImmutableModule, oml.tables.OMLSpecificationTables)]
  = for {
    im2st <- acc
    all_tboxes = im2st.map(_._1).toSet[omf#Module]
    all_tables = im2st.map(_._2).to[Set]

    all_aps = all_tables.flatMap(_.annotationProperties)

    s = ops.fromImmutableTerminology(bundle)
    suuid = s.uuid.asInstanceOf[resolver.api.taggedTypes.BundleUUID]

    s_common_aps = s.annotationProperties intersect all_aps
    s_ap = s.annotationProperties -- s_common_aps

    // Check that there are no overlaping annotation properties
    _ = {
      if (s_common_aps.nonEmpty) {
        val common = s_common_aps.to[Seq].sortBy(_.abbrevIRI.toString)
        System.out.println(
          s"TerminologyGraph ${s.iri} duplicates ${common.size} Annotations defined in imported modules: " +
            common.map(_.abbrevIRI).mkString("\n\t",", ","\n"))
      }
    }

    allConceptDesignationTerminologyAxioms <-
    s.conceptDesignation.foldLeft(
      Seq.empty[oml.tables.ConceptDesignationTerminologyAxiom].right[Throwables]
    ) { case ( acc1, omf_ax) =>
      for {
        axs <- acc1
        omf_info = ops.fromConceptDesignationTerminologyAxiom(omf_ax)
        _ <- if (all_tboxes.contains(omf_info.designatedTerminology))
          ().right[Throwables]
        else
          Set[java.lang.Throwable](OMFError.omfError(
            s"TerminologyGraph ${s.iri} has a ConceptDesignationTerminologyAxiom (uuid=${omf_info.uuid}) "+
              s" whose designated terminology is not imported: ${ops.getModuleIRI(omf_info.designatedTerminology)}"))
            .left[Unit]
        ax = oml.tables.ConceptDesignationTerminologyAxiom(
          uuid = omf_info.uuid,
          tboxUUID = omf_info.graphUUID,
          designatedConceptUUID = ops.getConceptUUID(omf_info.designatedConcept),
          designatedTerminologyIRI = ops.getModuleIRI(omf_info.designatedTerminology))

      } yield axs :+ ax
    }

    allExtensionAxioms <-
    s.extensions.foldLeft(
      Seq.empty[oml.tables.TerminologyExtensionAxiom].right[Throwables]
    ) { case (acc1, omf_ax) =>
      for {
        axs <- acc1
        omf_info = ops.fromTerminologyExtensionAxiom(omf_ax)
        _ <- if (all_tboxes.contains(omf_info.extendedTerminology))
          ().right[Throwables]
        else
          Set[java.lang.Throwable](OMFError.omfError(
            s"TerminologyGraph ${s.iri} has a TerminologyExtensionAxiom (uuid=${omf_info.uuid}) "+
              s" whose extended terminology is not imported: ${ops.getModuleIRI(omf_info.extendedTerminology)}"))
            .left[Unit]
        ax = oml.tables.TerminologyExtensionAxiom(
          uuid = omf_info.uuid,
          tboxUUID = suuid,
          extendedTerminologyIRI = ops.getModuleIRI(omf_info.extendedTerminology))

      } yield axs :+ ax
    }

    allNestingAxioms <-
    s.nesting.foldLeft(
      Seq.empty[oml.tables.TerminologyNestingAxiom].right[Throwables]
    ) { case (acc1, omf_ax) =>
      for {
        axs <- acc1
        omf_info = ops.fromTerminologyNestingAxiom(omf_ax)
        _ <- if (all_tboxes.contains(omf_info.nestingTerminology))
          ().right[Throwables]
        else
          Set[java.lang.Throwable](OMFError.omfError(
            s"TerminologyGraph ${s.iri} has a TerminologyNestingAxiom (uuid=${omf_info.uuid}) "+
              s" whose nesting terminology is not imported: ${ops.getModuleIRI(omf_info.nestingTerminology)}"))
            .left[Unit]
        ax = oml.tables.TerminologyNestingAxiom(
          uuid = omf_info.uuid,
          tboxUUID = suuid,
          nestingTerminologyIRI = ops.getModuleIRI(omf_info.nestingTerminology),
          nestingContextUUID = ops.getConceptUUID(omf_info.nestingContext))

      } yield axs :+ ax
    }

    allBundledTerminologyAxioms <-
    s.bundledTerminologies.foldLeft(
      Seq.empty[oml.tables.BundledTerminologyAxiom].right[Throwables]
    ) { case (acc1, omf_ax) =>
      for {
        axs <- acc1
        omf_info = ops.fromBundledTerminologyAxiom(omf_ax)
        _ <- if (all_tboxes.contains(omf_info.bundledTerminology))
          ().right[Throwables]
        else
          Set[java.lang.Throwable](OMFError.omfError(
            s"Bundle ${s.iri} has a BundledTerminologyAxiom (uuid=${omf_info.uuid}) "+
              s" whose bundled terminology is not imported: ${ops.getModuleIRI(omf_info.bundledTerminology)}"))
            .left[Unit]
        ax = oml.tables.BundledTerminologyAxiom(
          uuid = omf_info.uuid,
          bundleUUID = suuid,
          bundledTerminologyIRI = ops.getModuleIRI(omf_info.bundledTerminology))

      } yield axs :+ ax
    }

    allAspects = s.aspects.map { a =>
      oml.tables.Aspect(
        tboxUUID = suuid,
        uuid = ops.getAspectUUID(a),
        name = ops.getTermName(a))
    }.to[Seq].sorted

    allConcepts = s.concepts.map { c =>
      oml.tables.Concept(
        tboxUUID = suuid,
        uuid = ops.getConceptUUID(c),
        name = ops.getTermName(c))
    }.to[Seq].sorted

    allReifiedRelationships = s.reifiedRelationships.map { rr =>
      val sig = ops.fromReifiedRelationship(rr)
      oml.tables.ReifiedRelationship(
        tboxUUID = suuid,
        uuid = sig.uuid,
        name = sig.name,
        unreifiedPropertyName = sig.unreifiedPropertyName,
        unreifiedInversePropertyName = sig.unreifiedInversePropertyName,
        isAsymmetric = sig.characteristics.exists(RelationshipCharacteristics.isAsymmetric == _),
        isEssential = sig.characteristics.exists(RelationshipCharacteristics.isEssential == _),
        isFunctional = sig.characteristics.exists(RelationshipCharacteristics.isFunctional == _),
        isInverseEssential = sig.characteristics.exists(RelationshipCharacteristics.isInverseEssential == _),
        isInverseFunctional = sig.characteristics.exists(RelationshipCharacteristics.isInverseFunctional == _),
        isIrreflexive = sig.characteristics.exists(RelationshipCharacteristics.isIrreflexive == _),
        isReflexive = sig.characteristics.exists(RelationshipCharacteristics.isReflexive == _),
        isSymmetric = sig.characteristics.exists(RelationshipCharacteristics.isSymmetric == _),
        isTransitive = sig.characteristics.exists(RelationshipCharacteristics.isTransitive == _),
        sourceUUID = ops.getEntityUUID(sig.source),
        targetUUID = ops.getEntityUUID(sig.target))
    }.to[Seq].sorted

    allUnreifiedRelationships = s.unreifiedRelationships.map { ur =>
      val sig = ops.fromUnreifiedRelationship(ur)
      oml.tables.UnreifiedRelationship(
        tboxUUID = suuid,
        uuid = sig.uuid,
        name = sig.name,
        isAsymmetric = sig.characteristics.exists(RelationshipCharacteristics.isAsymmetric == _),
        isEssential = sig.characteristics.exists(RelationshipCharacteristics.isEssential == _),
        isFunctional = sig.characteristics.exists(RelationshipCharacteristics.isFunctional == _),
        isInverseEssential = sig.characteristics.exists(RelationshipCharacteristics.isInverseEssential == _),
        isInverseFunctional = sig.characteristics.exists(RelationshipCharacteristics.isInverseFunctional == _),
        isIrreflexive = sig.characteristics.exists(RelationshipCharacteristics.isIrreflexive == _),
        isReflexive = sig.characteristics.exists(RelationshipCharacteristics.isReflexive == _),
        isSymmetric = sig.characteristics.exists(RelationshipCharacteristics.isSymmetric == _),
        isTransitive = sig.characteristics.exists(RelationshipCharacteristics.isTransitive == _),
        sourceUUID = ops.getEntityUUID(sig.source),
        targetUUID = ops.getEntityUUID(sig.target))
    }.to[Seq].sorted

    allScalars = s.scalarDataTypes.map { sc =>
      oml.tables.Scalar(
        tboxUUID = suuid,
        uuid = ops.getScalarUUID(sc),
        name = ops.getTermName(sc))
    }.to[Seq].sorted

    allStructures = s.structuredDataTypes.map { st =>
      oml.tables.Structure(
        tboxUUID = suuid,
        uuid = ops.getStructureUUID(st),
        name = ops.getTermName(st))
    }.to[Seq].sorted

    allBinaryScalarRestrictions = s.binaryScalarRestrictions.map { dr =>
      val info = ops.fromBinaryScalarRestriction(dr)
      oml.tables.BinaryScalarRestriction(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        length = info.length,
        maxLength = info.maxLength,
        minLength = info.minLength,
        restrictedRangeUUID = ops.getDataRangeUUID(info.restrictedRange))
    }.to[Seq].sorted

    allIRIScalarRestrictions = s.iriScalarRestrictions.map { dr =>
      val info = ops.fromIRIScalarRestriction(dr)
      oml.tables.IRIScalarRestriction(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        length = info.length,
        maxLength = info.maxLength,
        minLength = info.minLength,
        pattern = info.pattern.map(canonicalLiteralPattern),
        restrictedRangeUUID = ops.getDataRangeUUID(info.restrictedRange))
    }.to[Seq].sorted

    allNumericScalarRestrictions = s.numericScalarRestrictions.map { dr =>
      val info = ops.fromNumericScalarRestriction(dr)
      oml.tables.NumericScalarRestriction(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        maxExclusive = info.maxExclusive,
        maxInclusive = info.maxInclusive,
        minExclusive = info.minExclusive,
        minInclusive = info.minInclusive,
        restrictedRangeUUID = ops.getDataRangeUUID(info.restrictedRange))
    }.to[Seq].sorted

    allPlainLiteralScalarRestrictions = s.plainLiteralScalarRestrictions.map { dr =>
      val info = ops.fromPlainLiteralScalarRestriction(dr)
      oml.tables.PlainLiteralScalarRestriction(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        langRange = info.langRange,
        length = info.length,
        maxLength = info.maxLength,
        minLength = info.minLength,
        pattern = info.pattern.map(canonicalLiteralPattern),
        restrictedRangeUUID = ops.getDataRangeUUID(info.restrictedRange))
    }.to[Seq].sorted

    allScalarOneOfRestrictions = s.scalarOneOfRestrictions.map { dr =>
      val info = ops.fromScalarOneOfRestriction(dr)
      oml.tables.ScalarOneOfRestriction(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        restrictedRangeUUID = ops.getDataRangeUUID(info.restrictedRange))
    }.to[Seq].sorted

    allStringScalarRestrictions = s.stringScalarRestrictions.map { dr =>
      val info = ops.fromStringScalarRestriction(dr)
      oml.tables.StringScalarRestriction(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        length = info.length,
        maxLength = info.maxLength,
        minLength = info.minLength,
        pattern = info.pattern.map(canonicalLiteralPattern),
        restrictedRangeUUID = ops.getDataRangeUUID(info.restrictedRange))
    }.to[Seq].sorted

    allSynonymScalarRestrictions = s.synonymScalarRestrictions.map { dr =>
      val info = ops.fromSynonymScalarRestriction(dr)
      oml.tables.SynonymScalarRestriction(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        restrictedRangeUUID = ops.getDataRangeUUID(info.restrictedRange))
    }.to[Seq].sorted

    allTimeScalarRestrictions = s.timeScalarRestrictions.map { dr =>
      val info = ops.fromTimeScalarRestriction(dr)
      oml.tables.TimeScalarRestriction(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        maxExclusive = info.maxExclusive,
        maxInclusive = info.maxInclusive,
        minExclusive = info.minExclusive,
        minInclusive = info.minInclusive,
        restrictedRangeUUID = ops.getDataRangeUUID(info.restrictedRange))
    }.to[Seq].sorted

    allEntity2ScalarProperties = s.entityScalarDataProperties.map { e2sc =>
      val info = ops.fromEntityScalarDataProperty(e2sc)
      oml.tables.EntityScalarDataProperty(
        tboxUUID = suuid,
        uuid = info.uuid,
        domainUUID = ops.getEntityUUID(info.domain),
        rangeUUID = ops.getDataRangeUUID(info.range),
        isIdentityCriteria = info.isIdentityCriteria,
        name = info.name)
    }.to[Seq].sorted

    allEntity2StructureProperties = s.entityStructuredDataProperties.map { e2sc =>
      val info = ops.fromEntityStructuredDataProperty(e2sc)
      oml.tables.EntityStructuredDataProperty(
        tboxUUID = suuid,
        uuid = info.uuid,
        domainUUID = ops.getEntityUUID(info.domain),
        rangeUUID = ops.getStructureUUID(info.range),
        isIdentityCriteria = info.isIdentityCriteria,
        name = info.name)
    }.to[Seq].sorted

    allScalarProperties = s.scalarDataProperties.map { s2sc =>
      val info = ops.fromScalarDataProperty(s2sc)
      oml.tables.ScalarDataProperty(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        domainUUID = ops.getStructureUUID(info.domain),
        rangeUUID = ops.getDataRangeUUID(info.range))
    }.to[Seq].sorted

    allStructuredProperties = s.structuredDataProperties.map { s2sc =>
      val info = ops.fromStructuredDataProperty(s2sc)
      oml.tables.StructuredDataProperty(
        tboxUUID = suuid,
        uuid = info.uuid,
        name = info.name,
        domainUUID = ops.getStructureUUID(info.domain),
        rangeUUID = ops.getStructureUUID(info.range))
    }.to[Seq].sorted

    allAxioms = s.axioms.foldLeft(Axioms())(Axioms.combine(suuid, ops))

    allRootConceptTaxonomyAxioms = s.rTAxioms.map { ax =>
      val info = ops.fromRootConceptTaxonomyAxiom(ax)
      oml.tables.RootConceptTaxonomyAxiom(
        uuid = info.uuid,
        bundleUUID = suuid,
        rootUUID = ops.getConceptUUID(info.root))
    }.to[Seq].sorted

    allSpecificDisjointConceptAxioms = s.sTAxioms.map { ax =>
      val info = ops.fromSpecificDisjointConceptAxiom(ax)
      oml.tables.SpecificDisjointConceptAxiom(
        uuid = info.uuid,
        disjointLeafUUID = ops.getConceptUUID(info.disjointLeaf),
        disjointTaxonomyParentUUID = ops.getConceptTreeDisjunctionUUID(info.disjointTaxonomyParent))
    }.to[Seq].sorted

    allAnonymousConceptUnionAxioms = s.aTAxioms.map { ax =>
      val info = ops.fromAnonymousConceptTaxonomyAxiom(ax)
      oml.tables.AnonymousConceptUnionAxiom(
        uuid = info.uuid,
        disjointTaxonomyParentUUID = ops.getConceptTreeDisjunctionUUID(info.disjointTaxonomyParent),
        name = info.name)
    }.to[Seq].sorted

    table = oml.tables.OMLSpecificationTables(

        bundles = Seq(oml.tables.Bundle(
          uuid = suuid,
          kind = if (TerminologyKind.isOpenWorldKind(s.kind))
            oml.tables.OpenWorldDefinitions
          else
            oml.tables.ClosedWorldDesignations,
          iri = s.iri)),

        conceptDesignationTerminologyAxioms = allConceptDesignationTerminologyAxioms.sorted,
        terminologyExtensionAxioms = allExtensionAxioms.sorted,
        terminologyNestingAxioms = allNestingAxioms.sorted,
        bundledTerminologyAxioms = allBundledTerminologyAxioms,

        aspects = allAspects,
        concepts = allConcepts,
        reifiedRelationships = allReifiedRelationships,
        unreifiedRelationships = allUnreifiedRelationships,
        scalars = allScalars,
        structures = allStructures,
        binaryScalarRestrictions = allBinaryScalarRestrictions,
        iriScalarRestrictions = allIRIScalarRestrictions,
        numericScalarRestrictions = allNumericScalarRestrictions,
        plainLiteralScalarRestrictions = allPlainLiteralScalarRestrictions,
        scalarOneOfRestrictions = allScalarOneOfRestrictions,
        stringScalarRestrictions = allStringScalarRestrictions,
        synonymScalarRestrictions = allSynonymScalarRestrictions,
        timeScalarRestrictions = allTimeScalarRestrictions,
        entityScalarDataProperties = allEntity2ScalarProperties,
        entityStructuredDataProperties = allEntity2StructureProperties,
        scalarDataProperties = allScalarProperties,
        structuredDataProperties = allStructuredProperties,

        aspectSpecializationAxioms =
          allAxioms.aspectSpecializationAxioms.sorted,
        conceptSpecializationAxioms =
          allAxioms.conceptSpecializationAxioms.sorted,
        reifiedRelationshipSpecializationAxioms =
          allAxioms.reifiedRelationshipSpecializationAxioms.sorted,
        entityExistentialRestrictionAxioms =
          allAxioms.entityExistentialRestrictionAxioms.sorted,
        entityUniversalRestrictionAxioms =
          allAxioms.entityUniversalRestrictionAxioms.sorted,
        entityScalarDataPropertyExistentialRestrictionAxioms =
          allAxioms.entityScalarDataPropertyExistentialRestrictionAxioms.sorted,
        entityScalarDataPropertyParticularRestrictionAxioms =
          allAxioms.entityScalarDataPropertyParticularRestrictionAxioms.sorted,
        entityScalarDataPropertyUniversalRestrictionAxioms =
          allAxioms.entityScalarDataPropertyUniversalRestrictionAxioms.sorted,
        scalarOneOfLiteralAxioms =
          allAxioms.scalarOneOfLiteralAxioms.sorted,

        rootConceptTaxonomyAxioms = allRootConceptTaxonomyAxioms,
        specificDisjointConceptAxioms = allSpecificDisjointConceptAxioms,
        anonymousConceptUnionAxioms = allAnonymousConceptUnionAxioms,

        annotationProperties = s_ap.to[Seq].sorted,

        annotationPropertyValues = s.annotationPropertyValues.to[Seq].sorted
      )

  } yield im2st :+ (bundle -> table)

}
