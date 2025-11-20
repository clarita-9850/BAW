# 🎯 Load Testing Setup Complete!

## ✅ What Has Been Created

I've set up a complete load testing suite for your CMIPS application with **4 different tools**:

---

## 📁 **Files Created**

```
/Users/sajeev/Documents/cmips-security/load-testing/
├── simple-load-test.sh           ← Bash script (easiest, no dependencies)
├── locust-load-test.py            ← Python/Locust (best UI)
├── jmeter-test-plan.jmx           ← JMeter GUI test plan
├── README.md                      ← Complete documentation
├── QUICKSTART.md                  ← 5-minute quick start guide
└── SUMMARY.md                     ← This file
```

---

## 🚀 **Quick Start (30 seconds)**

```bash
# Navigate to load testing directory
cd /Users/sajeev/Documents/cmips-security/load-testing

# Run the easiest test
./simple-load-test.sh

# Choose option 1: Quick Test (100 requests)
```

**That's it!** You'll see results in seconds.

---

## 🛠️ **4 Load Testing Tools**

### **1. Bash Script** ⭐ Recommended for Quick Tests
- **File**: `simple-load-test.sh`
- **Pros**: No dependencies, instant results, easy to use
- **Best for**: Quick performance checks
- **Run**: `./simple-load-test.sh`

### **2. Locust** ⭐ Recommended for Visual Analysis
- **File**: `locust-load-test.py`
- **Pros**: Beautiful web UI, real-time graphs, easy to scale
- **Best for**: Detailed analysis, team presentations
- **Run**: `locust -f locust-load-test.py --host=http://localhost:8081`
- **UI**: http://localhost:8089

### **3. Apache JMeter** ⭐ Enterprise Standard
- **File**: `jmeter-test-plan.jmx`
- **Pros**: Industry standard, extensive features, detailed reports
- **Best for**: Enterprise testing, compliance
- **Run**: `jmeter -t jmeter-test-plan.jmx`

### **4. K6** ⭐ Modern CLI
- **File**: Create `k6-load-test.js` (instructions in README.md)
- **Pros**: Modern, fast, cloud integration
- **Best for**: CI/CD pipelines, automated testing
- **Run**: `k6 run k6-load-test.js`

---

## 📊 **Test Scenarios Available**

| Scenario | Requests | Description | Command |
|----------|----------|-------------|---------|
| **Quick Test** | 100 | Baseline performance | `./simple-load-test.sh` → Option 1 |
| **Medium Test** | 1,000 | Normal load | `./simple-load-test.sh` → Option 2 |
| **Heavy Test** | 5,000 | High load | `./simple-load-test.sh` → Option 3 |
| **Progressive** | 10-2,000 | Find breaking point | `./simple-load-test.sh` → Option 4 |
| **Mixed Workload** | Custom | 80% read, 20% write | `./simple-load-test.sh` → Option 5 |
| **Spike Test** | Variable | Sudden traffic spikes | Locust with SpikeLoadShape |

---

## 🎯 **What Each Tool Tests**

All tools test the same endpoints:

1. **Authentication** (Keycloak Token)
   - POST `/realms/cmips/protocol/openid-connect/token`
   
2. **Read Operations** (GET Timesheets)
   - GET `/api/timesheets`
   - Tests field-level authorization
   
3. **Write Operations** (Create Timesheet)
   - POST `/api/timesheets`
   - Tests permission enforcement

---

## 📈 **Performance Metrics Tracked**

### **Response Time**
- Average, Min, Max, 95th percentile
- **Target**: < 500ms average

### **Throughput**
- Requests per second
- **Target**: > 50 req/s

### **Success Rate**
- Percentage of successful requests
- **Target**: > 99%

### **Error Rate**
- Percentage of failed requests
- **Target**: < 1%

### **Concurrent Users**
- Maximum users system can handle
- **Target**: At least 100 concurrent users

---

## 💡 **Usage Examples**

### Example 1: Can the system handle 100 users?
```bash
./simple-load-test.sh
# Choose: Option 1 (Quick Test)
```

**Expected Output**:
```
📊 RESULTS:
  ├─ Successful (200):  100
  ├─ Success Rate:      100%
  ├─ Requests/sec:      65.66
  └─ Average:           0.234s
```

### Example 2: What's our breaking point?
```bash
./simple-load-test.sh
# Choose: Option 4 (Progressive Load Test)
```

This tests: 10 → 50 → 100 → 500 → 1,000 → 2,000 requests

**Watch for**: Where response time increases significantly.

### Example 3: Visual analysis with Locust
```bash
locust -f locust-load-test.py --host=http://localhost:8081
# Open: http://localhost:8089
# Set users: 500, spawn rate: 25
# Click: Start swarming
```

**View**: Real-time graphs, statistics, and charts.

---

## 🔧 **Prerequisites**

### **Must Be Running**:
1. ✅ Keycloak (Port 8080)
2. ✅ Backend (Port 8081)
3. ✅ User `provider1` with password `password123`

