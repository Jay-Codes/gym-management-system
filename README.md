# Gym Services Management System
## Overview

This a Spring Boot-based application designed to manage gym operations (member management, invoicing, discount handling, SMS notifications and reporting). It is a backend for gym administrators and users to handle memberships, generate invoices, send automated SMS notifications and export reports in PDF and Excel formats. The application integrates with a MySQL database, uses JWT for authentication and supports SMS communication via the Beem Africa API. It also includes Swagger for API documentation and Thymeleaf for HTML templating.

## Table of Contents

1. [Project Structure](#project-structure)
2. [Features](#features)
3. [Architecture and Components](#architecture-and-components)
4. [Endpoints](#endpoints)
   - [AuthController](#authcontroller)
   - [CompanyProfileController](#companyprofilecontroller)
   - [DiscountController](#discountcontroller)
   - [InvoiceController](#invoicecontroller)
   - [MemberController](#membercontroller)
   - [MixedController](#mixedcontroller)
   - [PackageController](#packagecontroller)
   - [ReportController](#reportcontroller)
   - [SmsController](#smscontroller)
5. [Assumptions and Notes](#assumptions-and-notes)

## Project Structure

The project follows a standard Maven structure with a clear separation of concerns:

```
.
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.jerrycode.gym_services
│   │   │       ├── GymServicesApplication.java
│   │   │       ├── business
│   │   │       │   ├── controller
│   │   │       │   ├── service
│   │   │       │   └── task
│   │   │       ├── comms
│   │   │       │   └── system
│   │   │       ├── config
│   │   │       ├── data
│   │   │       │   ├── dao
│   │   │       │   ├── dto
│   │   │       │   └── vo
│   │   │       ├── exception
│   │   │       ├── request
│   │   │       ├── response
│   │   │       └── utils
│   │   └── resources
│   │       ├── application.properties
│   │       ├── static
│   │       │   ├── css
│   │       │   └── storage
│   │       └── templates
│   └── test
└── uploads
```

- **business**: Contains controllers, services and scheduled tasks for business logic.
- **comms**: Includes commented-out Kafka/Debezium integration for event streaming.
- **config**: Configuration classes for security, JWT, Swagger and data initialization.
- **data**: Data access objects (DAO), data transfer objects (DTO) and value objects (VO).
- **exception**: Custom exception handling.
- **request/response**: Request and response DTOs for API communication.
- **utils**: Utility classes for PDF generation, Excel export, SMS handling and enums.
- **resources**: Configuration files, static assets (CSS, logos) and Thymeleaf templates.
- **test**: Unit tests for the application.

## Features

1. **Member Management**: CRUD gym members.
2. **Invoice Management**: CRUD PDF reports for invoices.
3. **Discount Management**: Manage discounts, calculate discounts for packages and their status.
4. **Company Profile**: Manage gym's profile(logo and contact details).
5. **SMS Notifications**: Send automated SMS for payment confirmations, reminders and welcoming texts using Beem Africa API.
6. **Reporting**: Generate monthly reports and export member invoices in Excel format.
7. **Authentication**: JWT-based authentication with role-based access (admin, user).
8. **API Documentation**: Swagger UI for API exploration.
9. **PDF Generation**: Generate styled PDF invoices using Thymeleaf and OpenHTMLtoPDF.
10. **File Upload**: Handle logo uploads for company profiles.

## Architecture and Components

### Core Components

1. **Controllers** (`business/controller`):
   - Handle HTTP requests and responses.
   - Delegate to service layer for business logic.

2. **Services** (`business/service`):
   - Implement business logic.

3. **Repositories** (`data/dao`):
   - Interface with the MySQL database using Spring Data JPA.

4. **Value Objects** (`data/vo`):
   - Represent domain entities (e.g., `Member`, `Invoices`, `CompanyProfile`).

5. **DTOs** (`data/dto`):
   - Used for data transfer between layers (e.g., `MemberDTO`, `InvoicesDTO`).

6. **Request/Response Objects** (`request`, `response`):
   - Define input (`MemberRequest`) and output (`MemberResponse`) structures for APIs.

7. **Utilities** (`utils`):
   - `PDFGenerator`: Converts Thymeleaf templates to PDF invoices.
   - `ExcelExporter`: Exports reports to Excel.
   - `SmsClient`: Handles SMS communication via Beem Africa API.
   - Enums (`Status`, `Role`, `TemplateType`): Define constants for application states.

8. **Configuration** (`config`):
   - `SecurityConfig`: Configures JWT-based security and CORS.
   - `SwaggerConfig`: Sets up Swagger UI.
   - Data initializers: Preload data for members, invoices, etc.

9. **Templates** (`resources/templates`):
   - Thymeleaf template (`invoice-template.html`) for PDF generation.

10. **Static Assets** (`resources/static`):
    - CSS (`invoice-styles.css`) for styling invoices.
    - Storage for uploaded logos.

## Endpoints

### AuthController

**Base Path**: `/api/`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/login` | POST | Any | `LoginRequest` (email, password) | Validates credentials, generates JWT token | `Response<UserResponse>` (user details, token) | Authenticates user and returns JWT. |
| `/logout` | POST | Any | Authorization header (JWT) | Invalidates token | `Response<UserResponse>` | Logs out user by invalidating token. |
| `/add-user` | POST | admin | `UserRequest` (user details) | Creates new user with role | `Response<UserResponse>` | Adds a new user. |
| `/all-users` | GET | admin | None | Retrieves all users | `Response<List<UserResponse>>` | Lists all users. |
| `/user/{id}` | GET | admin, user | Path: `id` | Fetches user by ID | `Response<UserResponse>` | Retrieves user details. |
| `/user/{id}/update` | PUT | admin, user | Path: `id`, Body: `UserRequest` | Updates user details | `Response<UserResponse>` | Updates user information. |
| `/user/{id}/delete` | DELETE | admin | Path: `id` | Deletes user | `Response<UserResponse>` | Deletes a user. |
| `/password/{id}/update` | PUT | admin, user | Path: `id`, Body: `PasswordUpdateRequest` | Updates user password | `Response<UserResponse>` | Changes user password. |
| `/profile/{id}/image` | POST | admin, user | Path: `id`, Multipart: `image` | Uploads profile image | `Response<UserResponse>` | Updates user profile image. |
| `/profile/{id}` | GET | admin, user | Path: `id` | Fetches user profile | `Response<UserResponse>` | Retrieves user profile details. |

**Notes**:
- All endpoints except `/login` and `/logout` require JWT authentication.
- `RoleCheck` annotation enforces role-based access.
- Responses are wrapped in `Response` objects with `success` and `message` fields.

### CompanyProfileController

**Base Path**: `/api/company`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/` | POST | Any | `CompanyProfileRequest`, Multipart: `logo` | Updates or creates company profile with logo | `Response<CompanyProfileResponse>` | Manages company profile and logo. |
| `/` | GET | Any | None | Retrieves company profile | `Response<CompanyProfileResponse>` | Fetches company details. |
| `/` | DELETE | Any | None | Deletes company profile | `Response<CompanyProfileResponse>` | Removes company profile. |

**Notes**:
- Logo is stored in `uploads/logos` and converted to Base64 for PDF rendering.
- Uses `FileStorageService` for handling file uploads.

### DiscountController

**Base Path**: `/api`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/discount` | POST | Any | `DiscountRequest` | Creates a new discount | `Response<DiscountResponse>` | Adds a discount. |
| `/calculate-discount` | POST | Any | Query: `packageId`, `discountId` (optional) | Calculates discount for a package | `Response<Map<String, Double>>` | Returns discounted amount. |
| `/all-discounts` | GET | Any | None | Retrieves all discounts | `Response<List<DiscountResponse>>` | Lists all discounts. |
| `/active-discounts` | GET | Any | None | Retrieves active discounts | `Response<List<DiscountResponse>>` | Lists active discounts. |
| `/discount/{id}/update` | PUT | Any | Path: `id`, Body: `DiscountRequest` | Updates discount | `Response<DiscountResponse>` | Modifies discount details. |
| `/discount/{id}/toggle` | PUT | Any | Path: `id` | Toggles discount status | `Response<DiscountResponse>` | Activates/deactivates discount. |
| `/discount/{id}/delete` | DELETE | Any | Path: `id` | Deletes discount | `Response<DiscountResponse>` | Removes a discount. |

**Notes**:
- Discounts are applied to packages for invoicing.
- `calculate-discount` returns a map with original and discounted amounts.

### InvoiceController

**Base Path**: `/api`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/create-invoice` | POST | Any | `InvoiceRequest` | Creates a new invoice | `Response<InvoiceResponse>` | Generates a new invoice. |
| `/invoice-report/{id}/report` | GET | Any | Path: `id` | Generates PDF report for invoice | `ByteArrayResource` (PDF) or `ErrorResponse` | Returns invoice PDF or error. |
| `/invoice-reports` | GET | admin, user | None | Fetches invoices (all for admin, daily for user) | `Response<List<InvoiceResponse>>` | Lists invoices based on role. |
| `/invoice/{id}` | GET | Any | Path: `id` | Retrieves invoice by ID | `Response<InvoiceResponse>` | Fetches invoice details. |
| `/invoice/{id}/update` | PUT | Any | Path: `id`, Body: `InvoiceRequest` | Updates invoice | `Response<InvoiceResponse>` | Modifies invoice details. |
| `/invoices/{id}` | DELETE | Any | Path: `id` | Deletes invoice | `Response<InvoiceResponse>` | Removes an invoice. |
| `/total-invoices` | GET | Any | None | Counts total invoices | `Response<Long>` | Returns total invoice count. |
| `/total-discounts` | GET | Any | None | Counts invoices with discounts | `Response<Long>` | Returns count of discounted invoices. |

**Notes**:
- PDF generation uses `PDFGenerator` with Thymeleaf template (`invoice-template.html`).
- `invoice-report/{id}/report` returns a PDF file or JSON error response if the invoice is not found.

### MemberController

**Base Path**: `/api`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/add-member` | POST | Any | `MemberRequest` | Adds a new member | `Response<MemberResponse>` | Creates a new member. |
| `/all-members` | GET | Any | None | Retrieves all members | `Response<List<MemberResponse>>` | Lists all members. |
| `/member/{id}` | GET | Any | Path: `id` | Fetches member by ID | `Response<MemberResponse>` | Retrieves member details. |
| `/member/{id}/update` | PUT | Any | Path: `id`, Body: `MemberRequest` | Updates member | `Response<MemberResponse>` | Modifies member details. |
| `/member/{id}/delete` | DELETE | Any | Path: `id` | Deletes member | `Response<MemberResponse>` | Removes a member. |
| `/total-members` | GET | Any | None | Counts total members | `Response<Long>` | Returns total member count. |

### MixedController

**Base Path**: `/api/totals`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/` | GET | Any | None | Fetches counts of members, invoices, etc. | `Response<AllCountsResponse>` | Returns aggregated counts. |

**Notes**:
- Used for dashboard statistics, aggregating data across entities.

### PackageController

**Base Path**: `/api`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/package` | POST | Any | `PackageRequest` | Adds a new package | `Response<PackagesResponse>` | Creates a new package. |
| `/all-packages` | GET | Any | None | Retrieves all packages | `Response<List<PackagesResponse>>` | Lists all packages. |
| `/package/{id}` | GET | Any | Path: `id` | Fetches package by ID | `Response<PackagesResponse>` | Retrieves package details. |
| `/package/{id}/update` | PUT | Any | Path: `id`, Body: `PackageRequest` | Updates package | `Response<PackagesResponse>` | Modifies package details. |
| `/package/{id}/delete` | DELETE | Any | Path: `id` | Deletes package | `Response<PackagesResponse>` | Removes a package. |
| `/total-packages` | GET | Any | None | Counts total packages | `Response<Long>` | Returns total package count. |

### ReportController

**Base Path**: `/api`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/monthly-reports` | GET | Any | Query: `year` (optional) | Fetches monthly report data | `Response<MonthlyReportResponse>` | Returns monthly statistics. |
| `/download-report` | GET | Any | Query: `year` (optional) | Generates Excel report | `Response<Resource>` | Downloads Excel report. |
| `/export-members-invoices` | GET | Any | None | Exports member invoices to Excel | `Response<Resource>` | Downloads member invoices Excel. |

**Notes**:
- Uses `ExcelExporter` to generate Excel files.
- `MonthlyReportResponse` includes new members, paid members, and total amounts by month.

### SmsController

**Base Path**: `/api/sms`

| Endpoint | Method | Role | Input | Processing | Output | Description |
|----------|--------|------|-------|------------|--------|-------------|
| `/send/{memberId}/{templateName}` | POST | Any | Path: `memberId`, `templateName` (TemplateType), Body: `Map<String, String>` (placeholders) | Sends SMS to member using template | `Response<String>` | Sends SMS notification. |

**Notes**:
- Integrates with `SmsClient` to send SMS via Beem Africa API.
- Supports templates like `PAYMENT_CONFIRMATION`, `SUBSCRIPTION_REMINDER`, etc.
- Uses `CompletableFuture` for asynchronous SMS sending.

## Assumptions and Notes

1. **Commented-Out Kafka Code**:
   - `DataInjector.java` and `DebeziumListener.java` are commented out, indicating Kafka/Debezium integration is not active.

2. **File Upload Path**:
   - The default `file.upload-dir` is set to `C:/uploads`, which is Windows-specific. (Unless you want to run it in docker like I did)

3. **SMS API**:
   - Beem Africa API credentials must be provided in `application.properties`.

4. **Security**:
   - JWT authentication requires a valid token for most endpoints.
   - Admin role is required for sensitive operations (e.g. user management).
   - Table-model mismatch problem.

5. **Frontend**:
   - The application assumes a React frontend at `http://localhost:5173` (based on `cors.allowedOrigins`).

6. **Database**:
   - MySQL database `fitnessCenterDB` must exist before starting the application.
   - Hibernate’s `ddl-auto=update` automatically creates/updates schema.

7. **Logo Handling**:
   - Logos are stored in `/var/uploads/gym-services/logos` and converted to Base64 for PDF rendering.

8. **Cron Schedules**:
   - SMS tasks are scheduled (every 5 minutes for payment reminders).

This README provides a clean guide to understanding the Gym Services Management Backend System.
