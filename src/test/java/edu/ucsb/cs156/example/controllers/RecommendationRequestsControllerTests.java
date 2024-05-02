package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import liquibase.pro.packaged.L;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestsController.class)
@Import(TestConfig.class)
public class RecommendationRequestsControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRequestRepository RecommendationRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/recommendationrequests/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_RecommendationRequests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                RecommendationRequest recommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("gaucho1@ucsb.edu")
                                .professorEmail("prof1@ucsb.edu")
                                .explanation("I need a recommendation for grad school 1")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt2)
                                .done(true)
                                .build();

                LocalDateTime ldt3 = LocalDateTime.parse("2022-03-12T00:00:00");
                LocalDateTime ldt4 = LocalDateTime.parse("2022-03-13T00:00:00");

                RecommendationRequest recommendationRequest2 = RecommendationRequest.builder()
                                .requesterEmail("gaucho2@ucsb.edu")
                                .professorEmail("prof2@ucsb.edu")
                                .explanation("I need a recommendation for grad school 2")
                                .dateRequested(ldt3)
                                .dateNeeded(ldt4)
                                .done(false)
                                .build();

                ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
                expectedRequests.addAll(Arrays.asList(recommendationRequest1, recommendationRequest2));

                when(RecommendationRequestRepository.findAll()).thenReturn(expectedRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRequests);
                String responseString = response.getResponse().getContentAsString();
                System.out.println("expectedJson=\n" + expectedJson);
                System.out.println("responseString=\n" + responseString);
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/recommendationrequests/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationrequests/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationrequests/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_RecommendationRequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                RecommendationRequest recommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("gaucho1@ucsb.edu")
                                .professorEmail("prof1@ucsb.edu")
                                .explanation("I need a recommendation for grad school 1")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt2)
                                .done(true)
                                .build();

                when(RecommendationRequestRepository.save(eq(recommendationRequest1))).thenReturn(recommendationRequest1);

                // act
                MvcResult response = mockMvc.perform(

                                //post("/api/recommendationrequests/post?name=firstDayOfClasses&quarterYYYYQ=20222&localDateTime=2022-01-03T00:00:00")
                                post("/api/recommendationrequests/post?requesterEmail=gaucho1@ucsb.edu&professorEmail=prof1@ucsb.edu&explanation=I need a recommendation for grad school 1&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-03-11T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RecommendationRequestRepository, times(1)).save(recommendationRequest1);
                String expectedJson = mapper.writeValueAsString(recommendationRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/recommendationrequests?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/recommendationrequests?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                        .requesterEmail("gaucho1@ucsb.edu")
                        .professorEmail("prof1@ucsb.edu")
                        .explanation("I need a recommendation for grad school 1")
                        .dateRequested(ldt1)
                        .dateNeeded(ldt2)
                        .done(true)
                        .build();

                when(RecommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(recommendationRequest));

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(recommendationRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(RecommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
        }


//         // Tests for DELETE /api/recommendationrequests?id=... 

//         @WithMockUser(roles = { "ADMIN", "USER" })
//         @Test
//         public void admin_can_delete_a_date() throws Exception {
//                 // arrange

//                 LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

//                 RecommendationRequest recommendationRequest1 = RecommendationRequest.builder()
//                                 .name("firstDayOfClasses")
//                                 .quarterYYYYQ("20222")
//                                 .localDateTime(ldt1)
//                                 .build();

//                 when(RecommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(recommendationRequest1));

//                 // act
//                 MvcResult response = mockMvc.perform(
//                                 delete("/api/recommendationrequests?id=15")
//                                                 .with(csrf()))
//                                 .andExpect(status().isOk()).andReturn();

//                 // assert
//                 verify(RecommendationRequestRepository, times(1)).findById(15L);
//                 verify(RecommendationRequestRepository, times(1)).delete(any());

//                 Map<String, Object> json = responseToJson(response);
//                 assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
//         }
        
//         @WithMockUser(roles = { "ADMIN", "USER" })
//         @Test
//         public void admin_tries_to_delete_non_existant_RecommendationRequest_and_gets_right_error_message()
//                         throws Exception {
//                 // arrange

//                 when(RecommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

//                 // act
//                 MvcResult response = mockMvc.perform(
//                                 delete("/api/recommendationrequests?id=15")
//                                                 .with(csrf()))
//                                 .andExpect(status().isNotFound()).andReturn();

//                 // assert
//                 verify(RecommendationRequestRepository, times(1)).findById(15L);
//                 Map<String, Object> json = responseToJson(response);
//                 assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
//         }

//         // Tests for PUT /api/recommendationrequests?id=... 

//         @WithMockUser(roles = { "ADMIN", "USER" })
//         @Test
//         public void admin_can_edit_an_existing_RecommendationRequest() throws Exception {
//                 // arrange

//                 LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
//                 LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

//                 RecommendationRequest RecommendationRequestOrig = RecommendationRequest.builder()
//                                 .name("firstDayOfClasses")
//                                 .quarterYYYYQ("20222")
//                                 .localDateTime(ldt1)
//                                 .build();

//                 RecommendationRequest RecommendationRequestEdited = RecommendationRequest.builder()
//                                 .name("firstDayOfFestivus")
//                                 .quarterYYYYQ("20232")
//                                 .localDateTime(ldt2)
//                                 .build();

//                 String requestBody = mapper.writeValueAsString(RecommendationRequestEdited);

//                 when(RecommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(RecommendationRequestOrig));

//                 // act
//                 MvcResult response = mockMvc.perform(
//                                 put("/api/recommendationrequests?id=67")
//                                                 .contentType(MediaType.APPLICATION_JSON)
//                                                 .characterEncoding("utf-8")
//                                                 .content(requestBody)
//                                                 .with(csrf()))
//                                 .andExpect(status().isOk()).andReturn();

//                 // assert
//                 verify(RecommendationRequestRepository, times(1)).findById(67L);
//                 verify(RecommendationRequestRepository, times(1)).save(RecommendationRequestEdited); // should be saved with correct user
//                 String responseString = response.getResponse().getContentAsString();
//                 assertEquals(requestBody, responseString);
//         }

        
//         @WithMockUser(roles = { "ADMIN", "USER" })
//         @Test
//         public void admin_cannot_edit_RecommendationRequest_that_does_not_exist() throws Exception {
//                 // arrange

//                 LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

//                 RecommendationRequest ucsbEditedDate = RecommendationRequest.builder()
//                                 .name("firstDayOfClasses")
//                                 .quarterYYYYQ("20222")
//                                 .localDateTime(ldt1)
//                                 .build();

//                 String requestBody = mapper.writeValueAsString(ucsbEditedDate);

//                 when(RecommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

//                 // act
//                 MvcResult response = mockMvc.perform(
//                                 put("/api/recommendationrequests?id=67")
//                                                 .contentType(MediaType.APPLICATION_JSON)
//                                                 .characterEncoding("utf-8")
//                                                 .content(requestBody)
//                                                 .with(csrf()))
//                                 .andExpect(status().isNotFound()).andReturn();

//                 // assert
//                 verify(RecommendationRequestRepository, times(1)).findById(67L);
//                 Map<String, Object> json = responseToJson(response);
//                 assertEquals("RecommendationRequest with id 67 not found", json.get("message"));

//         }
}
