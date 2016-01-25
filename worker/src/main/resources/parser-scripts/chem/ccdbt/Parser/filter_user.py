#!/bin/usr/env python

#.../Method/Parser/std_user.py

def orbsym(result):
	"""To remove consecutive empty space in gaussian 03 orbital sysm analysis."""
	from string import join,replace
	result = result.replace('(','')
	result = result.replace(')','')
	return join(result.split(),' ')
