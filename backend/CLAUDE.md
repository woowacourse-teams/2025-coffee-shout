# Coffee Shout - Spring Boot Backend

## Project Overview

**Coffee Shout** is a real-time multiplayer web application that allows users to create rooms, play mini-games, and use a roulette system to determine who pays for coffee. The application features WebSocket-based real-time communication, Redis for data persistence, and AWS S3 integration for QR code generation.

### Key Features
- **Room-based multiplayer system** with host/guest roles
- **Real-time communication** via WebSockets
- **Mini-games** with scoring and roulette probability adjustment
- **QR code generation** for easy room joining
- **Roulette system** with dynamic probability calculation
- **Menu selection** with category-based organization
- **AWS S3 integration** for file storage
- **Redis** for session and data management
- **Prometheus metrics** for monitoring

## Technology Stack

### Core Framework
- **Spring Boot 3.5.3** - Main application framework
- **Java 21** - Programming language
- **Gradle 8.14.2** - Build tool

### Key Dependencies
- **Spring Web** - REST API endpoints
- **Spring WebSocket** - Real-time communication
- **Spring Data Redis** - Redis integration
- **Spring Boot Actuator** - Application monitoring
- **Spring AOP** - Aspect-oriented programming
- **Spring Validation** - Request validation

### External Integrations
- **AWS S3** (spring-cloud-aws-starter-s3) - File storage for QR codes
- **ZXing** (Google) - QR code generation
- **Prometheus** (Micrometer) - Metrics collection
- **WebSocket Docs Generator** (custom library) - API documentation

### Testing
- **Spring Boot Test** - Integration testing
- **Testcontainers** - Container-based testing
- **JUnit 5** - Unit testing framework

### Infrastructure
- **Redis/Valkey** - In-memory data store
- **Docker Compose** - Local development environment

## Project Structure

```
coffee-shout/
├── src/main/java/coffeeshout/
│   ├── CoffeeShoutApplication.java          # Main application class
│   ├── global/                              # Cross-cutting concerns
│   │   ├── config/                         # Configuration classes
│   │   ├── websocket/                      # WebSocket configuration
│   │   ├── converter/                      # Data converters
│   │   ├── exception/                      # Global exception handling
│   │   ├── filter/                         # HTTP filters
│   │   ├── interceptor/                    # Request interceptors
│   │   ├── log/                           # Logging utilities
│   │   ├── metric/                        # Custom metrics
│   │   └── ui/                            # Global controllers
│   ├── room/                               # Room domain
│   │   ├── domain/                        # Room business logic
│   │   ├── application/                   # Room services
│   │   ├── ui/                           # Room controllers
│   │   └── infra/                        # Room infrastructure
│   └── minigame/                          # Mini-game domain
│       ├── domain/                        # Game business logic
│       ├── application/                   # Game services
│       ├── ui/                           # Game controllers
│       └── common/                       # Shared game utilities
├── src/main/resources/
│   ├── application.yml                    # Main configuration
│   ├── application-dev.yml               # Development config
│   ├── application-prod.yml              # Production config
│   ├── application-test.yml              # Test config
│   └── data/menu-data.yml               # Menu seed data
├── lab/                                  # Experimental module
│   ├── src/main/java/coffeeshout/
│   │   ├── domain/                       # Lab domain models
│   │   ├── repository/                   # Lab repositories
│   │   └── config/                       # Lab configuration
│   └── build.gradle.kts                  # Lab module build
├── docs/                                 # Documentation
│   ├── 도메인모델.md                      # Domain model docs
│   └── 카드게임도메인모델.md                # Card game domain docs
├── scripts/                              # Deployment scripts
├── build.gradle.kts                      # Main build configuration
├── settings.gradle.kts                   # Gradle settings
├── docker-compose.yml                    # Local Redis setup
├── appspec.yml                          # AWS CodeDeploy config
├── buildspec-dev.yml                    # AWS CodeBuild (dev)
└── buildspec-prod.yml                   # AWS CodeBuild (prod)
```

## Architecture

### Domain-Driven Design
The application follows DDD principles with clear domain boundaries:

1. **Room Domain**: Manages room lifecycle, player management, and game coordination
2. **Mini-game Domain**: Handles game logic, scoring, and result processing
3. **Global**: Cross-cutting concerns like configuration, exception handling, and WebSocket setup

### Layered Architecture
- **UI Layer**: Controllers (REST + WebSocket)
- **Application Layer**: Services and use cases
- **Domain Layer**: Business logic and entities
- **Infrastructure Layer**: External integrations (Redis, S3, etc.)

### Real-time Communication
- WebSocket endpoints for real-time updates
- Message broadcasting for room events
- Event-driven architecture for game state changes

## Configuration Profiles

