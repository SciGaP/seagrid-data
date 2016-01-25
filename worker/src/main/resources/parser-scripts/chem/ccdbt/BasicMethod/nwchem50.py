#!/usr/bin/env python
# Method/BasicMethod/nwchem50.py
# method class in this module is nwchem50.nwchem

import default #default.py ---must have
import re
from default import PERIODIC_TABLE, RECORD_SEP, LINE_SEP, PAIRSEPARATOR, ENDOFLINES # important
from Parser import * # ---must have
import string

#############################################

	########################
	#bunch of nwchem cards;#
	########################

	#ALL
#CARDS_ALL = ['MEMORY','RESTART','BASIS','GEOMETRY','ZMAT','GRID','CUBE','CARTESIAN','SPHERICAL','SORT','CPP','RHF-SCF','HF-SCF','RHF','UHF-SCF','UHF','HF','DFT','KS','RKS','UKS','MULTI','MCSCF','CASSCF','CASVB','MRCI','CI-PRO','CI','ACPF','AQCC','CEPA','RS2','RS3','MP2','MP3','MP4','CISD','CCSD\(T\)','CCSD','BCCD','QCI','QCSID','UCCSD','RCCSD','FCI','FULLCI','LOCALI','MERGE','POP','DMA','PROPERTY','DIP','QUAD','PIGLO','IGLO','NMR','FORCES','OPT','OPTG','MIN','PUT','HESSIAN','FREQUENCY','MASS','DDR'] #slashes are for the usage of re pattern match.

	#DFT names 
DFT_NAME = ['ACM','B3LYP','BECKEHANDH','PBE0','BECHE97GGA1','BECKE97-2','BECKE97-1','BECKE97','BECKE98','HCTH120','HCTH147','HCTH407P','HCTH','HCTH407','MPW91','MPW1K','XFT97','CFT97','FT97','XPKZB99','CPKZB99','XTPSS03','CTPSS03','XCTPSSH','HFEXCH.*?','BECKE88.*?','XPERDEW91.*?','XPBE96.*?','GILL96.*?','LYP.*?','PERDEW81.*?','PERDEW86.*?','PERDEW91.*?','CPBE96.*?','PW911DA.*?','SLATER.*?','VWN_1_RPA.*?','VWN_1.*?','VWN_2.*?','VWN_3.*?','VWN_4.*?','VWN_5.*?']

	#TCE stuff
TCE_REF = ['DFT','HF','SCF']
TCE_FREEZE = ['FREEZE.*?']
TCE_METHOD = ['LCCD','CCD','CCSD\(T\)','CCSD\[T\]','CCSD','LCCSD','CCSDTQ','CCSDT','QCISD','CISDTQ','CISDT','CISD','MBPT2','MBPT3','MBPT4','MP2','MP3','MP4']
	
	#Method
CARDS_METHOD = ['TDDFT','PSPW','BAND','(?:;)TCE(?:;.*?END;)','SELCI','MCSCF','SODFT','DFT','DIRECT_MP2','RIMP2','MP2','CCSD\+\(CCSD\)','CCSD\(T\)','(?:;)CCSD(?:;.*?END;)','MD','SCF'] 

	#CalcType
CARDS_CALCTYPE = ['ENERGY','GRADIENT','OPTIMIZE','SADDLE','HESSIAN','FREQUENCIES','FREQ','PROPERTY','DYNAMICS','THERMODYNAMICS']

	#Basis
