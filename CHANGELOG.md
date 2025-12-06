# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - Unreleased
### Added
- Prometheus metrics endpoint
- Healthcheck endpoint

## [1.0.0] - Initial Service Release
### Added
- Initial release of Traversium Audit Service
- User activity audit logging
- Trip activity audit logging with Event Sourcing support
- Kafka integration for receiving audit events
- RESTful API for querying audit logs
- Event Sourcing and CQRS support for trip activities
- Trip state revert functionality (within 7-day window)
- Docker support with multi-platform builds (amd64, arm64)
- GitHub Actions CI/CD pipeline
- Multi-module Maven project structure (audit-models, audit-service)

### Security
- Multi-tenancy support with tenant context isolation

