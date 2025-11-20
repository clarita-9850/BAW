#!/bin/bash

echo "🚀 CMIPS Load Testing - Direct Execution"
echo "========================================"
echo ""

# Set Java environment
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "📊 Running Load Test with JMeter CLI Mode..."
echo "This will give you the best performance results!"
echo ""

# Create results directory
mkdir -p results

# Run JMeter in CLI mode (non-GUI) for better performance
echo "🎯 Starting load test..."
jmeter -n -t jmeter-test-plan-fixed.jmx -l results/load-test-results.jtl -e -o results/html-report

echo ""
echo "✅ Load test completed!"
echo ""
echo "📊 Results saved to:"
echo "  - Raw data: results/load-test-results.jtl"
echo "  - HTML report: results/html-report/index.html"
echo ""
echo "🌐 Open the HTML report:"
echo "open results/html-report/index.html"





