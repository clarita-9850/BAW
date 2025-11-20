#!/bin/bash

# CMIPS POC Setup Script
# This script sets up the CMIPS POC application with all dependencies

set -e

echo "🚀 Setting up CMIPS POC Application..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_status "Checking requirements..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.6 or higher."
        exit 1
    fi
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js 18 or higher."
        exit 1
    fi
    
    # Check npm
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed. Please install npm."
        exit 1
    fi
    
    # Check PostgreSQL
    if ! command -v psql &> /dev/null; then
        print_error "PostgreSQL is not installed. Please install PostgreSQL 13 or higher."
        exit 1
    fi
    
    print_success "All requirements are met!"
}

# Setup PostgreSQL database
setup_database() {
    print_status "Setting up PostgreSQL database..."
    
    # Check if PostgreSQL is running
    if ! pg_isready -q; then
        print_error "PostgreSQL is not running. Please start PostgreSQL service."
        exit 1
    fi
    
    # Create database
    if psql -lqt | cut -d \| -f 1 | grep -qw cmips_poc; then
        print_warning "Database 'cmips_poc' already exists."
    else
        createdb cmips_poc
        print_success "Database 'cmips_poc' created successfully."
    fi
    
    # Create user if it doesn't exist
    if psql -t -c "SELECT 1 FROM pg_roles WHERE rolname='cmips_user';" | grep -q 1; then
        print_warning "User 'cmips_user' already exists."
    else
        psql -c "CREATE USER cmips_user WITH PASSWORD 'cmips_password';"
        psql -c "GRANT ALL PRIVILEGES ON DATABASE cmips_poc TO cmips_user;"
        print_success "User 'cmips_user' created successfully."
    fi
}

# Setup backend
setup_backend() {
    print_status "Setting up backend..."
    
    cd backend
    
    # Install dependencies
    print_status "Installing Maven dependencies..."
    mvn clean install -DskipTests
    
    print_success "Backend setup completed!"
    cd ..
}

# Setup frontend
setup_frontend() {
    print_status "Setting up frontend..."
    
    cd frontend
    
    # Install dependencies
    print_status "Installing npm dependencies..."
    npm install
    
    print_success "Frontend setup completed!"
    cd ..
}

# Create environment files
create_env_files() {
    print_status "Creating environment files..."
    
    # Backend environment
    cat > backend/.env << EOF
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cmips_poc
SPRING_DATASOURCE_USERNAME=cmips_user
SPRING_DATASOURCE_PASSWORD=cmips_password
JWT_SECRET=cmips-poc-secret-key-for-jwt-token-generation-and-validation
JWT_EXPIRATION=86400000
EOF
    
    # Frontend environment
    cat > frontend/.env << EOF
VITE_API_BASE_URL=http://localhost:8080/api
EOF
    
    print_success "Environment files created!"
}

# Main setup function
main() {
    echo "=========================================="
    echo "    CMIPS POC Setup Script"
    echo "=========================================="
    echo ""
    
    check_requirements
    setup_database
    setup_backend
    setup_frontend
    create_env_files
    
    echo ""
    echo "=========================================="
    print_success "Setup completed successfully!"
    echo "=========================================="
    echo ""
    echo "To start the application:"
    echo ""
    echo "1. Start the backend:"
    echo "   cd backend && mvn spring-boot:run"
    echo ""
    echo "2. Start the frontend (in a new terminal):"
    echo "   cd frontend && npm run dev"
    echo ""
    echo "3. Open your browser and go to:"
    echo "   http://localhost:5173"
    echo ""
    echo "Demo accounts:"
    echo "  - admin / admin123 (Full access)"
    echo "  - caseworker / case123 (Timesheet management)"
    echo "  - auditor / audit123 (View only)"
    echo "  - manager / manager123 (User and timesheet management)"
    echo "  - supervisor / super123 (Timesheet management)"
    echo ""
    echo "Happy coding! 🚀"
}

# Run main function
main




