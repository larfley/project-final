package com.javarush.jira.profile.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.profile.ProfileTo;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProfileRestController.class)
public class ProfileRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AbstractProfileController abstractProfileController;

    @InjectMocks
    private ProfileRestController profileRestController;

    @Test
    @WithMockUser(username = "testUser", password = "testPassword", roles = "USER")
    public void testGetProfileSuccess() throws Exception {
        // Arrange
        AuthUser authUser = new AuthUser("testUser", "testPassword", "USER");
        ProfileTo profileTo = new ProfileTo(); // Replace with the actual constructor of your ProfileTo
        when(abstractProfileController.get(authUser.id())).thenReturn(profileTo);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(ProfileRestController.REST_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect((ResultMatcher) jsonPath("$.yourProperty").value("yourExpectedValue"));

        verify(abstractProfileController, times(1)).get(authUser.id());
    }

    @Test
    @WithMockUser(username = "testUser", password = "testPassword", roles = "USER")
    public void testGetProfileNotFound() throws Exception {
        // Arrange
        AuthUser authUser = new AuthUser("testUser", "testPassword", "USER");
        when(abstractProfileController.get(authUser.id())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(ProfileRestController.REST_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(abstractProfileController, times(1)).get(authUser.id());
    }

    @Test
    @WithMockUser(username = "testUser", password = "testPassword", roles = "USER")
    public void testUpdateProfileSuccess() throws Exception {
        // Arrange
        AuthUser authUser = new AuthUser("testUser", "testPassword", "USER");
        ProfileTo profileTo = new ProfileTo(); // Replace with the actual constructor of your ProfileTo
        ObjectMapper objectMapper = new ObjectMapper();
        String profileJson = objectMapper.writeValueAsString(profileTo);
        doNothing().when(abstractProfileController).update(eq(profileTo), eq(authUser.id()));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson))
                .andExpect(status().isNoContent());

        verify(abstractProfileController, times(1)).update(eq(profileTo), eq(authUser.id()));
    }

    @Test
    @WithMockUser(username = "testUser", password = "testPassword", roles = "USER")
    public void testUpdateProfileValidationError() throws Exception {
        // Arrange
        AuthUser authUser = new AuthUser("testUser", "testPassword", "USER");
        ProfileTo profileTo = new ProfileTo(); // Replace with the actual constructor of your ProfileTo and set it up to be invalid
        ObjectMapper objectMapper = new ObjectMapper();
        String profileJson = objectMapper.writeValueAsString(profileTo);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson))
                .andExpect(status().isBadRequest());

        verify(abstractProfileController, never()).update(eq(profileTo), eq(authUser.id()));
    }
}