### **Check Services**:
```bash
# Keycloak
curl http://localhost:8080

# Backend
curl http://localhost:8081/actuator/health
```

### **If Not Running**:
```bash
# Start Keycloak
cd /Users/sajeev/Documents/cmips-security/keycloak-25.0.0
./bin/kc.sh start-dev --http-port=8080 &

# Start Backend
cd /Users/sajeev/Documents/cmips-security/cmipsapplication/backend
mvn spring-boot:run &
```

---

## 📚 **Documentation**

- **Quick Start**: Read `QUICKSTART.md` (5-minute guide)
- **Full Guide**: Read `README.md` (comprehensive documentation)
- **This Summary**: `SUMMARY.md` (you are here)

---

## 🎨 **Advanced Features**

### **Custom Load Shapes** (Locust)
```bash
# Step Load (gradual increase)
locust -f locust-load-test.py --headless --shape StepLoadShape

# Spike Load (sudden spikes)
locust -f locust-load-test.py --headless --shape SpikeLoadShape
```

### **Headless Mode** (No GUI)
```bash
# Locust headless
locust -f locust-load-test.py --headless -u 1000 -r 50 -t 5m --html report.html

# JMeter headless
jmeter -n -t jmeter-test-plan.jmx -l results.jtl -e -o html-report
```

### **Generate Reports**
```bash
# Bash script saves CSV to: ./results/
# Locust can generate HTML reports
# JMeter generates beautiful dashboards
```

---

## 🚨 **Troubleshooting**

### Problem: "Token failed"
**Solution**: Check Keycloak is running and user exists

### Problem: "Connection refused"
**Solution**: Check backend is running on port 8081

### Problem: High error rate
**Solution**: Reduce concurrent users, check backend logs

### Problem: Slow response times
**Solution**: Check database performance, increase connection pool

---

## 📊 **Success Metrics**

Your system is performing well if:

| Metric | Target | Status |
|--------|--------|--------|
| Success Rate | > 99% | ✅ |
| Avg Response Time | < 500ms | ✅ |
| 95th Percentile | < 1000ms | ✅ |
| Throughput | > 50 req/s | ✅ |
| Concurrent Users | > 100 | ✅ |
| Error Rate | < 1% | ✅ |

---

## 🎯 **Recommended Testing Strategy**

### **Phase 1: Baseline** (Day 1)
```bash
# Test with 100 requests
./simple-load-test.sh → Option 1
```
**Goal**: Establish baseline performance

### **Phase 2: Scale Up** (Day 2)
```bash
# Test with 1000 requests
./simple-load-test.sh → Option 2
```
**Goal**: Verify system can handle normal load

### **Phase 3: Find Limits** (Day 3)
```bash
# Progressive load test
./simple-load-test.sh → Option 4
```
**Goal**: Find breaking point

### **Phase 4: Spike Test** (Day 4)
```bash
# Spike load test
locust -f locust-load-test.py --headless --shape SpikeLoadShape
```
**Goal**: Test resilience to traffic spikes

### **Phase 5: Endurance** (Day 5)
```bash
# Long-running test
locust -f locust-load-test.py --headless -u 200 -r 20 -t 2h
```
**Goal**: Find memory leaks, verify stability

---

## 🔥 **Most Common Commands**

```bash
# 1. Quick test (easiest)
./simple-load-test.sh

# 2. Visual analysis
locust -f locust-load-test.py --host=http://localhost:8081

# 3. JMeter GUI
jmeter -t jmeter-test-plan.jmx

# 4. Headless test with 1000 users
locust -f locust-load-test.py --host=http://localhost:8081 --headless -u 1000 -r 50 -t 5m

# 5. Progressive load test
./simple-load-test.sh  # Choose Option 4
```

---

## 📞 **Getting Help**

### Check Logs:
```bash
# Backend
tail -f /Users/sajeev/Documents/cmips-security/cmipsapplication/backend/backend.log

# Keycloak
tail -f /Users/sajeev/Documents/cmips-security/keycloak-25.0.0/data/log/keycloak.log
```

### Monitor System:
```bash
# CPU and Memory
htop

# Network connections
netstat -an | grep 8081
```

---

## 🎉 **You're Ready!**

Start with the simplest tool:

```bash
cd /Users/sajeev/Documents/cmips-security/load-testing
./simple-load-test.sh
```

**Choose Option 1** for your first test!

---

## 📊 **Results Storage**

All test results are saved in:
```
/Users/sajeev/Documents/cmips-security/load-testing/results/
```

Each test creates:
- **CSV files** with timing data
- **JTL files** (JMeter format)
- **HTML reports** (with `--html` flag)

---

## 🚀 **Next Steps**

1. ✅ Run baseline test (100 requests)
2. ✅ Analyze results
3. ✅ Run progressive test to find limits
4. ✅ Optimize based on findings
5. ✅ Run endurance test
6. ✅ Document performance characteristics

---

**Happy Load Testing!** 🎯

For detailed instructions, see `QUICKSTART.md` or `README.md`






