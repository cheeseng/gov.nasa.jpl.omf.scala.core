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

package gov.nasa.jpl.omf.scala.core

import gov.nasa.jpl.imce.oml.resolver
import gov.nasa.jpl.imce.oml.tables.{taggedTypes,AnnotationProperty,AnnotationPropertyValue}

import scala.collection.immutable.Set

case class DescriptionBoxSignature[omf <: OMF[omf], +S[A] <: scala.collection.Iterable[A]]
( override val uuid: resolver.api.taggedTypes.DescriptionBoxUUID,
  override val name: taggedTypes.LocalName,
  override val iri: omf#IRI,
  kind: DescriptionKind,

  descriptionBoxRefinements: S[omf#DescriptionBoxRefinement],
  closedWorldDefinitions: S[omf#DescriptionBoxExtendsClosedWorldDefinitions],
  conceptInstances: S[omf#ConceptInstance],
  reifiedRelationshipInstances: S[omf#ReifiedRelationshipInstance],
  reifiedRelationshipInstanceDomains: S[omf#ReifiedRelationshipInstanceDomain],
  reifiedRelationshipInstanceRanges: S[omf#ReifiedRelationshipInstanceRange],
  unreifiedRelationshipInstanceTuples: S[omf#UnreifiedRelationshipInstanceTuple],
  singletonScalarDataPropertyValues: S[omf#SingletonInstanceScalarDataPropertyValue],
  singletonStructuredDataPropertyValues: S[omf#SingletonInstanceStructuredDataPropertyValue],
  scalarDataPropertyValues: S[omf#ScalarDataPropertyValue],
  structuredDataPropertyTuples: S[omf#StructuredDataPropertyTuple],

  override val annotationPropertyValues: S[AnnotationPropertyValue],
  override val annotationProperties: S[AnnotationProperty])
  extends ModuleSignature[omf]  {

  override def importedTerminologies
  (implicit ops: OMFOps[omf])
  : Set[omf#IRI]
  = Set.empty[omf#IRI] ++
    closedWorldDefinitions.map(ops.fromClosedWorldDefinitionsAxiom(_).extendedClosedWorldDefinitions)

  override def importedDescriptions
  (implicit ops: OMFOps[omf])
  : Set[omf#IRI]
  = Set.empty[omf#IRI] ++
    descriptionBoxRefinements.map(ops.fromDescriptionBoxRefinementAxiom(_).refinedDescriptionBox)

}
