# 🛡️ Pre-Mortem Engine
### Identify Risks Before They Become Failures

## 📌 Project Overview

Pre-Mortem Engine is a web-based project risk assessment application designed to help users identify potential project failures before they occur.

The application analyzes available project information and supported files to identify possible risks, evaluate their severity, and provide preventive recommendations. It also offers project history and What-If Simulation to help users examine alternative scenarios and make informed decisions.

The primary objective is to encourage proactive planning by identifying weaknesses early rather than responding to problems after they arise.

## 🎯 Problem Statement

Projects can encounter unexpected challenges because of incomplete planning, technical limitations, missing requirements, integration issues, or overlooked dependencies.

Traditional project planning often focuses on achieving the expected outcome, while potential failure scenarios may not receive enough attention.

Identifying these risks late can lead to delays, additional costs, rework, and unsuccessful project outcomes.

A structured approach is therefore needed to examine what could go wrong, assess potential risks, and prepare preventive measures before implementation.

## 💡 Proposed Solution

Pre-Mortem Engine provides a centralized platform for project risk assessment and preventive planning.

Users can create projects, provide relevant information, and submit supported project files for analysis. The application processes the available inputs using its implemented analysis logic and presents risk assessments, scores, and preventive recommendations.

The What-If Simulation feature supports the exploration of alternative scenarios, while Project History helps users revisit saved project information and previous assessments.

By combining these capabilities, the application supports a more systematic and proactive approach to project planning.

## ✨ Key Features

### 1. Project Management
Create and manage project records by providing details such as project name, type, and description, according to the implemented form.

### 2. Project File Analysis
Analyze supported project files to identify potential issues within the scope of the application's implemented checks.

### 3. Risk Assessment
Evaluate potential project risks and present a risk score that helps users understand which areas may require further attention.

### 4. Preventive Recommendations
Provide suggestions and preventive measures to help users address identified weaknesses before they develop into larger problems.

### 5. What-If Simulation
Explore alternative scenarios and compare assessment results when supported conditions or parameters are changed.

### 6. Project History
Revisit saved project details and previous assessments to support continuity and review, depending on the implemented persistence functionality.

### 7. Web-Based Interface
Provide a user-friendly interface for accessing project information, reviewing risk assessments, and interacting with the application's available features.

## 🔄 Application Workflow

The application follows a structured workflow:

**Project Creation → Information Collection → Risk Analysis → Risk Assessment → What-If Simulation → Preventive Planning**

1. **Project Creation:** Enter the required project details.
2. **Information Collection:** Provide project information and supported files.
3. **Risk Analysis:** Process the available information using the application's analysis logic.
4. **Risk Assessment:** Review the generated risk score and identified concerns.
5. **Scenario Simulation:** Explore supported alternative scenarios and examine their effects on assessment results.
6. **Preventive Planning:** Review recommendations and determine suitable actions to reduce potential risks.

## 🧪 What-If Simulation

What-If Simulation helps users examine how alternative decisions or conditions may influence a project's risk assessment.

For example, a user may want to explore whether additional testing, a change in implementation strategy, or a different dependency could affect the identified risks.

This feature can support:

- Comparing alternative project scenarios.
- Exploring the possible effects of preventive measures.
- Understanding how selected parameters influence assessment results.
- Supporting project planning and decision-making.
- Identifying scenarios that require additional investigation.

Simulation results depend on the application's implemented logic and assumptions. They are intended to support decision-making rather than guarantee actual project outcomes.

## 📚 Project History

Project History supports the review of previously saved project information and assessments.

It helps users revisit earlier results, compare information where supported, and maintain continuity during project planning.

The records available in the history depend on the application's database structure and implemented persistence functionality.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core programming language |
| Spring Boot | Server-side application framework |
| Maven | Dependency management and build automation |
| MySQL | Relational database |
| HTML | Web page structure |
| CSS | Interface styling |
| JDBC | Database Connectivity |

The final technology list should reflect the libraries and frameworks actually used in the application.

## 🏗️ System Architecture

Pre-Mortem Engine follows a web application architecture that separates the user interface, application logic, and data persistence.

**High-level architecture:**

User Interface  
↓  
Spring Boot Controllers  
↓  
Application Services  
↓  
Risk Analysis and Scoring Logic  
↓  
Data Access Layer  
↓  
MySQL Database

### Main Components

**Presentation Layer:** Displays project information, forms, assessment results, history, and simulation interfaces.

**Controller Layer:** Receives web requests and coordinates the required application operations.

**Service Layer:** Implements project-related operations, analysis logic, risk scoring, and other application functionality.

**Data Access Layer:** Handles database operations for storing and retrieving application records.

**Database Layer:** Maintains persistent information according to the configured schema.

The exact component responsibilities depend on the current implementation.

## 🗄️ Database Management

MySQL provides relational data storage for the application.

Depending on the implemented schema, the database may store:

- Project information.
- Risk assessment results.
- Related risk details.
- Project history and other supported records.

The database schema and relationships determine how this information is stored and retrieved.

Database credentials should be managed securely and should not be embedded in publicly shared documentation or source code.

## 🧪 Testing and Validation

Automated tests can be used to validate important application components, including project analysis and risk-scoring services.

Testing helps verify expected behavior, identify defects, and reduce the likelihood of regressions when changes are introduced.

Application testing should cover normal inputs, invalid inputs, boundary conditions, database operations, and important user workflows.

The effectiveness of testing depends on the test cases implemented and the scenarios covered.

## 🔐 Security and Limitations

- Risk scores and recommendations are intended to support decisions, not replace professional judgment.
- Analysis results depend on the quality and completeness of the supplied information.
- File analysis is limited to supported formats and implemented checks.
- Simulation results depend on the parameters and behavior supported by the application.
- Database credentials and other secrets must be protected.
- Authentication, authorization, file-upload validation, and access controls should be reviewed before production deployment.

## 🔮 Future Enhancements

Potential areas for further development include:

- Expanded project file analysis.
- More comprehensive risk identification.
- Improved explanations for generated risk scores.
- Additional What-If Simulation scenarios.
- Enhanced assessment comparison and history.
- Detailed report generation and export.
- Stronger authentication and role-based access control.
- Expanded automated testing and deployment support.

These enhancements represent possible future development and are not necessarily part of the current implementation.

## 📝 Conclusion

Pre-Mortem Engine encourages proactive project planning by helping users identify potential failures, examine risks, and consider preventive actions before implementation.

Through project management, risk assessment, What-If Simulation, and project history, the application provides a structured approach to understanding uncertainty and supporting informed decisions.

**Identify risks early. Plan prevention. Build with confidence.**
