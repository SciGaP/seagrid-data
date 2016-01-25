#!/usr/bin/env python
# Method/BasicMethod/gaussian.py
# method class in this module is gaussian.gaussian

import default #default.py
import re
from default import PERIODIC_TABLE, RECORD_SEP, LINE_SEP, PAIRSEPARATOR, ENDOFLINES
from Parser import *
import string

def archive_sep(archfield):
	"""Separate gaussian 03 archive filed into 5 pieces."""
	try:
		archfield = string.replace(archfield,'\\ \\', '\\\\')
	except:
		pass
	try:
		f1,f2,f3,f4,f5,drop_value,drop_value2 = (archfield + '\\\\\\\\').split('\\\\',6)
	except:
		print 'Unexpected error when parsing the archived field..'
		return None
	else: 
		if f5.startswith('Version'):
			return (f1,f2,f3,f4,f5)
		else: # in the case of z-matrix
			f4 = f4 + '@' + f5
			f5 = drop_value
			return (f1,f2,f3,f4,f5)

def archive_1_parse(sumfield):
	"""UAHPC-COMPUTE-3-7\\Freq\\RB3LYP\\GenECP\\C51H43As1F1O2P2Pd1(1+)\\MCHEN10\\06-Jun-2008\\0"""
	dict={}
	try:
		head1, head2, dict['CalcMachine'],dict['CalcType'],dict['Methods'],dict['Basis'],dict['Formula'],dict['CalcBy'],dict['FinTime'],drop_value = sumfield.split('\\',9)
	except: return None
	else: return dict

def archive_2_parse(sumfield):
	"""#p 5D 7F b3lyp DGDZVP2"""
	if len(sumfield) == 0:
		return None
	else: return {'Keywords':sumfield}

def archive_3_parse(sumfield):
        """Title section"""
        if len(sumfield) == 0:
                return None
        else: return {'Title':sumfield}

def archive_4_parse(sumfield,title='untitiled'):
	"""Geometry. Convert into string read by jmol."""
	dict4 = {}
	if len(sumfield) == 0:
                return None
	if sumfield.count('@') > 1:
		print 'Z-Matrix has been used.'
		#return 'debug' # here needs a converter.
	numatom = sumfield.count('\\')
	if numatom == 0:
		return None
	charge_multi, rawgeom = sumfield.split('\\',1)
	try:
		charge, multi = charge_multi.split(',',1)
	except:
		charge = None
		multi = None
		print '[error]when extract charge and multi\t'
	else:
		dict4.update({'Charge':charge,'Multiplicity':multi})
	from string import join
	raw_geom = join(join(rawgeom.split(','),' ').split('\\'),'|')
	geom  = '|' + str(numatom) + '|' + title + '|' + raw_geom + '\n'
	dict4.update({'FinalGeom': geom})
	return dict4

def archive_5_parse_one(sumfield,keyname,recordname):
	"""\\Version=AM64L-G03RevD.02\State=1-A\HF=-769.6487177\RMSD=6.496e-09\RMSF=5.948e-05"""
	pat = keyname+'='+'(?P<result>[^\\\\]*)'
	match = re.search(pat,sumfield,re.I)
	if match:
		try:
			temp_dict = match.groupdict()
			if temp_dict.has_key('result'):
				return {recordname:temp_dict['result']}
			else: return None
		except:
			return None
	else:
		return None

def archive_5_parse(sumfield,key_dict): #key_dict is a dictionary {recordname:keypattern}
	result_dict = {}
	for (recordname,keyname) in key_dict.items():
		try:
			if type(keyname)!=type('') or type(recordname)!=type(''):
				continue
			else:
				temp_dict = archive_5_parse_one(sumfield,keyname,recordname)
				if not temp_dict: continue
				else:
					if not result_dict.has_key(recordname):
						result_dict.update(temp_dict)
					else:
						result_dict[recordname] += (RECORD_SEP + temp_dict[recordname])
		except:
			print 'Error or fail when analysing %s..' % (recordname)
	if not result_dict: return None
	else:
		return result_dict

