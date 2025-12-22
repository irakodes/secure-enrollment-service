#!/usr/bin/env python3

"""
data_helper.py

Dependency-injected data provider service for reading profile data.
Designed to be extensible for multiple data sources and consumers.
"""

from abc import ABC, abstractmethod
from ast import Dict
from typing import Optional


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
    
    def get_random_record(self) -> Optional[Dict[str, str]]:
        """
        Docstring for get_random_record
        
        :param self: Description
        :return: Description
        :rtype: Any
        """