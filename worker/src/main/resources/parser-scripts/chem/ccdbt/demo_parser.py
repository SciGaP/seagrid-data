#!/usr/bin/env python
# Method/demo_parser.py

from ParserLoader import ParserPyramid
from MetaFile import metafile

parser_pyramid = ParserPyramid()
#print 'ParserPyramid Created',repr(parser_pyramid), type(parser_pyramid), parser_pyramid
if not parser_pyramid:
	print 'Fail to create a parser_pyramid' 
	raise SystemExit	
#print '\t[Parser_pyramid(3-level)]:\n', parser_pyramid.__dict__

#print


meta_f_path = '/Users/supun/Downloads/Gaussian.log'

#print '\t[File path]:\t', meta_f_path

#print
meta_f = metafile(meta_f_path,'1','remote-path','3','1')
#print 'meta_f',meta_f
if meta_f.parse(parser_pyramid):
#	print '\t[Parsing result]:\n', meta_f.record
#	print
	print meta_f.record
#else:
#	print meta_f.lines
#	print meta_f.suffix
#	print 'fail to parse'
