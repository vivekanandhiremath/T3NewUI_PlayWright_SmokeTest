# utilities/readproperties.py
import os
from configparser import ConfigParser


class readconfig:
    _urls = {}

    @classmethod
    def _load_urls(cls):
        if not cls._urls:
            try:
                config = ConfigParser()
                config_path = os.path.join(os.path.dirname(__file__), '../confiy/confi.ini')
                config.read(config_path)

                # Get URLs from the [common info] section
                cls._urls = {
                    't3': config.get('common info', 't3BaseURL', fallback='').strip('"\' '),
                    't1': config.get('common info', 't1BaseURL', fallback='').strip('"\''),
                    'ore': config.get('common info', 'oreBaseURL', fallback='').strip('"\''),
                    'encrypted': config.get('common info', 'encryptedURL', fallback='').strip('"\'')
                }

                # Remove any empty URLs
                cls._urls = {k: v for k, v in cls._urls.items() if v}

                print(f"Loaded URLs: {cls._urls}")

            except Exception as e:
                print(f"Error loading URLs from confi.ini: {e}")
                # Fallback to default URLs
                cls._urls = {
                    't3': 'https://stage-dealeradmin.eshopdemo.net/widget/redesign',
                    't1': 'https://www.jeep.com',
                    'ore': 'https://e-shop.alfaromeousa.com',
                    'encrypted': 'https://web-ihub.eshopdemo.net'
                }
                print(f"Using default URLs: {cls._urls}")

    @classmethod
    def getApplicationT3URL(cls):
        cls._load_urls()
        return cls._urls.get('t3', '')

    @classmethod
    def getApplicationT1URL(cls):
        cls._load_urls()
        return cls._urls.get('t1', '')

    @classmethod
    def getApplicationOREURL(cls):
        cls._load_urls()
        return cls._urls.get('ore', '')

    @classmethod
    def getApplicationEncryptedURL(cls):
        cls._load_urls()
        return cls._urls.get('encrypted', '')
