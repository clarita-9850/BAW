#!/usr/bin/env python3
"""
Locust Load Testing Script for CMIPS Application
Install: pip install locust
Run: locust -f locust-load-test.py --host=http://localhost:8081
Web UI: http://localhost:8089
"""

from locust import HttpUser, task, between
import json
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class CMIPSUser(HttpUser):
    """
    Simulates a user interacting with CMIPS application
    """
    # Wait time between tasks (1-3 seconds)
    wait_time = between(1, 3)
    
    # Configuration
    keycloak_url = "http://localhost:8080"
    username = "provider1"
    password = "password123"
    client_id = "cmips-frontend-new"
    access_token = None
    
    def on_start(self):
        """
        Called when a simulated user starts
        Get authentication token from Keycloak
        """
        logger.info(f"User {self.username} logging in...")
        self.get_auth_token()
    
    def get_auth_token(self):
        """Get JWT token from Keycloak"""
        try:
            response = self.client.post(
                f"{self.keycloak_url}/realms/cmips/protocol/openid-connect/token",
                data={
                    "username": self.username,
                    "password": self.password,
                    "grant_type": "password",
                    "client_id": self.client_id
                },
                headers={"Content-Type": "application/x-www-form-urlencoded"},
                name="1. Keycloak - Get Token"
            )
            
            if response.status_code == 200:
                self.access_token = response.json()["access_token"]
                logger.info(f"✓ Token obtained for {self.username}")
            else:
                logger.error(f"✗ Failed to get token: {response.status_code}")
                logger.error(f"Response: {response.text}")
        except Exception as e:
            logger.error(f"✗ Exception getting token: {e}")
    
    def get_headers(self):
        """Get headers with authorization token"""
        return {
            "Authorization": f"Bearer {self.access_token}",
            "Content-Type": "application/json"
        }
    
    @task(5)  # Weight: 5 (executed more frequently)
    def get_timesheets(self):
        """GET /api/timesheets - Fetch all timesheets"""
        if not self.access_token:
            self.get_auth_token()
            return
        
        with self.client.get(
            "/api/timesheets",
            headers=self.get_headers(),
            catch_response=True,
            name="2. GET Timesheets"
        ) as response:
            if response.status_code == 200:
                response.success()
                logger.debug(f"✓ Fetched timesheets: {len(response.json())} records")
            elif response.status_code == 401:
                logger.warning("Token expired, refreshing...")
                self.get_auth_token()
                response.failure("Token expired")
            else:
                response.failure(f"Failed with status {response.status_code}")
    
    @task(2)  # Weight: 2
    def create_timesheet(self):
        """POST /api/timesheets - Create a new timesheet"""
        if not self.access_token:
            self.get_auth_token()
            return
        
        timesheet_data = {
            "employeeId": self.username,
            "employeeName": "Load Test User",
            "payPeriodStart": "2025-10-01",
            "payPeriodEnd": "2025-10-15",
            "regularHours": 40,
            "overtimeHours": 5,
            "department": "IT",
            "location": "Remote",
            "comments": f"Load test by user {self.username}"
        }
        
        with self.client.post(
            "/api/timesheets",
            json=timesheet_data,
            headers=self.get_headers(),
            catch_response=True,
            name="3. POST Create Timesheet"
        ) as response:
            if response.status_code in [200, 201]:
                response.success()
                logger.debug(f"✓ Created timesheet")
            elif response.status_code == 400:
                # Business validation error (e.g., duplicate)
                logger.debug(f"Business validation error: {response.text}")
                response.success()  # Don't count as failure
            elif response.status_code == 401:
                logger.warning("Token expired, refreshing...")
                self.get_auth_token()
                response.failure("Token expired")
            else:
                response.failure(f"Failed with status {response.status_code}: {response.text}")
    
    @task(3)  # Weight: 3
    def get_my_timesheets(self):
        """GET /api/timesheets/my - Fetch user's own timesheets"""
        if not self.access_token:
            self.get_auth_token()
            return
        
        with self.client.get(
            "/api/timesheets/my",
            headers=self.get_headers(),
            catch_response=True,
            name="4. GET My Timesheets"
        ) as response:
            if response.status_code == 200:
                response.success()
                logger.debug(f"✓ Fetched my timesheets")
            elif response.status_code == 401:
                logger.warning("Token expired, refreshing...")
                self.get_auth_token()
                response.failure("Token expired")
            else:
                response.failure(f"Failed with status {response.status_code}")
    
    @task(1)  # Weight: 1 (executed less frequently)
    def get_actions(self):
        """GET /api/timesheets/actions - Get allowed actions"""
        if not self.access_token:
            self.get_auth_token()
            return
        
        with self.client.get(
            "/api/timesheets/actions",
            headers=self.get_headers(),
            catch_response=True,
            name="5. GET Allowed Actions"
        ) as response:
            if response.status_code == 200:
                response.success()
                logger.debug(f"✓ Fetched allowed actions")
            elif response.status_code == 401:
                logger.warning("Token expired, refreshing...")
                self.get_auth_token()
                response.failure("Token expired")
            else:
                response.failure(f"Failed with status {response.status_code}")


# Custom load shapes for different test scenarios
from locust import LoadTestShape

class StepLoadShape(LoadTestShape):
    """
    A step load shape that increases users gradually
    Useful for finding performance breaking points
    
    Step 1: 0-60s:  100 users
    Step 2: 60-120s: 300 users
    Step 3: 120-180s: 500 users
    Step 4: 180-240s: 1000 users
    Step 5: 240-300s: 2000 users
    """
    
    step_time = 60  # Seconds per step
    step_load = 100  # Initial users
    spawn_rate = 10  # Users to spawn per second
    time_limit = 300  # Total test duration
    
    def tick(self):
        run_time = self.get_run_time()
        
        if run_time > self.time_limit:
            return None
        
        current_step = run_time // self.step_time
        user_count = self.step_load * (2 ** current_step) if current_step < 5 else 2000
        
        return (user_count, self.spawn_rate)


class SpikeLoadShape(LoadTestShape):
    """
    A spike load shape that simulates sudden traffic spikes
    
    0-30s:   100 users
    30-60s:  1000 users (SPIKE!)
    60-90s:  100 users
    90-120s: 2000 users (BIGGER SPIKE!)
    120-150s: 100 users
    """
    
    def tick(self):
        run_time = self.get_run_time()
        
        if run_time < 30:
            return (100, 10)
        elif run_time < 60:
            return (1000, 50)  # Spike!
        elif run_time < 90:
            return (100, 10)
        elif run_time < 120:
            return (2000, 100)  # Bigger spike!
        elif run_time < 150:
            return (100, 10)
        else:
            return None


# Usage examples:
"""
# Basic load test (manual control via web UI):
locust -f locust-load-test.py --host=http://localhost:8081

# Headless mode with specific parameters:
locust -f locust-load-test.py --host=http://localhost:8081 --headless -u 1000 -r 50 -t 5m

# With step load shape:
locust -f locust-load-test.py --host=http://localhost:8081 --headless --shape StepLoadShape

# With spike load shape:
locust -f locust-load-test.py --host=http://localhost:8081 --headless --shape SpikeLoadShape

# Generate HTML report:
locust -f locust-load-test.py --host=http://localhost:8081 --headless -u 1000 -r 50 -t 5m --html report.html

Parameters:
  -u, --users: Number of concurrent users
  -r, --spawn-rate: Rate to spawn users (users per second)
  -t, --run-time: Test duration (e.g., 300s, 5m, 1h)
  --headless: Run without web UI
  --html: Generate HTML report
"""