def rawstd2jmol(rawstd_geom, title='Untitled'):
	"""Convert a gaussian standard orientation string to jmol readable string."""
	try:
		raw_list = rawstd_geom.splitlines()
		for num_empty in range(raw_list.count('')):
			raw_list.remove('')
		numatom = len(raw_list)
		geom_jmol = '|'+str(numatom) + '|' + title
		for atom in raw_list:
			try:atom_seg = atom.strip().split() #['1', '13', '0', '0.000000', '0.000000', '0.000000']
			except: continue
			else:
				try:
					atom_jmol = '|' + PERIODIC_TABLE[atom_seg[1]] + ' ' + atom_seg[3] + ' ' +atom_seg[4] + ' ' + atom_seg[5]
				except: continue
				else:
					geom_jmol += atom_jmol
		geom_jmol += '\n'
	except:
		print 'Unexpect error occurs during geometry normalization..'	
		return None
	else:
		return geom_jmol


class gaussian(default.method):
	"""This is the gaussian baisic method, inheriting from C{method}.
	{default.method} has the line: from Parser import *
	"""
	def init_geom_std(self,linelist):
		"""Take the first standard orientation geometry from gaussian output file."""
		try:
			raw_init_geom = std.findsection(linelist,'Standard orientation:','----------------------','-----------------------',bra_skip=1) 
		except:
			return None
		if not raw_init_geom: return None
		# without joinlines, only match once. return value is a string. but skip the 1st match of '-------'

		# raw_init_geom is like: """
		#    1         13             0        0.000000    0.000000    0.000000\n
		#    2          8             0        0.000000    0.000000    1.842599\n
		#    3          8             0        1.675945    0.000000   -0.515496\n
		#    4          8             0       -1.127404   -1.085048   -0.773904\n
		#"""
		geom_jmol = rawstd2jmol(raw_init_geom, 'Initial Standard Orientation')
		if not geom_jmol: return None
		else:
			if self.result.has_key('InitGeom'):
				self.result['InitGeom'] += (LINE_SEP + geom_jmol)
			else: self.result['InitGeom'] = geom_jmol
			return 1
			
	def init_geom_input(self,linelist):
		"""Take the first standard orientation geometry from gaussian output file."""
		try:
			raw_init_geom = std.findsection(linelist,'Input orientation:','--------------------','-------------------',bra_skip=1)
		except:
			return None
		if not raw_init_geom: return None
		geom_jmol = rawstd2jmol(raw_init_geom, 'Initial Input Orientation')
                if not geom_jmol: return None
                else:
                	if self.result.has_key('InitGeom'):
                        	self.result['InitGeom'] += (LINE_SEP + geom_jmol)
                        else: self.result['InitGeom'] = geom_jmol
                        return 1
	
	def formula(self, linelist):
		"""Take the first standard orientation geometry from gaussian output file, and get the formula.
		But if there is already a formula in the parsing result, formula() will be skiped.
		"""
		if self.result.has_key('Formula'):
			return None
		try:
			raw_init_geom = std.findsection(linelist,'Input Orientation:','--------------------','--------------------',bra_skip=1)
		except:
			return None
		if not raw_init_geom: return None
		atom_list = []
		atoms = raw_init_geom.splitlines()
		for atom_a in atoms:
			try:
				atom = atom_a.strip().split()[1]
			except:
				pass
			else:
				if atom in PERIODIC_TABLE:
					atom_list.append(PERIODIC_TABLE[atom])
		atom_set = list(set(atom_list))
		atom_set.sort()
		formula = ''
		if 'C' in atom_set:
			atomcount = str(atom_list.count('C'))
			atom_set.remove('C')
			formula += ('C' + atomcount)
		if 'H' in atom_set:
			atomcount = str(atom_list.count('H'))
			atom_set.remove('H')
			formula += ('H' + atomcount)
		#if 'O' in atom_set:
		#	atomcount = str(atom_list.count('O'))
		#	atom_set.remove('O')
		#	formula += ('O' + atomcount)
		for atom in atom_set:
			atomcount = str(atom_list.count(atom))
			formula += (atom + atomcount)
		try:
			charge_dict = std.findline_s(linelist,'Charge','Charge\s=\s*([^\s]*)',fullmatch=False)		
			multi_dict = std.findline_s(linelist,'Multiplicity','Multiplicity\s=\s*([^\s]*)',fullmatch=False)
		except:
			print "charge_dict = std.findline_s(linelist,'Charge','Charge\s=\s*([^\s]*)',fullmatch=False) error"
			print "multi_dict = std.findline_s(linelist,'Multiplicity','Multiplicity\s=\s*([^\s]*)',fullmatch=False) error"
			return None
		try:
			charge = charge_dict['Charge']
		except: formula += '(,' #e.g. C3H5O9Cl(,
		else:
			if int(charge) > 0:
				formula += ('(' + charge + '+,') #e.g. C3H5O9Cl(1+,
			if int(charge) < 0:
				formula += ('(' + str(-int(charge)) + '-,')  #e.g. C3H5O9Cl(2-,
			if int(charge) == 0:
				formula += ('(' + charge + ',') #e.g. C3H5O9Cl(0,
		try:
			multi = multi_dict['Multiplicity']
		except: 
			formula += ')'
		else:
			formula += (multi + ')')

		if not formula: return None
		else: #C3H5O9Cl(1+,2)
			self.result['Formula'] = formula
			return 1

	def pre2(self,linelist):
		"""Join data field leading by '1\\1\\', endswith '@'"""
		try:
			sect_dict=std.findsection_dict(linelist,'section','1\\\\1\\\\','1\\\\1\\\\','@',joinlines=True,fullmatch=True,bra_include=True,cket_include=True,iflstrip=True) #remove leading white spaces
		except:
			print "sect_dict=std.findsection_dict(linelist,'section','1\\\\1\\\\','1\\\\1\\\\','@',joinlines=True,fullmatch=True,bra_include=True,cket_include=True,iflstrip=True) error"
			return None
		if type(sect_dict) == type({}):
			if len(sect_dict.values()) == 1:
				return sect_dict.values()[0] # return the data field in a string format.
		return None

	def status(self,linelist):
		if self.keys.has_key('JobStatus'):
			self.executeone(linelist,'JobStatus') ########### defined in parser loader by user
		else: std.findline_s
	
	def fieldparsing(self,datafield):
		"""Parsing gaussian output within the summary data field.
		Generate Basic information about a calculation.

		['CalcMachine','CalcType','Methods','Basis','Formula','CalcBy','FinTime','Keywords','Title','Charge','Multiplicity','FinalGeom','CodeVersion','ElecSym','PG','Energy','HF','Thermal','Dipole','ZPE','NImag','S2']
		"""
		numfield = datafield.count('@\n')
		if numfield == 0:
			print 'No complete data summary.'
			return None
		else:
			fields = datafield.split('@\n')[:-1]
		for archfield in fields:
			archfields = archive_sep(archfield)
			if not archfields: continue
			f1,f2,f3,f4,f5 = archfields
			d1=archive_1_parse(f1)
			if d1: 
				for key in d1:
					if key != 'Formula': #Formula is unique
						if key not in self.result:
							self.result[key]=d1[key]
						else:
							self.result[key] += (RECORD_SEP + d1[key])
					else:
						continue
						### pass the following
						formula = d1[key]
						if formula.count('(') == 0:
							formula = formula + '(0,1)' #neutral, singlet
						else:
							if formula.count(',') == 1: #charged, not singlet
								pass
							else: #neutral, not singlet; or ; charged singlet
								formula_basic, ch_m = formula.split(')',1)[0].split('(',1)
								try: 
									temp_ch_m = int(ch_m)
								except: #ch_m is charge
									ch_m = '(' + ch_m + ',1)'
								else: #ch_m is multiplicity
									ch_m = '(0,' + ch_m + ')'
								formula = formula_basic + ch_m
						if key not in self.result:
							#reorder the formula here!!
							pureformula,charge_multipli = formula.split('(') #'C1H4O12Zr31' , '1-,1)'
							charge_multipli = '('+charge_multipli
							cterm=''
							hterm=''
							elemlist = re.sub('(?P<num>[0-9]+)','\g<num>,',pureformula).split(',')[:-1] #['C1', 'H4', 'O12', 'Zr31']
							for elenum in elemlist:
								if elenum[0] == 'C' and elenum[1].isdigit():
									cterm = elenum
									elemlist.remove(elenum)
								if elenum[0] == 'H' and elenum[1].isdigit():
									hterm = elenum
									elemlist.remove(elenum)
							elemlist.sort()
							sorted_part_of_formula = string.join(elemlist,"")
							formula = cterm + hterm + sorted_part_of_formula
                                                        self.result[key] = formula
						else:
							pass
			d2=archive_2_parse(f2)
			if d2:
				for key in d2:
                                        if key not in self.result:
                                                self.result[key]=d2[key]
                                        else:
                                                self.result[key] += (RECORD_SEP + d2[key])
 
			d3=archive_3_parse(f3)
			if d3:
				for key in d3:
                                        if key not in self.result:
                                                self.result[key]=d3[key]
                                        else:
                                                self.result[key] += (RECORD_SEP + d3[key])
				title = d3['Title']
				d4=archive_4_parse(f4,title)
			else:d4=archive_4_parse(f4)
			if d4:
				for key in d4:
                                        if key not in self.result:
                                                self.result[key]=d4[key]
                                        else:
						if key == 'Charge' or key == 'Multiplicity':
							pass
						else:
                                                	self.result[key] += (RECORD_SEP + d4[key])
			
			if d1:
				if d1.has_key('Methods'):
					pure_method = d1['Methods']
					if d1['Methods'].count('-') == 1:
						pure_method = d1['Methods'].split('-')[0]
					if pure_method.startswith('R') or pure_method.startswith('U'):
						pure_method = pure_method[1:]
					e_dict={'Energy':pure_method}
					pure_method_plus = '\\' + pure_method + '='
					if f5.count(pure_method_plus) == 0:
						e_dict={'Energy':'HF'}
				else:
					e_dict={}
			else: e_dict={}
			keydict = dict([
					('CodeVersion','Version'),('ElecSym','State'),('PG','PG'),('HF','HF'),
					('Thermal','Thermal'),('Dipole','Dipole'),('ZPE','ZeroPoint'),
					('NImag','NImag'),('S2','S2')
					])
			keydict.update(e_dict)
			if self.basic.has_key('archive') and type(self.basic['archive']['value']) == type({}):
				keydict.update(self.basic['archive']['value']) # add additional keys to keydict
			self.archive = keydict 
			d5=archive_5_parse(f5,keydict)
			if d5:
				for key in d5:
					kvalue = d5[key]
					if kvalue.count(' ') >= 1:
						kvalue = kvalue.split()[0]
					if kvalue.count('[') >= 1:
						kvalue = kvalue.split('[')[0]
					if key not in self.result:
						self.result[key] = kvalue 
					else:
						if self.result.has_key('OtherInfo'):
							self.result['OtherInfo'] += (RECORD_SEP + key + ':' + self.result[key])
							self.result[key] = kvalue
						else:
							self.result['OtherInfo'] = (key + ':' + self.result[key])
							self.result[key] = kvalue

	def parse(self,metafile):
		from copy import deepcopy				
		lines = deepcopy(metafile.lines)
		lines = self.parse_default(lines)
		fields = self.pre2(lines)	
		if not fields: pass
		else: self.fieldparsing(fields)
		if self.result.has_key('CodeVersion'):
			del self.result['CodeVersion']
		self.init_geom_std(lines)
                self.init_geom_input(lines)
		self.formula(lines)
		self.executeall(lines)
		return self.result
