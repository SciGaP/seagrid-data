#!/usr/bin/env python

from os.path import abspath, dirname
#from sys import path

#get current dir
cur_dir = dirname(abspath(__file__))
par_dir = dirname(cur_dir)

#modify sys.path
#path.append(par_dir) #get global settings from upper level
#from global_setting import * # will be specified in the future

__all__ = ['MetaFile','ParserLoader','BasicMethod','MethodLibrary']