DEFBAS = ['DZ\\W\\*\\+\\W\\*DOUBLE\\W\\*RYDBERG\\W\\*\\(DUNNING-HAY\\)', 'SV\\W\\*\\+\\W\\*DOUBLE\\W\\*RYDBERG\\W\\*\\(DUNNING-HAY\\)', 'BINNING-CURTISS\\W\\*\\(1D\\)\\W\\*POLARIZATION', 'BINNING-CURTISS\\W\\*\\(DF\\)\\W\\*POLARIZATION', 'CORE/VAL\\.\\W\\*FUNCTIONS\\W\\*\\(CC-PCV6Z\\)', 'DGAUSS\\W\\*A1\\W\\*DFT\\W\\*EXCHANGE\\W\\*FITTING', 'CORE/VAL\\.\\W\\*FUNCTIONS\\W\\*\\(CC-PCV5Z\\)', 'CORE/VAL\\.\\W\\*FUNCTIONS\\W\\*\\(CC-PCVQZ\\)', 'CORE/VAL\\.\\W\\*FUNCTIONS\\W\\*\\(CC-PCVDZ\\)', 'CORE/VAL\\.\\W\\*FUNCTIONS\\W\\*\\(CC-PCVTZ\\)', 'DGAUSS\\W\\*A2\\W\\*DFT\\W\\*EXCHANGE\\W\\*FITTING', 'DGAUSS\\W\\*A2\\W\\*DFT\\W\\*COULOMB\\W\\*FITTING', 'DGAUSS\\W\\*A1\\W\\*DFT\\W\\*COULOMB\\W\\*FITTING', 'POPLE\\W\\*\\(2DF,2PD\\)\\W\\*POLARIZATION', 'POPLE\\W\\*\\(3DF,3PD\\)\\W\\*POLARIZATION', 'SVP\\W\\*\\+\\W\\*RYDBERG\\W\\*\\(DUNNING-HAY\\)', 'SVP\\W\\*\\+\\W\\*DIFFUSE\\W\\*\\(DUNNING-HAY\\)', 'STUTTGART\\W\\*RSC\\W\\*SEGMENTED/ECP', 'SV\\W\\*\\+\\W\\*RYDBERG\\W\\*\\(DUNNING-HAY\\)', 'DUNNING-HAY\\W\\*DOUBLE\\W\\*RYDBERG', 'LANL2DZDP\\W\\*ECP\\W\\*POLARIZATION', 'POPLE\\W\\*\\(2D,2P\\)\\W\\*POLARIZATION', 'AHLRICHS\\W\\*COULOMB\\W\\*FITTING', 'AUG-CC-PV\\(T\\+D\\)Z\\W\\*DIFFUSE', 'SVP\\W\\*\\+\\W\\*DIFFUSE\\W\\*\\+\\W\\*RYDBERG', 'SDB-AUG-CC-PVQZ\\W\\*DIFFUSE', 'DZP\\W\\*\\+\\W\\*RYDBERG\\W\\*\\(DUNNING\\)', 'AUG-CC-PV\\(6\\+D\\)Z\\W\\*DIFFUSE', 'GLENDENING\\W\\*POLARIZATION', 'AUG-CC-PV\\(5\\+D\\)Z\\W\\*DIFFUSE', 'AUG-CC-PV\\(D\\+D\\)Z\\W\\*DIFFUSE', 'SDB-AUG-CC-PVTZ\\W\\*DIFFUSE', 'DZP\\W\\*\\+\\W\\*DIFFUSE\\W\\*\\(DUNNING\\)', 'AUG-CC-PV\\(Q\\+D\\)Z\\W\\*DIFFUSE', 'DZ\\W\\*\\+\\W\\*RYDBERG\\W\\*\\(DUNNING\\)', 'STUTTGART\\W\\*RSC\\W\\*1997\\W\\*ECP', 'HAY-WADT\\W\\*VDZ\\W\\*\\(N\\+1\\)\\W\\*ECP', 'D-AUG-CC-PV6Z\\W\\*DIFFUSE', 'DEMON\\W\\*COULOMB\\W\\*FITTING', 'HAY-WADT\\W\\*MB\\W\\*\\(N\\+1\\)\\W\\*ECP', 'STUTTGART\\W\\*RSC\\W\\*ANO/ECP', 'HUZINAGA\\W\\*POLARIZATION']

