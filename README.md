## About the Project
This project is a comprehensive full-stack application developed using Java JDK 21, Hibernate, Java Swing, Spring Framework, and PostgreSQL. It covers all development processes from terminal application to desktop GUI and web application. Hibernate ORM and PostgreSQL are used for database management.

## Project Contents
- **gui_tests:** Swing-based desktop GUI application
- **SpringWeb:** Spring Boot-based web application

## Installation Requirements
1. **Java JDK 21** - Required to compile and run the project
2. **PostgreSQL** - Database server (Latest version)
3. **Eclipse IDE** - Java development environment (or your preferred IDE)
4. **Maven** - Dependency management for gui_testleri
5. **Gradle** - Dependency management for SpringWeb

## Installation Steps

### Database Setup
1. Install and run PostgreSQL
2. Run the create_database.sql file to create the database

### GUI Application (gui_tests)
1. Open the project in your IDE
2. Make sure the JAR files are in the root directory of the project
   - These files should come with the project. You can download missing files with Maven: mvn dependency:resolve
3. Run the Main class

### Web Application (SpringWeb)
1. Download Gradle dependencies: gradle build
2. Run the application:
   - Web mode: gradle runWeb or start_web_service.bat
   - GUI mode: gradle runGUI
   - CLI mode: gradle runCLI or start_cli_service.bat

## Important Note
When compiling the project after cloning it to your computer, your IDE may recreate the .classpath file. In this case, allow the IDE to use its own plugin paths - this is normal.

## Features
- Multi-layered architecture (presentation, service, database)
- Desktop GUI application (Java Swing + FlatLAF)
- Web interface (Spring MVC + Thymeleaf)
- CLI mode (Spring Shell)
- Database management with ORM (Hibernate)
- Different running modes and environments

## License
Open source (add license information)

## Live Preview
Below is the demonstration video of the project:

[![Project Demo](https://img.youtube.com/vi/vdBjLsf7te4/0.jpg)](https://www.youtube.com/watch?v=vdBjLsf7te4)
