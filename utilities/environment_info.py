# utilities/environment_info.py
import logging
import platform
import socket
from datetime import datetime
from pathlib import Path

import psutil


class EnvironmentInfo:
    @staticmethod
    def get_system_info():
        """Collect system information"""
        try:
            system_info = {
                "Platform": platform.system(),
                "Platform Release": platform.release(),
                "Platform Version": platform.version(),
                "Architecture": platform.machine(),
                "Hostname": socket.gethostname(),
                "IP Address": socket.gethostbyname(socket.gethostname()),
                "Processor": platform.processor(),
                "RAM": f"{round(psutil.virtual_memory().total / (1024.0 ** 3))} GB",
                "CPU Cores": psutil.cpu_count(logical=True),
                "Python Version": platform.python_version(),
                "Current Time": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            }
            return system_info
        except Exception as e:
            logging.error(f"Error collecting system info: {str(e)}")
            return {"Error": str(e)}

    @staticmethod
    def get_environment_info(config):
        """Collect environment-specific information"""
        try:
            env_info = {
                "Base URL": config.get("base_url", "Not Set"),
                "Browser": config.get("browser", "Chrome"),
                "Headless": str(config.get("headless", False)),
                "Environment": config.get("environment", "QA"),
                "Tester": config.get("Tester", "Not Specified"),
                "Test Run ID": config.get("run_id", f"RUN-{datetime.now().strftime('%Y%m%d%H%M%S')}")
            }
            return env_info
        except Exception as e:
            logging.error(f"Error collecting environment info: {str(e)}")
            return {"Error": str(e)}

    @classmethod
    def attach_environment_info(cls, config):
        """Attach environment information to Allure report"""
        try:
            # Get system and environment info
            system_info = cls.get_system_info()
            env_info = cls.get_environment_info(config)

            # Combine all info
            all_info = {
                "=== SYSTEM INFORMATION ===": "=" * 20,
                **system_info,
                "\n=== TEST ENVIRONMENT ===": "=" * 20,
                **env_info
            }

            # Create environment.properties file in allure-results directory
            results_dir = Path(__file__).parent.parent / "reports" / "allure-results"
            results_dir.mkdir(parents=True, exist_ok=True)

            env_file = results_dir / "environment.properties"

            # Log the path for debugging
            logging.info(f"Writing environment info to: {env_file.absolute()}")

            # Write to environment file
            with open(env_file, "w") as f:
                for key, value in all_info.items():
                    if not key.startswith("==="):  # Skip section headers
                        f.write(f"{key}={value}\n")
                        logging.info(f"Added to environment: {key}={value}")

            logging.info("Environment information has been written successfully")

        except Exception as e:
            logging.error(f"Failed to attach environment info: {str(e)}", exc_info=True)
