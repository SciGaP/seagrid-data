#!/usr/bin/env python

# Method/BasicMethod/default.py

from os.path import abspath, dirname
par_dir = dirname(dirname(abspath(__file__)))
from sys import path
path.append(par_dir)

PERIODIC_TABLE = {'1':'H','2':'He',
		'3':'Li','4':'Be','5':'B','6':'C','7':'N','8':'O','9':'F','10':'Ne',
		'11':'Na','12':'Mg','13':'Al','14':'Si','15':'P','16':'S','17':'Cl','18':'Ar',
		'19':'K','20':'Ca','21':'Sc','22':'Ti','23':'V','24':'Cr','25':'Mn','26':'Fe','27':'Co','28':'Ni','29':'Cu','30':'Zn','31':'Ga','32':'Ge','33':'As','34':'Se','35':'Br','36':'Kr',
		'37':'Rb','38':'Sr','39':'Y','40':'Zr','41':'Nb','42':'Mo','43':'Tc','44':'Ru','45':'Rh','46':'Pd','47':'Ag','48':'Cd','49':'In','50':'Sn','51':'Sb','52':'Te','53':'I','54':'Xe',
		'55':'Cs','56':'Ba','57':'La','58':'Ce','59':'Pr','60':'Nd','61':'Pm','62':'Sm','63':'Eu','64':'Gd','65':'Tb','66':'Dy','67':'Ho','68':'Er','69':'Tm','70':'Yb','71':'Lu','72':'Hf','73':'Ta','74':'W','75':'Re','76':'Os','77':'Ir','78':'Pt','79':'Au','80':'Hg','81':'Tl','82':'Pb','83':'Bi','84':'Po','85':'At','86':'Rn',
		'87':'Fr','88':'Ra','89':'Ac','90':'Th','91':'Pa','92':'U','93':'Np','94':'Pu','95':'Am','96':'Cm','97':'Bk','98':'Cf','99':'Es','100':'Fm','101':'Md','102':'No','103':'Lr','104':'Rf','105':'Db','106':'Sg','107':'Bh','108':'Hs','109':'Mt','110':'Uun','111':'Uuu','112':'Uub','114':'Uuq','116':'Uuh'
		}

KEY_LIST_BASIC = ['StorageHost','FileRoot','Account','FileRelativePath','User','GroupName','LastModTime','ParsedBy','RemotePath','LocalPath']
KEY_LIST_ESSENTIAL = ['Formula','Charge','Multiplicity','Title','Keywords','CalcType','Methods','Basis','NumBasis','NumFC','NumVirt','JobStatus','FinTime','InitGeom','FinalGeom','PG','ElecSym','NImag','Energy','EnergyKcal','ZPE','ZPEKcal']
KEY_LIST_EXTRA = ['HF','HFKcal','Thermal','ThermalKcal','Enthalpy','EnthalpyKcal','Entropy','EntropyKcal','Gibbs','GibbsKcal','OrbSym','Dipole','Freq','AtomWeigh','Conditions','ReacGeom','ProdGeom','MulCharge','NatCharge','S2','CodeVersion','CalcMachine','CalcBy','MemCost','TimeCost','CPUTime','Convergence','FullPath','InputButGeom','OtherInfo','Comments']
KEY_LIST_RESERVED = []
KEY_LIST_USER = []

KEY_OUT = KEY_LIST_BASIC #Info gained without parsing
KEY_IN = [] #Info get from a parser
KEY_IN.extend(KEY_LIST_ESSENTIAL)
KEY_IN.extend(KEY_LIST_EXTRA)
KEY_IN.extend(KEY_LIST_RESERVED)
KEY_IN.extend(KEY_LIST_USER)
KEY_AVAILABLE = [] #similar with KEY_IN but without reserved keys
KEY_AVAILABLE.extend(KEY_LIST_ESSENTIAL)
KEY_AVAILABLE.extend(KEY_LIST_EXTRA)
KEY_AVAILABLE.extend(KEY_LIST_USER)

KEY_ALL = []
KEY_ALL.extend(KEY_OUT)
KEY_ALL.extend(KEY_IN)

KEY_UNIQUE_LIST = ['RemotePath'] #for indexing for RC3_MAIN
KEY_UNIQUE_LIST_2 = ['LocalPath'] #for indexing for RC3_LOCAL
KEY_DATE_LIST = ['LastModTime'] #date type
KEY_INT_LIST = ['Charge','Multiplicity','NImag'] #signed integer
KEY_FLOAT_LIST = ['ZPE','ZPEKcal','MulCharge','NatCharge','S2','HF','HFKcal','Thermal','ThermalKcal','Enthalpy','EnthalpyKcal','Entropy','EntropyKcal','Gibbs','GibbsKcal','Energy','EnergyKcal']
KEY_SHORT_VARCHAR_LIST = ['StorageHost','Account','User','GroupName','ParsedBy'] #varchar(40)
KEY_LONG_VARCHAR_LIST = ['FileLocation','FullPath','RemotePath','LocalPath'] #varchar(959) len(unique index) < 1000
KEY_TEXT_LIST = ['InitGeom','FinalGeom','ReacGeom','ProdGeom','OrbSym','InputButGeom','OtherInfo','Comments']


RECORD_SEP = '; '
LINE_SEP = '\n'
PAIRSEPARATOR = 'PAIRSEPARATOR'
ENDOFLINES = 'ENDOFLINES'

from Parser import *