DEFBAS.extend(['6-311G\\*\\*\\W\\*POLARIZATION', 'D-AUG-CC-PVTZ\\W\\*DIFFUSE', 'BLAUDEAU\\W\\*POLARIZATION', 'D-AUG-CC-PV5Z\\W\\*DIFFUSE', 'D-AUG-CC-PVDZ\\W\\*DIFFUSE', 'D-AUG-CC-PVQZ\\W\\*DIFFUSE', 'AHLRICHS\\W\\*POLARIZATION', 'CHIPMAN\\W\\*DZP\\W\\*\\+\\W\\*DIFFUSE', '6-31G\\*\\*\\W\\*POLARIZATION', 'PARTRIDGE\\W\\*UNCONTR\\.\\W\\*2', 'PARTRIDGE\\W\\*UNCONTR\\.\\W\\*1', 'BINNING/CURTISS\\W\\*VTZP', 'PARTRIDGE\\W\\*UNCONTR\\.\\W\\*3', '6-311G\\*\\W\\*POLARIZATION', 'STO-3G\\*\\W\\*POLARIZATION', 'POPLE-STYLE\\W\\*DIFFUSE', 'DUNNING-HAY\\W\\*RYDBERG', '3-21G\\*\\W\\*POLARIZATION', 'HONDO7\\W\\*POLARIZATION', 'AUG-CC-PV5Z\\W\\*DIFFUSE', 'AUG-CC-PV6Z\\W\\*DIFFUSE', 'BINNING/CURTISS\\W\\*SVP', 'MCLEAN/CHANDLER\\W\\*VTZ', 'AUG-CC-PVTZ\\W\\*DIFFUSE', 'AUG-CC-PVDZ\\W\\*DIFFUSE', 'DZVP2\\W\\*\\(DFT\\W\\*ORBITAL\\)', 'DUNNING-HAY\\W\\*DIFFUSE', 'AUG-CC-PVQZ\\W\\*DIFFUSE', 'BINNING/CURTISS\\W\\*VTZ', '6-31G\\*\\W\\*POLARIZATION', 'NASA\\W\\*AMES\\W\\*CC-PCVQZ', 'BINNING/CURTISS\\W\\*SV', 'NASA\\W\\*AMES\\W\\*CC-PCV5Z', 'DZVP\\W\\*\\(DFT\\W\\*ORBITAL\\)', 'TZVP\\W\\*\\(DFT\\W\\*ORBITAL\\)', 'NASA\\W\\*AMES\\W\\*CC-PCVTZ', '6-311\\+\\+G\\(3DF,3PD\\)', 'FELLER\\W\\*MISC\\.\\W\\*CVDZ', 'NASA\\W\\*AMES\\W\\*CC-PVQZ', 'NASA\\W\\*AMES\\W\\*CC-PV5Z', 'FELLER\\W\\*MISC\\.\\W\\*CVTZ', 'DHMS\\W\\*POLARIZATION', 'STUTTGART\\W\\*RLC\\W\\*ECP', 'SVP\\W\\*\\(DUNNING-HAY\\)', 'NASA\\W\\*AMES\\W\\*CC-PVTZ', 'FELLER\\W\\*MISC\\.\\W\\*CVQZ', 'CC-PVQZ\\W\\*FI\\W\\*SF\\W\\*FW', 'BAUSCHLICHER\\W\\*ANO', 'CC-PVQZ\\W\\*FI\\W\\*SF\\W\\*LC', 'CC-PVDZ\\W\\*FI\\W\\*SF\\W\\*SC', 'CC-PVQZ\\(SEG-OPT\\)', 'CC-PVTZ\\(SEG-OPT\\)', 'CC-PVQZ\\W\\*PT\\W\\*SF\\W\\*SC', 'CC-PV5Z\\W\\*PT\\W\\*SF\\W\\*SC', 'CC-PVQZ\\W\\*PT\\W\\*SF\\W\\*LC', 'CC-PVDZ\\(SEG-OPT\\)', 'CC-PVTZ\\W\\*FI\\W\\*SF\\W\\*SC', 'CC-PVDZ\\W\\*PT\\W\\*SF\\W\\*FW', 'SV\\W\\*\\(DUNNING-HAY\\)', 'CC-PV5Z\\W\\*FI\\W\\*SF\\W\\*SC', 'CC-PVTZ\\W\\*PT\\W\\*SF\\W\\*FW', 'CC-PVQZ\\W\\*PT\\W\\*SF\\W\\*FW', 'CC-PVQZ\\W\\*FI\\W\\*SF\\W\\*SC', 'CC-PV5Z\\W\\*FI\\W\\*SF\\W\\*FW', 'CC-PVDZ\\W\\*PT\\W\\*SF\\W\\*LC', 'CC-PVDZ\\W\\*PT\\W\\*SF\\W\\*SC', 'CC-PV5Z\\W\\*PT\\W\\*SF\\W\\*LC', 'CC-PV5Z\\W\\*PT\\W\\*SF\\W\\*FW', 'CC-PVDZ\\W\\*FI\\W\\*SF\\W\\*FW', 'CC-PVTZ\\W\\*PT\\W\\*SF\\W\\*LC', 'CC-PVTZ\\W\\*PT\\W\\*SF\\W\\*SC', 'CC-PVTZ\\W\\*FI\\W\\*SF\\W\\*FW', 'CC-PVTZ\\W\\*FI\\W\\*SF\\W\\*LC', 'CC-PVDZ\\W\\*FI\\W\\*SF\\W\\*LC', 'CC-PV5Z\\W\\*FI\\W\\*SF\\W\\*LC', 'SDB-AUG-CC-PVTZ', '6-311\\+\\+G\\(2D,2P\\)', 'AUG-CC-PV\\(D\\+D\\)Z', 'AUG-CC-PV\\(Q\\+D\\)Z', 'SDB-AUG-CC-PVQZ', 'MINI\\W\\*\\(HUZINAGA\\)', '6-31G\\*-BLAUDEAU', 'AUG-CC-PV\\(T\\+D\\)Z'])

