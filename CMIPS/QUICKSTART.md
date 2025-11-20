# 🚀 CMIPS POC - Quick Start Guide

Get up and running with the CMIPS POC application in under 5 minutes!

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 13+
- Maven 3.6+

## 🏃‍♂️ Quick Setup

### Option 1: Automated Setup (Recommended)

```bash
# Run the setup script
./setup.sh
```

### Option 2: Manual Setup

1. **Database Setup**
```bash
# Create database
createdb cmips_poc

# Create user
psql -c "CREATE USER cmips_user WITH PASSWORD 'cmips_password';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE cmips_poc TO cmips_user;"
```

2. **Backend Setup**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

3. **Frontend Setup** (in a new terminal)
```bash
cd frontend
npm install
npm run dev
```

## 🌐 Access the Application

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api

## 🔐 Demo Accounts

| Username | Password | Role | Access Level |
|----------|----------|------|--------------|
| `admin` | `admin123` | Admin | Full access to all features |
| `caseworker` | `case123` | Case Worker | Can manage timesheets |
| `auditor` | `audit123` | Auditor | Can view timesheets only |
| `manager` | `manager123` | Manager | Can manage users and timesheets |
| `supervisor` | `super123` | Supervisor | Can manage timesheets |

## 🎯 Quick Demo Scenarios

### 1. Basic Timesheet Management
1. Login as `caseworker` / `case123`
2. Click "Add Timesheet" to create a new timesheet
3. View your timesheets in the list
4. Edit or delete timesheets as needed

### 2. Policy-Driven Access Control
1. Login as `admin` / `admin123`
2. Go to "Policy Management"
3. Create a new policy:
   - Role: `CASE_WORKER`
   - Resource: `timesheets`
   - Action: `DELETE`
   - Allowed: `false`
4. Login as `caseworker` / `case123`
5. Try to delete a timesheet (should be denied)

### 3. Role-Based Access
1. Login as `auditor` / `audit123`
2. Try to create a timesheet (should be denied)
3. View timesheets (should work)
4. Login as `admin` / `admin123`
5. Go to "User Management"
6. Change `auditor` role to `CASE_WORKER`
7. Login as `auditor` / `audit123`
8. Now you can create timesheets

## 🔧 Troubleshooting

### Backend Issues
- **Port 8080 already in use**: Change port in `application.yml`
- **Database connection failed**: Check PostgreSQL is running
- **JWT errors**: Check JWT secret in `application.yml`

### Frontend Issues
- **Port 5173 already in use**: Change port in `vite.config.ts`
- **API connection failed**: Check backend is running on port 8080
- **Build errors**: Run `npm install` again

### Database Issues
- **Database not found**: Run `createdb cmips_poc`
- **User not found**: Run the user creation commands
- **Permission denied**: Check user privileges

## 📚 Next Steps

1. **Explore the Code**: Check out the backend and frontend code
2. **Modify Policies**: Try creating different access policies
3. **Add Features**: Extend the application with new functionality
4. **Deploy**: Use Docker Compose for production deployment

## 🆘 Need Help?

- Check the full [README.md](README.md) for detailed documentation
- Review the API endpoints in the backend code
- Check the frontend components for UI examples
- Look at the database schema for data structure

---

**Happy coding! 🚀**




