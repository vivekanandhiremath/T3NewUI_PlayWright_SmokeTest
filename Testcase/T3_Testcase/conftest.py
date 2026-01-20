# Testcase/T3_Testcase/conftest.py
import pytest


def pytest_addoption(parser):
    parser.addoption("--headless", action="store_true", help="Run tests in headless mode")


@pytest.fixture
def headless(request):
    return request.config.getoption("--headless")