DEFBAS.extend(['6-311G\\(2DF,2PD\\)', 'AUG-CC-PV\\(6\\+D\\)Z', 'MIDI\\W\\*\\(HUZINAGA\\)', 'AUG-CC-PV\\(5\\+D\\)Z', '6-31G-BLAUDEAU', 'CC-PVTZ-FIT2-1', '6-31G\\(3DF,3PD\\)', 'CC-PVDZ-FIT2-1', 'LANL2DZDP\\W\\*ECP', 'DZP\\W\\*\\(DUNNING\\)', 'MINI\\W\\*\\(SCALED\\)', 'D-AUG-CC-PVQZ', 'D-AUG-CC-PVTZ', 'D-AUG-CC-PVDZ', 'D-AUG-CC-PV5Z', 'NASA\\W\\*AMES\\W\\*ANO', 'AHLRICHS\\W\\*PVDZ', 'D-AUG-CC-PV6Z', 'SBKJC\\W\\*VDZ\\W\\*ECP', 'AUG-CC-PCVQZ', 'TZ\\W\\*\\(DUNNING\\)', 'AUG-CC-PCV5Z', 'DZ\\W\\*\\(DUNNING\\)', 'AHLRICHS\\W\\*TZV', 'AUG-CC-PCVDZ', 'AHLRICHS\\W\\*VTZ', 'AUG-CC-PCVTZ', 'AHLRICHS\\W\\*VDZ', 'SDB-CC-PVQZ', 'AUG-CC-PV5Z', 'CC-PV\\(D\\+D\\)Z', 'SADLEJ\\W\\*PVTZ', 'CC-PV\\(Q\\+D\\)Z', 'CC-PV\\(6\\+D\\)Z', 'CC-PV\\(T\\+D\\)Z', 'CC-PV\\(5\\+D\\)Z', 'AUG-CC-PVQZ', 'GAMESS\\W\\*PVTZ', 'AUG-CC-PVTZ', 'LANL2DZ\\W\\*ECP', 'AUG-CC-PVDZ', 'SDB-CC-PVTZ', 'AUG-CC-PV6Z', 'CRENBS\\W\\*ECP', 'CC-PVDZ\\W\\*DK', '6-311\\+\\+G\\*\\*', 'CC-PVTZ\\W\\*DK', 'CRENBL\\W\\*ECP', 'CC-PV5Z\\W\\*DK', 'CC-PVQZ\\W\\*DK', 'WACHTERS\\+F', 'GAMESS\\W\\*VTZ', '6-31\\+\\+G\\*\\*', '3-21\\+\\+G\\*', 'CC-PCV5Z', 'CC-PCV6Z', '6-311\\+G\\*', 'CC-PCVDZ', 'CC-PCVTZ', '6-311G\\*\\*', 'CC-PCVQZ', '6-31\\+\\+G\\*', 'CC-PV5Z', 'STO-3G\\*', 'CC-PVQZ', 'CC-PV6Z', '6-31\\+G\\*', '3-21GSP', '6-31\\+\\+G', '4-22GSP', '3-21\\+\\+G', 'CC-PVTZ', 'CC-PVDZ', '6-311G\\*', '6-31G\\*\\*', '3-21G\\*', 'STO-6G', '6-31G\\*', 'STO-3G', '6-311G', 'STO-2G', '3-21G', '6-31G', 'MIDI!', '4-31G', 'PV6Z', 'WTBS'])



def rawstd2jmol(rawstd_geom, title=''):
	"""Convert a gaussian, molpro, nwchem standard orientation string to jmol readable string.
	 7
	 geometry
	 O                     1.80139886    -0.77906529    -0.10524317
	 H                     2.68337314    -0.39194034    -0.14904602
	 H                     1.15553488    -0.05529376    -0.06225138
	 O                    -0.63984719     0.87393540     0.04152872
	 C                    -1.53457028     0.05825187     0.08493079
	 H                    -1.34594453    -1.00798474     0.06666966
	 H                    -2.57795508     0.34674685     0.14475859
	"""
	try:
		if not rawstd_geom:
			return None
		g_list = rawstd_geom.splitlines()
		for x in range(g_list.count('')):
			g_list.remove('')
		g_list2 = []
		for geom in g_list:
			geom_line_list = geom.split()
			if len(geom_line_list) == 4:
				atom_tag = geom_line_list[0] + '__'
				if atom_tag[0:2] in PERIODIC_TABLE.values():
					geom_line_list[0] = atom_tag[0:2]
				else:
					if atom_tag[0:1] in PERIODIC_TABLE.values(): 
						geom_line_list[0] = atom_tag[0:1]
					else:
						if atom_tag[0] in PERIODIC_TABLE.values():
							geom_line_list[0] = atom_tag[0] #normalize the atom tag
			compact_geom = string.join(geom_line_list)
			g_list2.append(compact_geom)
		if len(g_list2) == 0: return None
		if not g_list2[0].isdigit(): return None
		if title:
			g_list[1] = title
		num_atom = int(g_list2[0])
		if len(g_list2) != (num_atom + 2):
			print 'Wrong number of atoms..'
			return None
		geom_jmol = '|' + string.join(g_list2, '|') + '|' + '\n'
	
	except:
		print 'Unexpect error occurs during nwchem geometry normalization..'	
		return None
	else:
		return geom_jmol #act_charge is active nuclear charge in a calculation (all but ecp)
		#geom_jmol is like:
		# '|numatom|title|at1 x y z|at2 x y z|at3 x y z|...|\n'


