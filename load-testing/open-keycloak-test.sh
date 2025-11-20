#!/bin/bash

echo "🚀 Opening Keycloak Token Test in JMeter"
echo "========================================"
echo ""

# Set Java environment
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "📊 Test Configuration:"
echo "  - Test Focus: Keycloak Token Generation Only"
echo "  - Users: 100 concurrent users"
echo "  - Ramp-up: 10 seconds"
echo "  - Loops: 5 per user"
echo "  - Expected Total Requests: 500 token requests"
echo "  - Target: http://localhost:8080/realms/cmips/protocol/openid-connect/token"
echo ""

# Change to the load-testing directory
cd /Users/sajeev/Documents/cmips-security/load-testing

echo "🎯 Opening JMeter with Keycloak Token Test..."
echo ""

# Open JMeter GUI with the test plan
jmeter -t keycloak-token-test.jmx

echo ""
echo "✅ JMeter opened with Keycloak Token Test!"
echo ""
echo "💡 What to expect:"
echo "1. Test plan will be loaded automatically"
echo "2. Click the green ▶️ Start button to run the test"
echo "3. Monitor Keycloak performance in real-time"
echo "4. Check Summary Report for token generation metrics"
echo ""
echo "📈 Expected Results:"
echo "  - Success Rate: 95-99%"
echo "  - Response Time: 20-100ms"
echo "  - Throughput: 50-100 tokens/second"
echo ""
echo "🎮 Ready to test Keycloak token generation!"







