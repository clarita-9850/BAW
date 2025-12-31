# 🚀 Timesheet Management System

## 📋 **System Overview**

This is a **production-ready, enterprise-grade** timesheet management system built with **microservices architecture** and **advanced data pipeline processing**. The system features **role-based field masking**, **real-time data processing**, **PDF/CSV report generation**, **email delivery**, **scheduled reports**, and **comprehensive security controls** for handling sensitive timesheet data.

### 🎯 **Key Features**
- **Event Logging Layer** with in-app EventService (no external broker required)
- **Advanced Data Pipeline** with field masking and security
- **Role-Based Access Control** with 6 user roles
- **Real-Time Report Generation** (PDF/CSV) for multiple target systems
- **Email Delivery System** with PDF attachments
- **Scheduled Reports** with cron job automation
- **Service Account Integration** with Keycloak JWT tokens
- **SFTP Delivery** with encrypted file transfer
- **Modern Frontend Interfaces** with responsive design
- **Comprehensive Security** with HIPAA/GDPR compliance
- **Production-Ready** with monitoring and health checks

---

## 🚀 **Quick Start Guide**

### **Prerequisites**
- Docker and Docker Compose
- Java 17+ (for local development)
- Maven 3.6+ (for local development)

### **1. Start Services**
```bash
# Start all services with Docker Compose
docker-compose up -d

# Check service status
docker-compose ps
```

### **2. Access Frontend Interfaces**
- **Field Masking**: http://localhost:8080/field-masking-interface.html
- **Role Comparison**: http://localhost:8080/role-comparison.html
- **Report Viewer**: http://localhost:8080/report-viewer.html

### **3. Keycloak Setup**
- **Admin Console**: http://localhost:8080 (Keycloak from sajeevs-codebase-main)
- **Realm**: `cmips`
- **Client**: `trial-app`
- **Configure County Mapper**: Run `./configure-keycloak-county-mapper.sh`
- **Create Users**: Run `./create-keycloak-cron-users.sh`

See [JWT_TOKEN_CONFIGURATION_SUMMARY.md](./JWT_TOKEN_CONFIGURATION_SUMMARY.md) for detailed setup instructions.

---

## 📚 **Documentation**

For detailed system documentation, please refer to:

- **[📐 ARCHITECTURE.md](./ARCHITECTURE.md)** - System architecture, components, and data flow
- **[🔌 API_DOCUMENTATION.md](./API_DOCUMENTATION.md)** - REST API endpoints and usage
- **[🚀 DEPLOYMENT.md](./DEPLOYMENT.md)** - Deployment guide for local and production environments
- **[🔐 JWT_TOKEN_CONFIGURATION_SUMMARY.md](./JWT_TOKEN_CONFIGURATION_SUMMARY.md)** - JWT token configuration and county-based access control
- **[✅ SETUP_CHECKLIST.md](./SETUP_CHECKLIST.md)** - Setup checklist and verification steps

---

## 🎉 **System Status**

**✅ PRODUCTION READY** - The system is fully functional with:
- Complete data pipeline processing
- Advanced field masking and security
- PDF/CSV report generation
- Email delivery system
- Scheduled report automation
- Service account integration
- SFTP file delivery
- Modern frontend interfaces
- Comprehensive monitoring and health checks
- Production-ready deployment configuration

**🚀 Ready for Enterprise Deployment!**

---

## 📞 **Support**

For questions, issues, or contributions, please refer to the comprehensive documentation or create an issue in the repository.

**Happy Coding! 🎯**
