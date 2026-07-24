package com.training.starter.security;

import com.training.starter.controller.AppointmentController;
import com.training.starter.controller.DoctorController;
import com.training.starter.controller.PatientController;
import com.training.starter.controller.UserController;
import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.request.CreateDoctorRequest;
import com.training.starter.dto.request.CreatePatientRequest;
import com.training.starter.dto.request.CreateUserRequest;
import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.dto.response.DoctorResponse;
import com.training.starter.dto.response.PatientResponse;
import com.training.starter.dto.response.UserResponse;
import com.training.starter.service.AccessTokenBlacklistStore;
import com.training.starter.service.AppointmentService;
import com.training.starter.service.DoctorService;
import com.training.starter.service.PatientService;
import com.training.starter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        UserController.class,
        PatientController.class,
        AppointmentController.class,
        DoctorController.class
})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ApiAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AccessTokenBlacklistStore accessTokenBlacklistStore;

    @Test
    void anonymousRequestsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotManageUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanManageUsers() throws Exception {
        when(userService.getAll(any())).thenReturn(new PageImpl<>(List.of(userResponse())));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanOnlyReadPatients() throws Exception {
        when(patientService.search(any(), any())).thenReturn(new PageImpl<>(List.of(patientResponse())));

        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/patients")
                        .contentType("application/json")
                        .content(createPatientJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanManagePatients() throws Exception {
        when(patientService.create(any(CreatePatientRequest.class))).thenReturn(patientResponse());

        mockMvc.perform(post("/api/v1/patients")
                        .contentType("application/json")
                        .content(createPatientJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanOnlyReadAppointments() throws Exception {
        when(appointmentService.search(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(appointmentResponse())));

        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/appointments/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanManageAppointments() throws Exception {
        when(appointmentService.create(any(CreateAppointmentRequest.class))).thenReturn(appointmentResponse());

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content(createAppointmentJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousCanReadDoctors() throws Exception {
        when(doctorService.getAll(any(), any())).thenReturn(new PageImpl<>(List.of(doctorResponse())));

        mockMvc.perform(get("/api/v1/doctors"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateDoctor() throws Exception {
        mockMvc.perform(post("/api/v1/admin/doctors")
                        .contentType("application/json")
                        .content(createDoctorJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateDoctor() throws Exception {
        when(doctorService.create(any(CreateDoctorRequest.class))).thenReturn(doctorResponse());

        mockMvc.perform(post("/api/v1/admin/doctors")
                        .contentType("application/json")
                        .content(createDoctorJson()))
                .andExpect(status().isCreated());
    }

    private UserResponse userResponse() {
        return new UserResponse(1L, "admin", "admin@example.com", "Admin", "ADMIN", "ACTIVE", true,
                LocalDateTime.now());
    }

    private PatientResponse patientResponse() {
        return new PatientResponse(1L, "Nguyen Van A", "0900000000", "patient@example.com",
                LocalDate.of(1990, 1, 1), "MALE", "HCM", null, LocalDateTime.now());
    }

    private AppointmentResponse appointmentResponse() {
        return new AppointmentResponse(1L, 1L, "Nguyen Van A", LocalDateTime.now().plusDays(1),
                "Consultation", "SCHEDULED", null, LocalDateTime.now());
    }

    private DoctorResponse doctorResponse() {
        return new DoctorResponse(1L, 10L, "Dr. Lisa Martin", "Cardiology", "A101",
                24, true, LocalDateTime.now());
    }

    private String createPatientJson() {
        return """
                {
                  "fullName": "Nguyen Van A",
                  "phone": "0900000000",
                  "email": "patient@example.com",
                  "dateOfBirth": "1990-01-01",
                  "gender": "MALE",
                  "address": "HCM",
                  "medicalNote": null
                }
                """;
    }

    private String createAppointmentJson() {
        return """
                {
                  "patientId": 1,
                  "scheduledAt": "2030-01-01T09:00:00",
                  "reason": "Consultation",
                  "status": "SCHEDULED",
                  "note": null
                }
                """;
    }

    private String createDoctorJson() {
        return """
                {
                  "userId": 10,
                  "specialty": "Cardiology",
                  "roomNumber": "A101",
                  "maxPatientsPerDay": 24,
                  "active": true
                }
                """;
    }
}
