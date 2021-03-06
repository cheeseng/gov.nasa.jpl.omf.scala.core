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

package test.gov.nasa.jpl.omf.scala.core.functionalAPI

import gov.nasa.jpl.imce.oml.tables.taggedTypes.localName
import gov.nasa.jpl.omf.scala.core._
import gov.nasa.jpl.omf.scala.core.TerminologyKind._
import org.scalatest.{Matchers, WordSpec}

abstract class IMCEMissionDomainTBoxExample[omf <: OMF[omf]]()(
  implicit val ops: OMFOps[omf],
  implicit val store: omf#Store )
  extends WordSpec with Matchers {

  import ops._

  "basic construction tests" when {
    "empty tbox should be empty" in {

      val result =
        for {
          i_m0 <- makeIRI( "http://imce.jpl.nasa.gov/foundation/mission/mission0" )
          g <- makeTerminologyGraph(i_m0, isOpenWorld)
          s = ops.fromMutableTerminology(g)
        } yield {
          s.importedTerminologies.isEmpty should be(true)
          s.aspects.isEmpty should be(true)
          s.concepts.isEmpty should be(true)
          s.reifiedRelationships.isEmpty should be(true)
          s.unreifiedRelationships.isEmpty should be(true)
          s.structuredDataTypes.isEmpty should be(true)
          s.scalarDataTypes.isEmpty should be(true)
          s.entityScalarDataProperties.isEmpty should be(true)
          s.entityStructuredDataProperties.isEmpty should be(true)
          s.scalarDataProperties.isEmpty should be(true)
          s.structuredDataProperties.isEmpty should be(true)
          s.axioms.isEmpty should be(true)
        }
      result.isRight should be(true)
    }

    "simple construction & lookup" in {

      val result =
        for {
          i_m1 <- makeIRI( "http://imce.jpl.nasa.gov/foundation/mission/mission1" )
          g <- makeTerminologyGraph(i_m1, isOpenWorld)
          component <- addConcept(g, localName("Component"))
          function <- addConcept(g, localName("Function"))
          s = fromMutableTerminology(g)
        } yield {
          s.iri should be(i_m1)
          s.importedTerminologies.isEmpty should be(true)
          s.aspects.isEmpty should be(true)

          s.concepts.nonEmpty should be(true)
          s.concepts.size should be(2)
          s.concepts.toSet.contains(component) should be(true)
          s.concepts.toSet.contains(function) should be(true)

          s.reifiedRelationships.isEmpty should be(true)
          s.scalarDataTypes.isEmpty should be(true)
          s.structuredDataTypes.isEmpty should be(true)
          s.entityScalarDataProperties.isEmpty should be(true)
          s.entityStructuredDataProperties.isEmpty should be(true)
          s.scalarDataProperties.isEmpty should be(true)
          s.structuredDataProperties.isEmpty should be(true)
          s.axioms.isEmpty should be(true)
        }
      result.isRight should be(true)
    }
  }

}