def InputParse(molin):
	"""molin(Molpro-Input) is the molpro input string in the format of upper-case."""
	#print '[debug]molin:\t',molin 
	lines = molin.splitlines(True)
	length = len(lines)
	newlines = []
	for x in range(length):
		temp_line = (lines.pop().splitlines()[0]).split('#')[0].strip() #remove documentations
		if not temp_line.endswith('\\') and  not temp_line.endswith(';'):
			temp_line += ';'
		if not temp_line.isspace() and (temp_line.strip() != ';') and len(temp_line):
			newlines.append(temp_line)
	n_length = len(newlines)
	newlines2 = []
	line_2_append = ''
	for y in range(n_length): #begin to parse
		line_pop = newlines.pop()
		if line_pop.endswith('\\'):
			line_2_append += line_pop
			pass
		else:
			line_2_append += line_pop
			newlines2.append(line_2_append)
			line_2_append = ''
	#print '[debug]newlines2:\t', newlines2	

	#do title:
	Title = ''
	for line in newlines2:
		if line.count('TITLE') >= 1:
			#print '[debug]title:\t',line
			Title = line.split('TITLE',1)[1].strip().replace('"','').replace("'",'')
			break

	n2_length = len(newlines2) #get the compact string list
	if n2_length == 0: return 0
	Methods,CalcType,InputButGeom = '','','' #initialize
	input_str = string.join(newlines2,'')
	#print '[debug]input_str:\n', input_str
	# Now build a re compiler:
	comp_method_str2 = '(?:TASK +(' + string.join(CARDS_METHOD,'|') + '))+'
	comp_method_str = '(?:;+(' + string.join(CARDS_METHOD,'|') + '))+'
	
		 #string to build the compiler
	comp_calctype_str = '(' + string.join(CARDS_CALCTYPE,'|') + ')+'
	comp_basis_str = '(' + string.join(DEFBAS,'|') + ')+'
	#print '[debug]comp_basis_str:\t', comp_basis_str
	comp_method = re.compile(comp_method_str,re.I)
	comp_method2 = re.compile(comp_method_str2,re.I)
	comp_calctype = re.compile(comp_calctype_str,re.I)
	comp_basis = re.compile(comp_basis_str,re.I)
	#Get results:
	#print '[debug]string_input:\t', input_str
	Methods_other_list = filter(None,re.findall(comp_method2, input_str))
	Methods_list = filter(None,re.findall(comp_method, input_str))
	Methods_list.extend(list(set([ meth for meth in Methods_other_list if meth not in Methods_list ])))


	#do dft, tce ajustify.
	#1) dft
	comp_dft_str = '(?:XC *(?:(' + string.join(DFT_NAME,'|') + ')+))+'
	comp_dft = re.compile(comp_dft_str, re.I)
	
	global DFT_list
	DFT_list = re.findall(comp_dft, input_str)
	#print '[debug]dft_list', DFT_list
	def dft_mapper(method):
		if method == 'DFT':
			try:
				dft = DFT_list.pop(0)
			except IndexError:
				return method
			else:
				return 'DFT(' + dft + ')'
		else:
			return method
	Methods_list = map(dft_mapper, Methods_list)
	del DFT_list
	#print '[debug]m_l_aft_dft:\t', Methods_list


	#2) tce
	#comp_tce_str = '(?:;TCE;*?.*?
	comp_tce_str = '(?:;TCE;.*?(DFT;|HF;|SCF;)*.*?(FREEZE.*?;)*.*?(' + string.join(TCE_METHOD, ';|') + ';)*.*?END;)+'
	comp_tce = re.compile(comp_tce_str,re.I)
	
	global TCE_list
	TCE_list = re.findall(comp_tce, input_str)
	#print '[debug]tce_list', TCE_list
	def tce_join(tce_tuple):
		tcelist = []
		t1,t2,t3 = tce_tuple
		if t1: 
			t1 = t1[:-1]
			tcelist.append(t1)
		if t2: 
			t2 = t2[:-1]
			tcelist.append(t2)
		if t3: 
			t3 = t3[:-1]
			tcelist.append(t3)
		return 'TCE(' + string.join(tcelist,',') + ')'
	TCE_list = map(tce_join, TCE_list)
	#print '[debug]tce_list', TCE_list

	def tce_mapper(method):
		if method == 'TCE':
			try:
				tce = TCE_list.pop(0)
			except IndexError:
				return method
			else:
				return tce
		else:
			return method
	
	Methods_list = map(tce_mapper, Methods_list)
	#print '[debug]method_list:\t', Methods_list
	del TCE_list

	Methods_other = string.join(Methods_other_list,',') #Methods = 'method1, method2, method3...'
	Methods = string.join(Methods_list,',')

	#print '[debug]met2:\t', Methods_other_list
	#print '[debug]met_all:\t', Methods_list	
	
	#print '[debug]CalcType:\t', re.findall('TASK (.*?);', input_str)	
	CalcType = string.join(re.findall('TASK (.*?);', input_str), ',')
	#print '[debug]CalcType:\t', re.findall('TASK (.*?);', input_str)

	#CalcType = string.join((string.join((re.findall(comp_calctype, input_str)),' ').split()),',')
	input_str2 = re.sub('GEOMETRY.*?END','GEOM',input_str) #strip all geometry specifications out..
	basis_list = re.findall(';(.*?LIBRARY.*?);',input_str2) #enter parsing basis set(composite)
	#print '[debug]basis', basis_list
	basis_list2 = [] #for nwchem general basis set will be specified by tag '*'  
		#re.findall('BASIS\s*=*([^\s]*?);',input_str2) #single basis specification
	#print '[debug]basis2', basis_list2
	basis_str = (string.join(basis_list2)).replace('LIBRARY','') + string.join(basis_list)
	#debug_basis = re.findall(comp_basis, basis_str)
	#print '[debug]basis match:\t', debug_basis
	
	#not sure which to use, maybe need to add a column BasisSum, to keep Basis set summury
	#Basis = string.join((string.join((re.findall(comp_basis, basis_str)),' ').split()),',') #Basis = 'bas1, bas2, bas3...'
	Basis = basis_str
	InputButGeom = re.sub('BASIS.*?END','BASIS',input_str2) #strip all basis set specifications out..
	InputButGeom = re.sub('ECP.*?END','ECP',InputButGeom)
	InputButGeom = re.sub('SO.*?END','SO',InputButGeom)
	result_dict = {}
	if Title: result_dict.update({'Title':Title})
	if Methods: result_dict.update({'Methods':Methods})
	if CalcType: result_dict.update({'CalcType':CalcType})
	if Basis: result_dict.update({'Basis':Basis})
	if InputButGeom: result_dict.update({'InputButGeom':InputButGeom})
	if not result_dict: return 0
	else: return result_dict
		



