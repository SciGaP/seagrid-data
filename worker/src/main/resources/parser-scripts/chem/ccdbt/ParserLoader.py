#!/usr/bin/env python
# Method/ParserLoader
from os.path import abspath, dirname, join
from Interpreter import interprete_dir
from BasicMethod import *

PARSER_LIBRARY_DIR = join(dirname(abspath(__file__)),'MethodLibrary')
class ParserPyramid:
	"""
	A parser class which can dynamicly load parameters to create run-time parsers.
	"""
	
	def LoadOne(self,parser_name,parser_setting):
		if not parser_setting:
			print 'An empty setting file loaded [ %s ] ..' % (parser_name)
			return 0
		try: 
			method = parser_setting['basic']['method']['value'] #method name
			method_p1, method_p2 = method.split('.',1)
			#print 'debug method name', method
			#print 'debug globals', globals()
		except: 
			print 'Method name not defined, use default.method by default..'
			method = 'default.method'
			parser_setting['basic']['method']['value'] = method
		else: 
			if not globals().has_key(method_p1):
				print 'Invalid method name detected, use default.method by default..'
				method = 'default.method'
				parser_setting['basic']['method']['value'] = method
			else:
				if not hasattr((eval(method_p1)), method_p2):
					print 'Invalid method name detected, use default.method by default..'
					method = 'default.method'
					parser_setting['basic']['method']['value'] = method
		#print '[debug]final_method', method
		try: suffix = parser_setting['basic']['suffix']['para'] #suffix
		except: 
			suffix = ['*']
			parser_setting['basic']['suffix']['para'] = suffix
			print 'Suffix not defined, use * by default..'
		else: 
			for suf in suffix:
				if not suf.isalnum():
					print 'Invalid suffix detected, use * by default..'
					suffix.remove(suf)
					suffix.append('*')
					parser_setting['basic']['suffix']['para'] = suffix

		try:
			verify = parser_setting['basic']['verify']['para'] #inner tag
		except:
			print 'Key list for verifying is essential, Incomplete setting file error..'
			return 0
		else:
			if type(verify) != type([]):
				print 'Wrong type error(list)..'
				return 0
			for key in verify:
				if type(key) != type(''):
					print 'Wrong type error(string)..'
					return 0
		try: priority = int(parser_setting['basic']['priority']['value']) #priority		
		except:
			print "Fail to recognize parser's priority.. set to lowest priority.."
			priority = 1 #lowest priority
			parser_setting['basic']['priority']['value'] = '1'
		else: pass
		
		#for key in parser_setting['basic'].keys(): #debug
		#	print key,'\t','\t',parser_setting['basic'][key]
		#	print ''
		one_parser = eval(method)(parser_setting)
		if not one_parser: return 0
		else:
			return {parser_name:{'suffix':suffix,'verify':verify,'parser':one_parser,'priority':priority}}

	def __init__(self,dirpath=PARSER_LIBRARY_DIR):
		"""Create a 3-level pyramid-parser."""
		self.parsers = {}
		try:
			composit_dict = interprete_dir(dirpath)  #{name:setting}
		except:
			print 'Fail to load parameters..'
			self.nonzero=0
		else:
			#print 'debug,comp_dict', composit_dict
			if composit_dict:
				
				for (parser_name,parser_setting) in composit_dict.items():
					#print '[debug]', parser_name, parser_setting.keys()
					loadone = self.LoadOne(parser_name,parser_setting)
					if not loadone:
						continue # fail to create a parser, try next
					else:	
						self.parsers.update(loadone) # append to parser dict
			
				# create 3 level dict
				if not self.parsers: return 0
				self.level_1 = dict([(name,self.parsers[name]['suffix']) for name in self.parsers])
				self.level_2 = dict([(name,self.parsers[name]['verify']) for name in self.parsers])
				self.level_3 = dict([(name,self.parsers[name]['parser']) for name in self.parsers])
				self.priority_dict = dict([(name,self.parsers[name]['priority']) for name in self.parsers])
				
				self.nonzero=1 #  for __nonzero__(self)	
			else:
				#print 'debug, fail, none' 
				self.nonzero=0
	def __nonzero__(self):
		"""True-value testing"""
		if not self.__dict__.has_key('nonzero'): return 0
		if self.nonzero: return 1
		else: return 0