class method:
	
	#from Parser import *
	"""
	base class.
	intergration of file recognizer, analyser, it is more like a template, the real parsers will be created at run-time.
	define method name, method level, function list, etc.
	"""

	def verify(self):
                """Exam if there is enough informations to create a resonable parser."""
                if len(self.setting['func_list']) > self.MAX_funcs:
                        print 'Too many keyterms to parse for parser[%s]!!' % (self.name)
                        return 0
                if len([func for func in self.funcs if func not in self.known_list]) != 0:
                        print 'Unkown parsers detected..'
                        return 0
		return 1
	
	def __init__(self, setting_dict):
		"""
		Most infos are from output files,
		So input files are mostly neglected, yet will be treated as a less important component of a O{file_pair}. 
		
		O{parser}s are created by a loader at run-time, via C{method} loading both default and user-defined parameters from files, 
		to form a parse list.
		even functions in the parser is created at run-time, by loading parameters(P{Method/MethodLibrary}) and agorithm (from P{Method/Paser}).
		
		The loader also form a filter which contains all the outer_rules and pair_rules from every parser in the parser list.
		
		The filter decides which parse the {metafile} goes to. 
		
		Call a new method to analyse files
		define self.name, self.priority
		
		Define component-parser list
		"""
		self.MAX_funcs = 100
		self.nonzero = 1
		try: # essential information.
			#print 'debug,keys', setting_dict.keys().
			self.basic = setting_dict['basic']
			self.keys = setting_dict['keys']
			self.name = self.basic['name']['value'] #parser's name, which don't need to be but best be unique
			self.priority = self.basic['priority']['value']
			self.suffix = self.basic['suffix']['para']
		except:
			#print 'debug, in exception'.
			print 'Not enough imformation to create a parser.'
			try: print self.name
			except: pass
			self.nonzero = 0	
		if not (self.name or self.priority or self.suffix):
			print 'Not enough imformation to create a parser.'
			self.nonzero = 0 # these 3 are a must.		

		try: self.verify = self.basic['verify']['para']
		except: self.verify = []

		try: self.pair = self.basic['pair']['para']
		except: self.pair = [] # self.verify and self.para are lists.

		self.funcs = []
		for keyname in self.keys: # collect func list.
			self.funcs.append(self.keys[keyname]['func'])
		self.result = {}		
		if not self.verify:
			print 'Fail to create parser[ %s ]..' % (self.name)
			self.nonzero = 0

		#check if parser is legitible:
		for key in self.keys.keys():
			if len(key) < 2:
				continue
			if key not in KEY_AVAILABLE and key[:-1] not in KEY_AVAILABLE:
				print "[ParserError] key name '%s' not available.." % (key)
				del self.keys[key]
				continue 
			if self.keys[key].has_key('func') and self.keys[key]['func'].count('.')==1:
				modun, funcn = self.keys[key]['func'].split('.',1) # get module name and function name. e.g: 'std.findline' --> 'std' 'findline'
				if modun in globals() and hasattr(eval(modun),funcn):
					pass # function referrence is good
				else:
					print "[ParserError] function  '%s' in key '%s' doesn't exist.. " % (self.keys[key]['func'], key)
					del self.keys[key]
					continue
			else:
				print "[ParserError] key '%s' is empty.." % (key)
				del self.keys[key]
				continue
			



	def parsedby(self):
		self.result['ParsedBy'] = self.name
	

	def formula(self,linelist):
		"""Formula is the key for searching."""
		
	def pre(self,linelist):
		"""Pre-string-operations to make parsing easier, default operation is only pass. For gaussian like data, join line operation is nessesary."""
		return linelist

	def executeone_virtual(self,linelist,key_name,allow_duplicate=False): # reloaded by subclasses by using executeone()
		"""execute one, results are appended to the old one."""
		if not allow_duplicate:
			if self.result.has_key(key_name):
				return None
		key_name_plus_1 = key_name #copy the original name
		# but in some case, we need multiple parse even where duplicate is not allowed
		# here comes how we solve this:
		# use keyname+[char(1)] to skip the if-duplicate test 
		if key_name not in KEY_AVAILABLE:
			key_name = key_name[:-1]
			if key_name in KEY_AVAILABLE:
				pass
			else:
				return None
		para_list = [linelist, key_name]
		para_list.extend(self.keys[key_name_plus_1]['para']) # ...['para'] is a list already
		result_dict = eval(self.keys[key_name_plus_1]['func'])(*para_list)
		if not result_dict:
			return None
		else:
			if not self.result.has_key(key_name):
				self.result.update(result_dict) # record the value
			else:
				self.result[key_name] += (RECORD_SEP + result_dict[key_name])
			return 1

	def executeone(self,linelist,key_name,allow_duplicate=False):
		"""Execute one."""
		return self.executeone_virtual(linelist,key_name,allow_duplicate)

	def executeall(self,linelist,allow_duplicate=False):
		"""Execute all."""
		#line_list = self.pre(linelist)
		for key_name in self.keys: # that's why funcs' second parameter is keyname, and return value is a dictionary
			self.executeone(linelist,key_name,allow_duplicate)
		return 1
		
	def parse_default(self, lines): 
		"""
		Default steps during a parsing.	
		"""
		self.result = {} #initialize
		self.parsedby()
		return self.pre(lines)
		
		#blarblar..


	#self.parse is the interface!!!!!!
	def parse(self,metafile):
		"""
                This function is to parse every single detail info, and then put pieces together.
                """
		#Put All Together, Now We Go!!!!
		from copy import deepcopy
		lines = deepcopy(metafile.lines)
		lines = self.parse_default(lines) #you must have!
		self.executeall(lines)
		self.formula(lines)
		return self.result

	def __nonzero__(self):
                """True-value testing"""
                if not self.__dict__.has_key('nonzero'): return 0
                if self.nonzero: return 1
                else: return 0

