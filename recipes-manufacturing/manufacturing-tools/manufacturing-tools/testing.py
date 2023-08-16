#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2019-07-08
# Version: 0.1
#
# Copyright (c) 2019 Gardena GmbH

"""
@package testing
Collection of testing-related utility classes and functions.
"""

import unittest

class ResultHandler(unittest.TextTestResult):
    """Our own result handler, which also takes care of feeding data to CPMS."""

    ipr_data_values = []
    ipr_data_results = []


    def addError(self, test, err):
        """ Add new error to test results."""
        super(ResultHandler, self).addError(test, err)
        self.storeResult(test._testMethodName, "ERR", str(err)) # pylint: disable=protected-access

    def addFailure(self, test, err):
        """ Add new failure to test results."""
        super(ResultHandler, self).addFailure(test, err)
        self.storeResult(test._testMethodName, "FAIL", str(err)) # pylint: disable=protected-access

    def addSuccess(self, test):
        """ Add new success to test results."""
        super(ResultHandler, self).addSuccess(test)
        self.storeResult(test._testMethodName, "OK") # pylint: disable=protected-access

    def addSkip(self, test, reason):
        """ Add new skipped to test results."""
        super(ResultHandler, self).addSkip(test, reason)
        self.storeResult(test._testMethodName, "SKIP", str(reason)) # pylint: disable=protected-access

    def addExpectedFailure(self, test, err):
        """ Add new expected failure to test results."""
        super(ResultHandler, self).addExpectedFailure(test, err)
        self.storeResult(test._testMethodName, "EXP_FAIL", str(err)) # pylint: disable=protected-access

    def addUnexpectedSuccess(self, test):
        """ Add new unexpected success to test results."""
        super(ResultHandler, self).addUnexpectedSuccess(test)
        self.storeResult(test._testMethodName, "UNEXP_OK") # pylint: disable=protected-access

    def storeValue(self, name, key, value):
        """Function to store values to later feed data to CPMS."""
        self.ipr_data_values.append({'test_name': name, 'key': key, 'value': value})

    def storeResult(self, name, status, message=""):
        """Function to store results to later feed data to CPMS."""
        self.ipr_data_results.append({'test_name': name, 'test_status': status, 'test_message': message})
