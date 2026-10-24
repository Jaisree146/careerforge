package com.careerforge.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 / Swagger UI Configuration for CareerForge.
 * Configures interactive API exploration, JWT Bearer Token authentication schema,
 * servers, contact information, and metadata.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI careerForgeOpenAPI(@Value("${server.port:8080}") String serverPort) {
        return new OpenAPI()
                .info(new Info()
                        .title("CareerForge Platform API")
                        .version("1.0.0")
                        .description("### Enterprise AI-Powered Job & Recruitment Platform\n\n"
                                + "Comprehensive REST API suite covering:\n"
                                + "- **Authentication & RBAC:** JWT bearer tokens for CANDIDATE, RECRUITER, and ADMIN roles.\n"
                                + "- **Profile Management:** Candidates, Recruiters, Companies, and Locations.\n"
                                + "- **Job Lifecycle:** Posting, updating, status changes, and multi-skill associations.\n"
                                + "- **Applications:** Multi-status applicant tracking (SUBMITTED, UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, OFFERED, REJECTED, WITHDRAWN) and saved jobs.\n"
                                + "- **AI Multi-Factor Matching Engine:** Weighted compatibility scoring across skills (60%), experience (25%), and profile completeness (15%).\n"
                                + "- **Event-Driven Notifications & Audit Logs:** Real-time event notifications and security audit logs.\n"
                                + "- **Recruitment Analytics:** Funnel conversion rates, time-to-hire, skill supply/demand, and platform KPIs.\n"
                                + "- **Google Gemini Generative AI:** Job description generation, resume parsing, tailored interview questions, and candidate job advice."
                        )
                        .contact(new Contact()
                                .name("CareerForge Engineering Team")
                                .email("engineering@careerforge.com")
                                .url("https://github.com/Jaisree146/careerforge"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from `/api/v1/auth/login` or `/api/v1/auth/register`.")
                        ));
    }
}
