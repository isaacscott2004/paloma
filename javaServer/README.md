# Paloma Mental Health API

A comprehensive REST API built with Spring Boot for mental health tracking and medication management.

## Features

- **User Management**: Complete CRUD operations for users
- **Daily Check-ins**: Track mood, energy, motivation, and suicidal ideation scores
- **Medication Management**: Track medications, dosages, and schedules
- **Medication Logs**: Record medication adherence
- **Trusted Contacts**: Manage emergency contacts
- **Alerts**: Handle emergency notifications
- **Score History**: Track mental health score trends over time
- **Role-based Access**: User roles and permissions

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Database**: MySQL 8.0
- **ORM**: JPA/Hibernate
- **Security**: Spring Security
- **Validation**: Jakarta Validation
- **Documentation**: Lombok
- **Build Tool**: Gradle

## Database Schema

The API follows the database schema shown in the provided diagrams with the following main entities:

### Core Entities
- `users` - User account information
- `roles` - User role definitions
- `user_roles` - Many-to-many relationship between users and roles
- `auth` - Authentication tokens
- `auth_credentials` - User credentials

### Mental Health Tracking
- `daily_checkins` - Daily mood and wellness scores
- `medications` - User medications
- `med_logs` - Medication adherence tracking
- `trusted_contacts` - Emergency contact information
- `alerts` - Emergency notifications
- `score_history` - Historical score tracking

## API Endpoints

### Health Check
- `GET /api/health` - Service health status
- `GET /api/info` - API information

### Users
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `GET /api/users/email/{email}` - Get user by email
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `PUT /api/users/{id}/login` - Update last login
- `GET /api/users/inactive?days={days}` - Get inactive users
- `GET /api/users/role/{roleName}` - Get users by role

### Daily Check-ins
- `GET /api/checkins/user/{userId}` - Get all check-ins for user
- `GET /api/checkins/user/{userId}/date/{date}` - Get check-in for specific date
- `POST /api/checkins/user/{userId}` - Create new check-in
- `PUT /api/checkins/{checkinId}` - Update check-in
- `DELETE /api/checkins/{checkinId}` - Delete check-in
- `GET /api/checkins/user/{userId}/range?startDate={date}&endDate={date}` - Get check-ins in date range
- `GET /api/checkins/user/{userId}/average?startDate={date}&endDate={date}` - Get average scores
- `GET /api/checkins/user/{userId}/today` - Get today's check-in
- `GET /api/checkins/user/{userId}/week` - Get week's check-ins
- `GET /api/checkins/user/{userId}/month` - Get month's check-ins

### Medications
- `GET /api/medications/user/{userId}` - Get all medications for user
- `GET /api/medications/user/{userId}/active` - Get active medications
- `GET /api/medications/{medicationId}` - Get medication by ID
- `POST /api/medications/user/{userId}` - Create new medication
- `PUT /api/medications/{medicationId}` - Update medication
- `DELETE /api/medications/{medicationId}` - Delete medication
- `PUT /api/medications/{medicationId}/deactivate` - Deactivate medication
- `PUT /api/medications/{medicationId}/activate` - Activate medication
- `GET /api/medications/user/{userId}/count` - Get active medication count

## Data Models

### User DTO
```json
{
  "id": "uuid",
  "username": "string",
  "email": "string",
  "fullName": "string",
  "createdAt": "datetime",
  "lastLogin": "datetime",
  "roles": ["string"]
}
```

### Daily Check-in DTO
```json
{
  "id": "uuid",
  "userId": "uuid",
  "date": "date",
  "moodScore": "integer (1-10)",
  "energyScore": "integer (1-10)",
  "motivationScore": "integer (1-10)",
  "suicidalScore": "integer (1-10)",
  "overallScore": "integer (1-10)",
  "notes": "string",
  "createdAt": "datetime"
}
```

### Medication DTO
```json
{
  "id": "uuid",
  "userId": "uuid",
  "name": "string",
  "dosage": "string",
  "schedule": "string",
  "isActive": "boolean",
  "createdAt": "datetime"
}
```

## Configuration

### Database Configuration
Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/paloma
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## Running the Application

1. **Prerequisites**:
   - Java 21+
   - MySQL 8.0+
   - Gradle

2. **Database Setup**:
   - Create MySQL database named `paloma`
   - Update database credentials in `application.properties`

3. **Run Application**:
   ```bash
   ./gradlew bootRun
   ```

4. **Access API**:
   - Base URL: `http://localhost:8080`
   - Health Check: `http://localhost:8080/api/health`
   - API Info: `http://localhost:8080/api/info`

## Error Handling

The API includes comprehensive error handling with:
- Global exception handler
- Validation error responses
- Proper HTTP status codes
- Structured error messages

## CORS Support

CORS is enabled for all origins on API endpoints (`/api/**`) to support frontend applications.

## Security

The API includes Spring Security dependencies and is configured for:
- Authentication token management
- Role-based access control
- Password hashing and validation

## Future Enhancements

- JWT-based authentication
- Real-time notifications
- Data analytics and reporting
- Mobile app integration
- Secure API key management