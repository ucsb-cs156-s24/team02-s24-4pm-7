package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {
    
        @MockBean
        UCSBOrganizationsRepository ucsbOrganizationsRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsborganizations/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?code=munger-hall"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findById(eq("munger-hall"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganizations with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganizations org1 = UCSBOrganizations.builder()
                                .orgCode("ORG1")
                                .orgTranslationShort("1")
                                .orgTranslation("Organization one")
                                .inactive(false)
                                .build();

                UCSBOrganizations org2 = UCSBOrganizations.builder()
                                .orgCode("CSSA")
                                .orgTranslationShort("CSSA")
                                .orgTranslation("Chinese Students and Scholars Association")
                                .inactive(false)
                                .build();

                ArrayList<UCSBOrganizations> expectedOrgs = new ArrayList<>();
                expectedOrgs.addAll(Arrays.asList(org1, org2));

                when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedOrgs);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrgs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
        
        // Tests for POST /api/ucsborganizations...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganizations/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganizations/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_orgs() throws Exception {
                // arrange

                UCSBOrganizations org = UCSBOrganizations.builder()
                                .orgCode("ORG1")
                                .orgTranslationShort("01")
                                .orgTranslation("Organization1")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationsRepository.save(eq(org))).thenReturn(org);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganizations/post?name=org&orgCode=ORG1&orgTranslationShort=01&orgTranslation=Organization1&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).save(org);
                String expectedJson = mapper.writeValueAsString(org);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/ucsborganizations?...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations?code=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganizations org = UCSBOrganizations.builder()
                                .orgCode("ORG")
                                .orgTranslationShort("01")
                                .orgTranslation("Organization1")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationsRepository.findById(eq("ORG"))).thenReturn(Optional.of(org));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?code=ORG"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findById(eq("ORG"));
                String expectedJson = mapper.writeValueAsString(org);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for DELETE /api/ucsborganizations?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_org() throws Exception {
                // arrange

                UCSBOrganizations cssa = UCSBOrganizations.builder()
                                .orgCode("CSSA")
                                .orgTranslationShort("CSSA")
                                .orgTranslation("Chinese Students and Scholars Association")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationsRepository.findById(eq("CSSA"))).thenReturn(Optional.of(cssa));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?code=CSSA")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("CSSA");
                verify(ucsbOrganizationsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id CSSA deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_orgs_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?code=munger-hall")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id munger-hall not found", json.get("message"));
        }

        // Tests for PUT /api/ucsborganizations?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_orgs() throws Exception {
                // arrange

                UCSBOrganizations cssa = UCSBOrganizations.builder()
                                .orgCode("CSSA")
                                .orgTranslationShort("CSSA")
                                .orgTranslation("Chinese Students and Scholars Association")
                                .inactive(true)
                                .build();

                UCSBOrganizations cssaEdited = UCSBOrganizations.builder()
                                .orgCode("CSSA")
                                .orgTranslationShort("Chinese SSA")
                                .orgTranslation("Chinese Students and Scholars")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(cssaEdited);

                when(ucsbOrganizationsRepository.findById(eq("CSSA"))).thenReturn(Optional.of(cssa));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganizations?code=CSSA")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("CSSA");
                verify(ucsbOrganizationsRepository, times(1)).save(cssaEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_orgs_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganizations editedOrgs = UCSBOrganizations.builder()
                                .orgCode("CSSA")
                                .orgTranslationShort("CHinese SSA")
                                .orgTranslation("Chinese Students and Scholars Association")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(editedOrgs);

                when(ucsbOrganizationsRepository.findById(eq("CSSA"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganizations?code=CSSA")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("CSSA");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id CSSA not found", json.get("message"));

        }
}
