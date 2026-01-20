from pathlib import Path
from typing import List


def get_base_url():
    """Read the base URL from the CSV file in testData directory."""
    try:
        # Get the project root directory (one level up from utilities)
        project_root = Path(__file__).parent.parent
        csv_path = project_root / "testData" / "urls.csv"

        print(f"Looking for CSV file at: {csv_path}")  # Debug print

        if not csv_path.exists():
            raise FileNotFoundError(f"CSV file not found at {csv_path}")

        with open(csv_path, 'r', newline='') as file:
            # Read the first line to get the header
            first_line = file.readline().strip()
            print(f"First line: '{first_line}'")  # Debug print

            # Read the second line to get the URL
            url = file.readline().strip()
            print(f"URL from file: '{url}'")  # Debug print

            if not url:
                raise ValueError("URL is empty in CSV file")

            return url

    except Exception as e:
        print(f"Error reading URL from CSV: {e}")
        raise  # Re-raise the exception to fail the test if URL cannot be read


def get_all_urls() -> List[str]:
    """Get all URLs from the CSV file.

    Returns:
        List[str]: A list of all URLs found in the CSV file.
    """
    try:
        # Get the project root directory (one level up from utilities)
        project_root = Path(__file__).parent.parent
        csv_path = project_root / "testData" / "urls.csv"

        if not csv_path.exists():
            raise FileNotFoundError(f"CSV file not found at {csv_path}")

        urls = []
        with open(csv_path, 'r', newline='') as file:
            # Skip header
            file.readline()

            # Read all URLs
            for line in file:
                url = line.strip()
                if url:  # Only add non-empty lines
                    urls.append(url)

        if not urls:
            raise ValueError("No URLs found in the CSV file")

        return urls

    except Exception as e:
        print(f"Error reading URLs from CSV: {e}")
        raise


def get_url_by_index(index: int) -> str:
    """Get a specific URL by its index (0-based).

    Args:
        index: The 0-based index of the URL to retrieve.

    Returns:
        str: The URL at the specified index.

    Raises:
        IndexError: If the index is out of range.
    """
    urls = get_all_urls()
    if not 0 <= index < len(urls):
        raise IndexError(f"URL index {index} is out of range. There are {len(urls)} URLs available.")
    return urls[index]


def get_url_count() -> int:
    """Get the total number of URLs in the CSV file.

    Returns:
        int: The number of URLs available.
    """
    return len(get_all_urls())
