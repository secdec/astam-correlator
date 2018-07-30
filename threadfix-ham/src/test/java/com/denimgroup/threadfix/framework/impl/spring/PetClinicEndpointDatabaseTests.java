////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.spring;

import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.data.enums.InformationSourceType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.TestConstants;
import com.denimgroup.threadfix.framework.engine.CodePoint;
import com.denimgroup.threadfix.framework.engine.DefaultCodePoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabase;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabaseFactory;
import com.denimgroup.threadfix.framework.engine.full.EndpointQuery;
import com.denimgroup.threadfix.framework.engine.full.EndpointQueryBuilder;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class PetClinicEndpointDatabaseTests {

    @Nullable
    private EndpointDatabase getSpringEndpointDatabaseDynamic() {
        File file = new File(TestConstants.PETCLINIC_SOURCE_LOCATION);

        return EndpointDatabaseFactory.getDatabase(file, FrameworkType.SPRING_MVC, new SpringPathCleaner("/petclinic", null));
    }

    @Test
    public void testPetClinicDynamicToStaticPathQueries() {

        EndpointDatabase db = getSpringEndpointDatabaseDynamic();

        for (String[] pair : dynamicToStaticTests) {
            String result = getStaticPath(db, pair[0]);
            assertTrue("Input: " + pair[0] + ", expected " + pair[1] + " but got " + result, result.equals(pair[1]));
        }
    }

    @Nonnull
    String[][] dynamicToStaticTests = new String[][] {
            { "/petclinic/owners", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners.html", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/{id}", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/3463", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/346323/edit", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/{id}/edit", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/find", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/new", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/3463", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/3463", "/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java" },
            { "/petclinic/owners/{id}/pets/{id}/visits/new", "/src/main/java/org/springframework/samples/petclinic/owner/VisitController.java" },
            { "/petclinic/owners/5/pets/2/visits/new", "/src/main/java/org/springframework/samples/petclinic/owner/VisitController.java" },
            { "/petclinic/owners/45683568/pets/6457247/visits/new", "/src/main/java/org/springframework/samples/petclinic/owner/VisitController.java" },
            { "/petclinic/oups", "/src/main/java/org/springframework/samples/petclinic/system/CrashController.java" },
            { "/petclinic/oups.html", "/src/main/java/org/springframework/samples/petclinic/system/CrashController.java" },
            { "/petclinic/owners/{id}/pets/{id}/edit", "/src/main/java/org/springframework/samples/petclinic/owner/PetController.java" },
            { "/petclinic/owners/5/pets/2/edit", "/src/main/java/org/springframework/samples/petclinic/owner/PetController.java" },
            { "/petclinic/owners/24562/pets/345724824/edit", "/src/main/java/org/springframework/samples/petclinic/owner/PetController.java" },
            { "/petclinic/vets.html", "/src/main/java/org/springframework/samples/petclinic/vet/VetController.java" },
            { "/petclinic/owners/{id}/pets/new", "/src/main/java/org/springframework/samples/petclinic/owner/PetController.java" },
            { "/petclinic/owners/36/pets/new", "/src/main/java/org/springframework/samples/petclinic/owner/PetController.java" },
    };

    @Nonnull
    private String getStaticPath(@Nonnull EndpointDatabase db, String dynamicPath) {
        EndpointQuery query = EndpointQueryBuilder.start()
                .setInformationSourceType(InformationSourceType.DYNAMIC)
                .setDynamicPath(dynamicPath)
                .generateQuery();

        return db.findBestMatch(query).getFilePath();
    }

    @Nonnull
    String[][] httpMethodTests = new String[][] {
            { "/petclinic/owners/new", "GET", "58" },
            { "/petclinic/owners/new", "POST", "65" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "GET", "97" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "POST", "104" },
            { "/petclinic/owners/{ownerId}/pets/new", "GET", "74" },
            { "/petclinic/owners/{ownerId}/pets/new", "POST", "82" },
            { "/petclinic/oups", "GET", "33" },
            { "/petclinic/oups", "POST", null },
            { "/petclinic/owners/find", "GET", "75" },
            { "/petclinic/owners/find", "POST", null },

            //  These endpoints don't seem to exist anymore in the test files
            //{ "/petclinic/owners/{ownerId}/pets/{petId}/visits", "GET", "79" },
            //{ "/petclinic/owners/{ownerId}/pets/{petId}/visits", "POST", null },

            { "/petclinic/owners/*/pets/{petId}/visits/new", "GET", "80" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/visits/new", "POST", "86" },
            { "/petclinic/owners/{ownerId}/edit", "GET", "106" },
            { "/petclinic/owners/{ownerId}/edit", "POST", "113" },
            { "/petclinic/owners", "GET", "81" },
            { "/petclinic/owners", "POST", null },
    };


    @Nullable
    private EndpointDatabase getSpringEndpointDatabaseStatic() {
        File file = new File(TestConstants.PETCLINIC_SOURCE_LOCATION);

        return EndpointDatabaseFactory.getDatabase(file, FrameworkType.SPRING_MVC, new SpringPathCleaner("/petclinic", null));
    }

    @Test
    public void testHttpMethodRecognition() {
        EndpointDatabase db = getSpringEndpointDatabaseStatic();

        for (String[] httpMethodTest : httpMethodTests) {
            EndpointQuery query =
                    EndpointQueryBuilder.start()
                        .setDynamicPath(httpMethodTest[0])
                        .setHttpMethod(httpMethodTest[1])
                        .generateQuery();

            Endpoint result = db.findBestMatch(query);

            String currentQuery = httpMethodTest[0] + ": " + httpMethodTest[1];

            if (result == null) {
                assertTrue("No result was found, but line " + httpMethodTest[2] + " was expected for " + currentQuery,
                        httpMethodTest[2] == null);
            } else {

                //String currentQuery = httpMethodTest[0] + ": " + httpMethodTest[1];

                assertTrue("Got an endpoint, but was not expecting one with " + currentQuery,
                        httpMethodTest[2] != null);

                Integer value = Integer.valueOf(httpMethodTest[2]);

                assertTrue("Got " + result.getStartingLineNumber() + " but was expecting " + value + " with " + currentQuery,
                        value.equals(result.getStartingLineNumber()));
            }
        }
    }


    // TODO once we figure out what's going on with parameters let's patch these up
    @Nonnull
    String[][] parameterTests = new String[][] {
            { "/petclinic/owners/new", null, "58" },
            { "/petclinic/owners/new", "lastName", "65" },
            { "/petclinic/owners/new", "city", "65" },
            { "/petclinic/owners/new", "firstName", "65" },
            { "/petclinic/owners/new", "telephone", "65" },
            { "/petclinic/owners/new", "pet.type.id", "65" },
            { "/petclinic/owners/new", "pet.name", "65" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "petId", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.pet.type.id", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.city", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.pet.type.name", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.firstName", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.id", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.id", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.id", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.telephone", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "name", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "type.name", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.address", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.firstName", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "birthDate", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.city", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.pet.name", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.birthDate", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.pet.id", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.lastName", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.type.id", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.name", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.type.name", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.telephone", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.address", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "owner.pet.owner.pet.birthDate", "104" },
            { "/petclinic/owners/{ownerId}/pets/{petId}/edit", "type.id", "104" },
            { "/petclinic/owners/{ownerId}/pets/new", "ownerId", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.pet.birthDate", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.city", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.lastName", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.pet.type.id", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.pet.name", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.id", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.pet.type.name", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.telephone", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.pet.id", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.address", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "name", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "type.name", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "owner.firstName", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "birthDate", "82" },
            { "/petclinic/owners/{ownerId}/pets/new", "type.id", "82" },

    };

    // TODO add parameter stuff
    @Test
    public void testParameterRecognition() {
        EndpointDatabase db = getSpringEndpointDatabaseStatic();

        for (String[] httpMethodTest : parameterTests) {
            EndpointQuery query =
                    EndpointQueryBuilder.start()
                            .setDynamicPath(httpMethodTest[0])
                            .setParameter(httpMethodTest[1])
                            .setHttpMethod(httpMethodTest[1] != null ? "POST" : "GET")
                            .generateQuery();

            Endpoint result = db.findBestMatch(query);

            String currentQuery = httpMethodTest[0] + ": " + httpMethodTest[1];

            if (result == null) {
                assertTrue("No result was found, but line " + httpMethodTest[2] + " was expected for " + currentQuery,
                        httpMethodTest[2] == null);
            } else {
                assertTrue("Got an endpoint, but was not expecting one with " + currentQuery,
                        httpMethodTest[2] != null);

                Integer value = Integer.valueOf(httpMethodTest[2]);

                assertTrue("Got " + result.getStartingLineNumber() + " but was expecting " + value + " with " + currentQuery,
                        value.equals(result.getStartingLineNumber()));
            }
        }
    }

    List<? extends CodePoint> basicModelElements = Arrays.asList(
            new DefaultCodePoint("java/org/springframework/samples/petclinic/owner/OwnerController.java",85,
                    "public String processFindForm(Owner owner, BindingResult result, Model model) {"),
            new DefaultCodePoint("java/org/springframework/samples/petclinic/owner/OwnerController.java", 93,
                    "Collection<Owner> results = this.clinicService.findOwnerByLastName(owner.getLastName());"),
            new DefaultCodePoint("java/org/springframework/samples/petclinic/owner/OwnerController.java", 93,
                    "Collection<Owner> results = this.clinicService.findOwnerByLastName(owner.getLastName());"),
            new DefaultCodePoint("java/org/springframework/samples/petclinic/owner/OwnerController.java", 72,
                    "return ownerRepository.findByLastName(lastName);"),
            new DefaultCodePoint("java/org/springframework/samples/petclinic/owner/OwnerRepository.java", 84,
                    "\"SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name like '\" + lastName + \"%'\",")
    );

    // TODO add parameter stuff
    @Test
    public void testCodePoints() {
        EndpointDatabase db = getSpringEndpointDatabaseStatic();

        EndpointQuery query = EndpointQueryBuilder.start()
                .setCodePoints(basicModelElements)
                .setStaticPath("java/org/springframework/samples/petclinic/owner/OwnerRepository.java")
                .generateQuery();

        Endpoint result = db.findBestMatch(query);

        assertTrue("Result was null!", result != null);
    }



}