### Development (`application-dev.yml`)
- Local/dev environment URLs
- WebSocket docs enabled
- Development-specific settings

### Production (`application-prod.yml`)
- Production URLs
- WebSocket docs disabled
- Optimized for production use

### Test (`application-test.yml`)
- Test-specific configurations
- In-memory or test containers

## How to Run

### Prerequisites
- Java 21
- Docker and Docker Compose
- AWS credentials (for S3 integration)

### Local Development

1. **Start Redis/Valkey**:
   ```bash
   docker-compose up -d
   ```

2. **Set Environment Variables**:
   ```bash
   export REDIS_HOST=localhost
   export S3_BUCKET_NAME=your-bucket-name
   export S3_QR_KEY_PREFIX=qr-codes/
   ```

3. **Run the Application**:
   ```bash
   ./gradlew bootRun
   ```

4. **Run with Profile**:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "RoomTest"

# Run integration tests
./gradlew integrationTest
```

### Building

```bash
# Build the project
./gradlew build

# Build without tests
./gradlew build -x test

# Create JAR
./gradlew bootJar
```

## Key Commands

### Development
```bash
# Clean and build
./gradlew clean build

# Run with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Generate dependency report
./gradlew dependencies

# Check for updates
./gradlew dependencyUpdates
```

### Testing
```bash
# Unit tests only
./gradlew test

# Integration tests
./gradlew integrationTest

# Test with coverage
./gradlew jacocoTestReport

# Continuous testing
./gradlew test --continuous
```

### Docker
```bash
# Start Redis
docker-compose up -d valkey

# Stop Redis
docker-compose down

# View Redis logs
docker-compose logs valkey
```

## Module Descriptions

### Main Module (`/`)
The primary Spring Boot application containing:
- Core business domains (Room, Mini-game)
- REST and WebSocket APIs
- Global configurations and utilities
- Production-ready features (metrics, health checks)

### Lab Module (`/lab`)
Experimental module for:
- Testing new features
- Redis migration experiments
- Proof of concepts
- Independent development and testing

## API Endpoints

### REST Endpoints
- `GET /api/rooms/{code}` - Get room information
- `POST /api/rooms` - Create new room
- `POST /api/rooms/{code}/join` - Join room
- `GET /api/menu-categories` - Get menu categories
- `GET /api/minigames` - Get available mini-games

### WebSocket Endpoints
- `/ws` - WebSocket connection endpoint
- `/app/rooms/{code}/join` - Join room via WebSocket
- `/app/rooms/{code}/start-game` - Start mini-game
- `/app/rooms/{code}/spin-roulette` - Spin roulette
- `/topic/rooms/{code}` - Room updates subscription

## Monitoring & Health Checks

### Actuator Endpoints
- `/actuator/health` - Application health
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Custom Metrics
- Room creation/join rates
- Game completion statistics
- WebSocket connection metrics
- Redis operation metrics

## Development Workflow

### Branch Strategy
- `main` - Production branch
- `be/dev` - Development branch
- `be/feat/xxx` - Feature branches

### Code Style
- Follow Java conventions
- Use Lombok for boilerplate reduction
- Implement proper error handling
- Write comprehensive tests

### Testing Strategy
- Unit tests for domain logic
- Integration tests for APIs
- Testcontainers for external dependencies
- WebSocket integration testing

## Deployment

### AWS CodeBuild
- Separate build specs for dev/prod environments
- Automated testing and deployment
- Docker image creation

### AWS CodeDeploy
- Blue-green deployment strategy
- Health checks and rollback capabilities
- Environment-specific configurations

## Environment Variables

### Required
- `REDIS_HOST` - Redis server hostname
- `S3_BUCKET_NAME` - AWS S3 bucket name
- `S3_QR_KEY_PREFIX` - S3 key prefix for QR codes

### Optional
- `SPRING_PROFILES_ACTIVE` - Active Spring profiles
- `REDIS_PORT` - Redis port (default: 6379)
- `REDIS_SSL_ENABLED` - Enable Redis SSL (default: true)

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   - Ensure Redis is running: `docker-compose ps`
   - Check connection settings in `application.yml`
   - Verify network connectivity

2. **S3 Integration Issues**
   - Verify AWS credentials
   - Check bucket permissions
   - Ensure correct region configuration

3. **WebSocket Connection Problems**
   - Check CORS configuration
   - Verify WebSocket endpoint URLs
   - Review browser console for errors

4. **Build Failures**
   - Clean Gradle cache: `./gradlew clean`
   - Check Java version compatibility
   - Verify all dependencies are available

### Log Locations
- Application logs: Console output
- Redis logs: `docker-compose logs valkey`
- Test logs: `build/reports/tests/`

For additional help, refer to the domain documentation in the `/docs` folder or check the WebSocket API documentation at `/websocket-docs` (dev environment only).