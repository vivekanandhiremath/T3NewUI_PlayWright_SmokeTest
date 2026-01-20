#!/bin/bash




# Set script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORT_DIR="${SCRIPT_DIR}/../reports/allure-report_$(date +'%Y-%m-%d_%H-%M-%S')"
CATEGORIES_FILE="${SCRIPT_DIR}/../allure-categories.json"

# Create necessary directories
mkdir -p "${SCRIPT_DIR}/../reports/allure-results"
mkdir -p "${SCRIPT_DIR}/../reports/screenshots"
mkdir -p "${SCRIPT_DIR}/../logs"

# Clean previous results if they exist
rm -rf "${SCRIPT_DIR}/../reports/allure-results/*"

# Parse command line arguments
HEADLESS=false
while [[ $# -gt 0 ]]; do
    case "$1" in
        --headless)
            HEADLESS=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

echo "Running tests in $(if [ "$HEADLESS" = true ]; then echo "HEADLESS"; else echo "NORMAL"; fi) mode..."

# Run the test
cd "${SCRIPT_DIR}/.." || exit 1

if [ "$HEADLESS" = true ]; then
    python3 -m pytest Testcase/T3_Testcase/test_T3_Smoke_Test.py --headless -v --alluredir=reports/allure-results
else
    python3 -m pytest Testcase/T3_Testcase/test_T3_Smoke_Test.py -v --alluredir=reports/allure-results
fi

# Generate and open Allure report
echo "Generating Allure report..."

# Check if categories file exists
if [ -f "$CATEGORIES_FILE" ]; then
    echo "Using categories from: $CATEGORIES_FILE"
    mkdir -p "${SCRIPT_DIR}/../reports/allure-results/categories"
    cp "$CATEGORIES_FILE" "${SCRIPT_DIR}/../reports/allure-results/categories/categories.json"
else
    echo "Warning: $CATEGORIES_FILE not found. Generating report without categories."
fi

# Generate the report
allure generate reports/allure-results -o "$REPORT_DIR" --clean

echo "Allure report generated at: $REPORT_DIR"
echo "To view the report, run: allure open \"$REPORT_DIR\""

# Open the report in default browser
if command -v xdg-open > /dev/null; then
    xdg-open "file://$(realpath "$REPORT_DIR/index.html")" >/dev/null 2>&1
elif command -v open > /dev/null; then
    open "file://$(realpath "$REPORT_DIR/index.html")" >/dev/null 2>&1
fi