class nwchem(default.method):
	"""This is the molpro basic method, inheriting from C{method}.
	{default.method} has the line: from Parser import *
	"""
	def ParseInput(self,lines):
		"""Extrat echoed input from molpro output files, then draw data from."""
		try:
			input_sect = std.findsection(lines,'================ echo of input deck ==============','================ echo of input deck ==============','============================================================================',myfilter='capitalize',iflstrip=True)
		except:
			return None
		if not input_sect: #then try if input in the paired files
			try:
				input_sect = std.findsection(lines, PAIRSEPARATOR, PAIRSEPARATOR, ENDOFLINES, myfilter='capitalize',iflstrip=True)
			except:
				return None
			if not input_sect:
				return 0 #no match, return!
		#input_sect = input_sect.upper()
		input_dict = InputParse(input_sect)
		if not input_dict: return 0
		else:
			self.result.update(input_dict)
			return 1
	def pre(self, lines): #will be evoked by self.parse_default
		self.ParseInput(lines)
		return lines 



	def init_geom(self,linelist):
		"""Take the first geometry from molpro output file."""
		#print '[debug]Now analyse the init geom..'
		try:
			raw_init_geom = std.findsection(linelist,'XYZ format geometry','------------','^\s*$')
		except:
			return None
		#print '[debug]raw_geom=:\n\t', raw_init_geom 
		if not raw_init_geom: return None
		# without joinlines, only match once. return value is a string. 

		# raw_init_geom is like: """
		#         7
		#         geometry
		#         O                     1.80139886    -0.77906529    -0.10524317
		#         H                     2.68337314    -0.39194034    -0.14904602
		#         H                     1.15553488    -0.05529376    -0.06225138
		#         O                    -0.63984719     0.87393540     0.04152872
		#         C                    -1.53457028     0.05825187     0.08493079
		#         H                    -1.34594453    -1.00798474     0.06666966
		#         H                    -2.57795508     0.34674685     0.14475859
		#"""
		geom_jmol = rawstd2jmol(raw_init_geom, 'Initial Geometry')
		if not geom_jmol: return None
		else:
			if self.result.has_key('InitGeom'):
				self.result['InitGeom'] += (LINE_SEP + geom_jmol)
			else: self.result['InitGeom'] = geom_jmol
			#Now, do formula:
			atom_list = []
			atom_list2 = (geom_jmol.split('|'))[3:] #['at1 x y z', 'at2 x y z', 'at3 x y z', '...']
			atom_list2 = filter(None, atom_list2) #remove ''
			#for num_empty in range(atom_list2.count('')):
			#	atom_list2.remove('')
			#print '[debug]atom_list2:\t',atom_list2
			for atom_a in atom_list2:
				try:
					atom = atom_a.strip().split()[0]
					#print '[debug]atom2:\t',atom
				except:
					pass
				else:
					if len(atom) >= 3:
						atom = atom[0].upper() + atom[1:2].lower()
						if atom in PERIODIC_TABLE.values():
							pass
						else:
							if atom[0:1] in PERIODIC_TABLE.values():
								atom = atom[0:1]
							else:
								if atom[0] in PERIODIC_TABLE.values(): 
									atom = atom[0]
								else:
									pass
					if len(atom) == 2:
						atom = atom[0].upper() + atom[1].lower()
						if atom in PERIODIC_TABLE.values():
							pass
						else:
							if atom[0] in PERIODIC_TABLE.values():
								atom = atom[0]
							else:
								pass
					if len(atom) == 1:
						atom = atom[0].upper()
						
					#print '[debug]atom:\t',atom
					atom_list.append(atom)
			atom_set = list(set(atom_list))
			atom_set.sort()
			#print '[debug]atom_list:\t',atom_list
			formula = ''
			if 'C' in atom_set:
				atomcount = str(atom_list.count('C'))
				atom_set.remove('C')
				formula += ('C' + atomcount)
			if 'H' in atom_set:
				atomcount = str(atom_list.count('H'))
				atom_set.remove('H')
				formula += ('H' + atomcount)	
			for atom in atom_set:
				atomcount = str(atom_list.count(atom))
				formula += (atom + atomcount)
			#Now Charge:
			try:
				total_charge_dict = std.findline_s(linelist, 'tc', 'CHARGE\s*=\s*([^\s]*)')
			except:
				return None
			if not total_charge_dict or not total_charge_dict.values()[0] or not total_charge_dict.values()[0].isdigit():
				total_charge =  0
			else:
				try:
					total_charge = int(total_charge_dict['tc'])
				except:
					total_charge = 0

