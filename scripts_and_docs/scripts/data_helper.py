#!/usr/bin/env python3

"""
data_helper.py

Dependency-injected data provider service for reading profile data.
Designed to be extensible for multiple data sources and consumers.
"""
import csv
import logging

from abc import ABC, abstractmethod
from random import choice
from typing import Dict, Optional
from pathlib import Path

log = logging.getLogger(__name__)

class DataProvider(ABC):
    """
    Abstract base class for data providers.
    """

    @abstractmethod
    def get_all_records(self) -> list[Dict[str, str]]:
        """
        Retrieve all records from the data source.
        
        Returns:
            List of dictionaries where keys are column names and values are the corresponding data.
        """
        #raise NotImplementedError("Subclasses must implement get_all_records method.")
        pass
    
    @abstractmethod
    def get_random_record(self) -> Optional[Dict[str, str]]:
        """
        Retrieve a random record from the data source.

        Returns:
            Dictionary representing a single record, or None if no records are available.
        """
        pass

    @abstractmethod
    def get_record_count(self) -> int:
        """
        Get the total number of records in the data source.

        Returns:
            Integer count of records.
        """
        pass

class CSVDataProvider(DataProvider):
    """CSV file data provider implementation."""

    def __init__(self, file_path: Path):
        """
        Initialize CSV data provider.

        Args:
            file_path: Path to the CSV file.

        Raises:
            FileNotFoundError: If the CSV file does not exist.
            ValueError: If the CSV file is empty or malformed.
        """
        # normalize and store the path on the instance before using it
        self.file_path = Path(file_path)

        if not self.file_path.exists():
            raise FileNotFoundError(f"CSV file not found: {file_path}")

        self._load_csv()

    def _load_csv(self):
        """Load and parse CSV file into memory."""
        log.debug("Loading CSV from: %s", self.file_path)

        try:
            with self.file_path.open(mode='r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                self.records = list(reader)
        except csv.Error as e:
            log.error("Error reading CSV file: %s", e, exc_info=True)
            raise ValueError(f"Error reading CSV file: {e}") from e
        except Exception as e:
            log.error("Unexpected error: %s", e, exc_info=True)
            raise

        if not self.records:
            raise ValueError(f"CSV file is empty or has no valid records: {self.file_path}")

        log.info("Loaded %d records from CSV.", len(self.records))

    def get_all_records(self) -> list[Dict[str, str]]:
        """Retrieve all records from the CSV file."""
        return self.records.copy()

    def get_random_record(self) -> Optional[Dict[str, str]]:
        """Get a random record from CSV."""
        if not self.records:
            return None
        return choice(self.records)

    def get_record_count(self) -> int:
        """Get total number of records."""
        return len(self.records)

class DataHelperService:
    """
    Service layer for dat operations.
    Uses dependency injection to work with any DataProvider implementation.
    """

    def __init__(self, provider: DataProvider):
        """
        Initialize service with a data provider.

        Args:
            provider: Implementation of DataProvider interface.
        """
        self.provider = provider
        log.debug("DataHelperService initialized with provider: %s",
                  type(provider).__name__)

    def get_random_user(self) -> Optional[Dict[str, str]]:
        """
        Get a random user profile record from the data source.

        Returns:
            Dictionary with user data or None if no records available.
        """
        record = self.provider.get_random_record()

        if record:
            log.debug("Retrieved random user: %s [Email: %s]",
                      record.get('Names'), record.get('Email'))
        else:
            log.warning("No user records available.")

        return record

    def get_random_admin(self) -> Optional[Dict[str, str]]:
        """
        Get a random admin profile record from the data source.

        Returns:
            Dictionary with admin data or None if no records available.
        """
        all_records = self.provider.get_all_records()
        admins = [r for r in all_records if r.get('App_Role') == 'ROLE_ADMIN']

        if not admins:
            log.warning("No admin records found")
            return None

        admin = choice(admins)
        log.debug("Retrieved random admin: %s", admin.get('Names'))
        return admin

    def get_random_reviewer(self) -> Optional[Dict[str, str]]:
        """
        Get a random reviewer profile from the available records.

        Returns:
            A Dictionary with the reviewer data or None if no available reviewer records are left.
        """
        all_records = self.provider.get_all_records()
        reviewers = [r for r in all_records if r.get('App_Role') == 'ROLE_REVIEWER']

        if not reviewers:
            log.warning("No viewer records were found")
            return None

        reviewer = choice(reviewers)
        return reviewer

    def get_random_by_role(self, role: str) -> Optional[Dict[str, str]]:
        """
        Get a random user profile record based on the specified role.
        Args:
            role: Role to filter by.
        Returns:
            Dictionary with user data or None if no records available.
        """
        all_records = self.provider.get_all_records()
        log.info("Selecting random user with role: %s", role)
        users_with_role = [r for r in all_records if r.get('App_Role') == role]

        if not users_with_role:
            log.warning("The application could not find any users with the role %s", role)
            return None

        random_user = choice(users_with_role)
        return random_user

    def get_record_count(self) -> int:
        """Get total number of records in the data source."""
        return self.provider.get_record_count()

def create_data_helper_service(csv_path: Optional[Path] = None) -> DataHelperService:
    """
    Factory method to create a DataHelperService instance.

    Args:
        csv_path: Path to the CSV file. Defaults to ../dataset.csv

    Returns:
        An instance of DataHelperService

    Raises:
        FileNotFoundError: If dataset file is not found.
        ValueError: If the CSV dataset is empty or malformed.
    """
    if csv_path is None:
        # Default: Look for dataset.csv one level up from this file
        csv_path = Path(__file__).parent.parent / "dataset.csv"

    log.info("Creating DataHelperService with CSV: %s", csv_path)

    provider = CSVDataProvider(csv_path)
    return DataHelperService(provider)

if __name__ == "__main__":
    logging.basicConfig(
        level=logging.DEBUG,
        format="[%(asctime)s %(levelname)s:%(name)s:%(message)s]",
        handlers=[logging.StreamHandler()]
    )

    service = create_data_helper_service()
    log.info("Total records: %d", service.get_record_count())

    random_user = service.get_random_user()
    if random_user:
        print("\nRandom User:")
        print(f"    Name: {random_user['Names']}")
        print(f"    Email: {random_user['Email']}")
        print(f"    Role: {random_user['App_Role']}")