#pending ..
			try:
				multi_dict = std.findline_s(linelist, 'mul', 'Spin multiplicity:\s*([^\s]*)')
			except:
				return None
			if not multi_dict or not multi_dict.values()[0] or not multi_dict.values()[0].isdigit():
				#blablabla
				try:
					alpha_e_dict = std.findline_s(linelist,'alpha','Alpha electrons\s*:\s*([^\s]*)')
				except:
					return None
				try:
					beta_e_dict = std.findline_s(linelist,'beta','Beta electrons\s*:\s*([^\s]*)')
				except:
					return None
				if not alpha_e_dict or not beta_e_dict or not alpha_e_dict.values()[0] or not beta_e_dict.values()[0] or not alpha_e_dict.values()[0].isdigit() or not beta_e_dict.values()[0].isdigit():
					multi = 1 
				else:
					alpha_e = int(alpha_e_dict['alpha'])
					beta_e = int(beta_e_dict['beta'])
					multi = 2 * abs(alpha_e - beta_e) + 1
					
			else:
				multi = int(multi_dict['mul'])
			
			if str(total_charge).isdigit():
				if total_charge > 0: charge_str = str(total_charge)+'+'
				if total_charge < 0: charge_str = str(abs(total_charge)) + '-'
				if total_charge == 0: charge_str = '0'
			else:
				charge_str = total_charge
			formula += ( '('+ charge_str + ',' + str(multi) + ')' )
			if str(total_charge).isdigit():
				self.result['Charge'] = str(total_charge)
			if str(multi).isdigit():
				self.result['Multiplicity'] = str(multi)
			self.result['Formula'] = formula
			return 1
			
	def final_geom(self,linelist):
		"""Parse the last geometry from the nwchem output file."""
		try:
			raw_init_geom = std.findsection(linelist,'XYZ format geometry','------------','^\s*$',last_match=True)
		except:
			return None
		if not raw_init_geom: return None
		geom_jmol = rawstd2jmol(raw_init_geom,'Last Geometry')
                if not geom_jmol: return None
                else:
                	if self.result.has_key('FinalGeom'):
                        	self.result['FinalGeom'] += (LINE_SEP + geom_jmol[0])
                        else: self.result['FinalGeom'] = geom_jmol[0]
                        return 1

	def energy(self,linelist):
		"""Parse energy from molpro06 outputs. The last match will be written into 'Energy', others go to 'OtherInfo'."""
		try: 
			P_index = linelist.index(PAIRSEPARATOR)
		except ValueError: pass
		else:
			linelist = linelist[:P_index]

		while True:
			try:
				lines_str_dict = std.findline_s(linelist,'energy_str','Total ([^\s]+ energy:\s*[^\s]*)',fullmatch=True, myfilter='compress')
			except:
				return None
			if not lines_str_dict: break 
			lines_str = lines_str_dict['energy_str']
			#print '[debug]energy_str:\t', lines_str
			if not lines_str: break
			energy_str_list = lines_str.split(RECORD_SEP)
			energy_str_list = filter(None, energy_str_list) # to remove ''
			#for x in range(energy_str_list.count('')):
			#	energy_str_list.remove('')
			if not energy_str_list: break
	
			#print '[debug]energy_str_list:\t',energy_str_list
			from copy import deepcopy
			temp_str_list = deepcopy(energy_str_list)
			for energy_str in temp_str_list:
				#print '[debug]en_str_one:\t', energy_str
				if energy_str.count('orbital') >= 1:
					#print '[debug]has orbi'
					energy_str_list.remove(energy_str)
					#print '[debug]',energy_str_list
			if not energy_str_list: break		
	
			for energy_str in energy_str_list[:-1]:
				energy_str_new = string.join(energy_str.split('energy: '),' ') + RECORD_SEP
				if self.result.has_key('OtherInfo'):
					self.result['OtherInfo'] += energy_str_new
				else: self.result['OtherInfo'] = energy_str_new
			trash, fin_energy = energy_str_list[-1:][0].split('energy:',1)
			#print '[debug]fin_energy:\t', fin_energy
			try:
				f_energy = fin_energy.strip()
			except:
				if self.result.has_key('OtherInfo'):
					self.result['OtherInfo'] += string.join(energy_str_list[-1:][0].split('energy: '))
				else: self.result['OtherInfo'] = string.join(energy_str_list[-1:][0].split('energy: '))
			else:
				self.result['Energy'] = f_energy
				fin_energy_str = string.join(energy_str_list[-1:][0].split('energy: '))
				if self.result.has_key('OtherInfo'):
					self.result['OtherInfo'] += fin_energy_str
				else: self.result['OtherInfo'] = fin_energy_str
			break
			# for TDDFT especially:
		try:
			TDDFT = std.findsection(linelist,'NWChem TDDFT Module','NWChem TDDFT Module','Task\s*times\s*cpu')
		except:
			return None
		if not TDDFT:
			return
		TDDFT_list = TDDFT.splitlines()
		ground, excited = [], []
		for tddft_line in TDDFT_list:
			tddft_line_strip = tddft_line.strip()
			# Ground state energy =   -108.945393441153
			if tddft_line_strip.count('Ground') == 1 and tddft_line_strip.count('state') == 1 and tddft_line_strip.count('energy') == 1:
				try:
					ground.append(tddft_line_strip.split()[4])
				except:
					continue
			if tddft_line_strip.count('Excited') == 1 and tddft_line_strip.count('state') == 1 and tddft_line_strip.count('energy') == 1:
				try:
					excited.append(tddft_line_strip.split()[4])
				except: continue

		if not ground or not excited or len(ground) != len(excited):
			return
		else:
			tddft_result = ['TDDFT(gr,ex):' + ground[x] + ',' + excited[x] for x in range(len(ground))]		
		#print '[debug]tddft:\t', ground, excited,'\n', tddft_result
		for tddft_res in tddft_result:
			if self.result.has_key('OtherInfo'):
				self.result['OtherInfo'] += RECORD_SEP + tddft_res
			else:
				self.result['OtherInfo'] = tddft_res
		#TDDFT_match = re.findall('((?:Ground\s*state\s*energy\s*=\s*)[^\s]+(?:\s*.*?Excited\s*state\s*energy\s*=\s*)[^\s]+)+',TDDFT,re.I)
		return 1

	def status(self,linelist):
		if self.keys.has_key('JobStatus'):
			self.executeone(linelist,'JobStatus') ########### defined in parser loader by user
		else:
			try:
				std.findkey_return_str(linelist,'return_value','Total times','CalcDone','Unfinished',fullmatch=True)
                        except:
				return None
			if not status_dict or not status_dict.values()[0]:
                                return 0
                        else:
                                self.result['JobStatus'] = status_dict['return_value']
	
	

	def parse(self,metafile):
		from copy import deepcopy				
		lines = deepcopy(metafile.lines)
		lines = self.parse_default(lines) #this is initialization, only do pre() and 'parsedby' by default
			
		self.init_geom(lines)
                try: #remove strings from the pair files
                        P_index = lines.index(PAIRSEPARATOR)
                except ValueError: pass
                else:
                        lines = lines[:P_index]
		self.final_geom(lines)
		self.energy(lines)
		self.executeall(lines)
		return self.result
