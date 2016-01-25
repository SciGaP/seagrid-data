#!/usr/bin/env python
# Method/BasicMethod/molpro.py
# method class in this module is molpro.molpro

import default #default.py ---must have
import re
from default import PERIODIC_TABLE, RECORD_SEP, LINE_SEP, PAIRSEPARATOR, ENDOFLINES # important
from Parser import * # ---must have
import string

#############################################

	########################
	#bunch of molpro cards;#
	########################

	#ALL
CARDS_ALL = ['MEMORY','RESTART','BASIS','GEOMETRY','ZMAT','GRID','CUBE','CARTESIAN','SPHERICAL','SORT','CPP','RHF-SCF','HF-SCF','RHF','UHF-SCF','UHF','HF','DFT','KS','RKS','UKS','MULTI','MCSCF','CASSCF','CASVB','MRCI','CI-PRO','CI','ACPF','AQCC','CEPA','RS2','RS3','MP2','MP3','MP4','CISD','CCSD\(T\)','CCSD','BCCD','QCI','QCSID','UCCSD','RCCSD','FCI','FULLCI','LOCALI','MERGE','POP','DMA','PROPERTY','DIP','QUAD','PIGLO','IGLO','NMR','FORCES','OPT','OPTG','MIN','PUT','HESSIAN','FREQUENCY','MASS','DDR'] #slashes are for the usage of re pattern match.

	#Method
CARDS_METHOD = ['RHF-SCF','UHF-SCF','RHF','HF-SCF','UHF','HF','DFT','RKS','UKS','KS','MULTI','MCSCF','CASSCF','CASVB','MRCI','CI-PRO','QCI','ACPF','AQCC','CEPA','RS2','RS3','MP2','MP3','MP4','CISD','UCCSD\(T\)','RCCSD\(T\)','CCSD\(T\)','BCCD','QCSID','UCCSD','RCCSD','CCSD','FCI','FULLCI','CI']

	#CalcType
CARDS_CALCTYPE = ['POP','DMA','PROPERTY','DIP','QUAD','PLOT','PIGLO','IGLO','NMR','FORCES','OPTG','OPT','MIN','PUT','HESSIAN','FREQUENCY','DDR']

DEFBAS = ['6-311G\*\*','6-311G\*','6-311G\+\+','6-311G\+','6-311G','6-31G\*\*','6-31G\*','6-31G\+\+','6-31G\+','6-31G','3-21G\*\*','3-21G\*','3-21G\+\+','3-21G\+','3-21G','AHLRICHS-VDZ', 'AHLRICHS-VTZ', 'AHLRICHS-PVDZ', 'BINNING-SVP', 'BINNING-SV', 'BINNING-VTZP', 'BINNING-VTZ', 'ECP10MDF', 'ECP10MHF', 'ECP10MWB', 'ECP10SDF', 'ECP18MWB', 'ECP18SDF', 'ECP28MHF', 'ECP28MDF', 'ECP28MWB', 'ECP28SDF', 'ECP2MHF', 'ECP2MWB', 'ECP2SDF', 'ECP36SDF', 'ECP46MHF', 'ECP46MWB', 'ECP46SDF', 'ECP47MHF', 'ECP47MWB', 'ECP48MHF', 'ECP48MWB', 'ECP49MHF', 'ECP49MWB', 'ECP50MHF', 'ECP50MWB', 'ECP51MHF', 'ECP51MWB', 'ECP52MHF', 'ECP52MWB', 'ECP53MHF', 'ECP53MWB', 'ECP54MHF', 'ECP54MWB', 'ECP54SDF', 'ECP55MHF', 'ECP55MWB', 'ECP56MHF', 'ECP56MWB', 'ECP57MHF', 'ECP57MWB', 'ECP58MHF', 'ECP58MWB', 'ECP59MHF', 'ECP59MWB', 'ECP60MDF', 'ECP60MHF', 'ECP60MWB', 'ECP68MDF', 'ECP78MHF', 'ECP78MWB', 'ECP92MDF', 'ECP92MHF', 'ECP92MWB', 'HUZINAGA', 'ROOS', 'WACHTERS\+', 'WACHTERS', 'ACV5Z', 'ACVDZ', 'ACVQZ', 'ACVTZ', 'AUG-CC-PCV5Z', 'AUG-CC-PCVDZ', 'AUG-CC-PCVQZ', 'AUG-CC-PCVTZ', 'AUG-CC-PV5Z-DK', 'AUG-CC-PV5Z-PP', 'AUG-CC-PV5Z', 'AUG-CC-PV6Z', 'AUG-CC-PVDZ-PP', 'AUG-CC-PVDZ', 'AUG-CC-PVQZ-DK', 'AUG-CC-PVQZ-PP', 'AUG-CC-PVQZ', 'AUG-CC-PVTZ-DK', 'AUG-CC-PVTZ-PP', 'AUG-CC-PVTZ', 'AV5Z\+D', 'AV5Z-DK', 'AV5Z-PP', 'AV5Z', 'AV6Z\+D', 'AV6Z', 'AVDZ\+D', 'AVDZ-PP', 'AVDZ', 'AVQZ\+D', 'AVQZ-DK', 'AVQZ-PP', 'AVQZ', 'AVTZ\+D', 'AVTZ-DK', 'AVTZ-PP', 'AVTZ', 'CC-PCV5Z', 'CC-PCVDZ', 'CC-PCVQZ', 'CC-PCVTZ', 'CC-PV5Z-DK', 'CC-PV5Z-PP', 'CC-PV5Z', 'CC-PV6Z', 'CC-PVDZ-DK', 'CC-PVDZ-PP', 'CC-PVDZ', 'CC-PVQZ-DK', 'CC-PVQZ-PP', 'CC-PVQZ', 'CC-PVTZ-DK', 'CC-PVTZ-PP', 'CC-PVTZ', 'CC-PWCV5Z-DK', 'CC-PWCV5Z-PP', 'CC-PWCV5Z', 'CC-PWCVDZ-PP', 'CC-PWCVQZ-DK', 'CC-PWCVQZ-PP', 'CC-PWCVQZ', 'CC-PWCVTZ-DK', 'CC-PWCVTZ-PP', 'CC-PWCVTZ', 'CV5Z', 'CVDZ', 'CVQZ', 'CVTZ', 'D-AUG-CC-PV5Z', 'D-AUG-CC-PV6Z', 'D-AUG-CC-PVDZ', 'D-AUG-CC-PVQZ', 'D-AUG-CC-PVTZ', 'DAV5Z', 'DAV6Z', 'DAVDZ', 'DAVQZ', 'DAVTZ', 'DZP', 'LANL2DZ', 'DZ', 'MBS-ECP1', 'ECP1', 'MBS-ECP2', 'ECP2', 'M1209', 'MIDI-BANG', 'MIDI', 'MINI-SCALED', 'MINI', 'SBKJC', 'STO-3G', 'SVP', 'SV', 'T-AUG-CC-PV5Z', 'T-AUG-CC-PV6Z', 'T-AUG-CC-PVDZ', 'T-AUG-CC-PVQZ', 'T-AUG-CC-PVTZ', 'TAV5Z', 'TAV6Z', 'TAVDZ', 'TAVQZ', 'TAVTZ', 'TZVPPP', 'TZVPP', 'TZVP', 'TZV', 'V5Z\+D', 'V5Z-DK', 'V5Z-PP', 'V5Z', 'V6Z\+D', 'V6Z', 'VDZ\+D', 'VDZ-DK', 'VDZ-PP', 'VDZ', 'VQZ\+D', 'WCVQZ-DK', 'VQZ-DK', 'VQZ-PP', 'VQZ', 'VTZ\+D', 'VTZ-DK', 'VTZ-PP', 'VTZ', 'WCV5Z-DK', 'WCV5Z-PP', 'WCV5Z', 'WCVDZ-PP', 'WCVQZ-PP', 'WCVQZ', 'WCVTZ-DK', 'WCVTZ-PP', 'WCVTZ']

def rawstd2jmol(rawstd_geom, title='Untitled'):
	"""Convert a gaussian standard orientation string to jmol readable string."""
	#print '[debug]mol2jmol:\t', rawstd_geom
	try:
		raw_list = rawstd_geom.splitlines()
		for num_empty in range(raw_list.count('')):
			raw_list.remove('')
		numatom = len(raw_list)
		geom_jmol = '|'+str(numatom) + '|' + title
		act_charge = 0
		for atom in raw_list:
			try:atom_seg = atom.strip().split() 
			#for gaussian:['1', '13', '0', '0.000000', '0.000000', '0.000000']
			#for molpro:['1', 'ZR', '12.00', '0.00000000', '2.784302510', '-0.508718015']
				#	or ['1', 'ZR1', '12.00', '0.00000000', '2.784302510', '-0.508718015']
			except: continue
			else:
				#print '[debug]atom_seg:\t',atom_seg
				if not atom_seg: continue
				try:
					if not atom_seg[1][0].isalpha(): #for gaussian, is not used here
						if atom_seg[1] in PERIODIC_TABLE:
							atom_jmol = '|' + PERIODIC_TABLE[atom_seg[1]] + ' ' + atom_seg[3] + ' ' +atom_seg[4] + ' ' + atom_seg[5]
					else: #for molpro
						try:
							act_ch_atom = int(round(float(atom_seg[2])))
						except: 
							pass
						else:
							act_charge += act_ch_atom
						atom_cap = atom_seg[1]
						#print 'debug.atom_cap:\t',atom_cap
						if len(atom_cap) == 2:
							if atom_cap[1].isalpha():
								atom_norm = atom_cap[0] + atom_cap[1].lower() #Captalize the 1st char of an atom only
							else:
								atom_norm = atom_cap[0]
						if len(atom_cap) > 3:
							atom_cap = atom_cap[:3]
						if len(atom_cap) == 1:
							atom_norm = atom_cap
						if len(atom_cap) == 3:
							if atom_cap[1].isalpha() and atom_cap[2].isalpha():
								atom_norm = atom_cap[0] + atom_cap[1].lower() +atom_cap[2].lower()
							if atom_cap[1].isalpha() and not atom_cap[2].isalpha():
								atom_norm = atom_cap[0] + atom_cap[1].lower()
							if not atom_cap[1].isalpha() and not atom_cap[2].isalpha():
								atom_norm = atom_cap[0]
						if len(atom_cap) == 0:
							print 'Error when parsing geometry.'
							atom_seg = '?'
						atom_jmol = '|' + atom_norm + ' ' + atom_seg[3] + ' ' +atom_seg[4] + ' ' + atom_seg[5]
						#print '[debug]atom_jmol:\t', atom_jmol
				except: continue
				else:
					geom_jmol += atom_jmol
		geom_jmol += '\n'
	except:
		print 'Unexpect error occurs during geometry normalization..'	
		return None
	else:
		return (geom_jmol,act_charge) #act_charge is active nuclear charge in a calculation (all but ecp)
		#geom_jmol is like:
		# '|numatom|title|at1 x y z|at2 x y z|at3 x y z|...|\n'


def MolproInputParse(molin):
	"""molin(Molpro-Input) is the molpro input string in the format of upper-case."""
	#print '[debug]molin:\t',molin 
	lines = molin.splitlines(True)
	length = len(lines)
	newlines = []
	for x in range(length):
		temp_line = (lines.pop().splitlines()[0]).split('!')[0].strip() #remove documentations
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
		if line.count('***,') >= 1:
			#print '[debug]title:\t',line
			Title = line.split('***,',1)[1]
			break

	n2_length = len(newlines2) #get the compact string list
	if n2_length == 0: return 0
	Methods,CalcType,InputButGeom = '','','' #initialize
	input_str = string.join(newlines2,'')
	# Now build a re compiler:
	comp_method_str = '(' + string.join(CARDS_METHOD,'|') + ')*'
		 #string to build the compiler
	comp_calctype_str = '(' + string.join(CARDS_CALCTYPE,'|') + ')*'
	comp_basis_str = '(' + string.join(DEFBAS,'|') + ')*'
	#print '[debug]comp_basis_str:\t', comp_basis_str
	comp_method = re.compile(comp_method_str,re.I)
	comp_calctype = re.compile(comp_calctype_str,re.I)
	comp_basis = re.compile(comp_basis_str,re.I)
	#Get results:
	Methods = string.join((string.join((re.findall(comp_method, input_str)),' ').split()),',') #Methods = 'method1, method2, method3...'
	CalcType = string.join((string.join((re.findall(comp_calctype, input_str)),' ').split()),',')
	input_str2 = re.sub('GEOMETRY\s*=\s*\{[^\}]*\}','GEOM',input_str) #strip all geometry specifications out..
	basis_list = re.findall('BASIS\s*=\s*\{([^\}]*)\}',input_str2) #enter parsing basis set(composite)
	#print '[debug]basis', basis_list
	basis_list2 = re.findall('BASIS\s*=*([^\s]*?);',input_str2) #single basis specification
	#print '[debug]basis2', basis_list2
	basis_str = string.join(basis_list2) + string.join(basis_list)
	#debug_basis = re.findall(comp_basis, basis_str)
	#print '[debug]basis match:\t', debug_basis
	Basis = string.join((string.join((re.findall(comp_basis, basis_str)),' ').split()),',') #Basis = 'bas1, bas2, bas3...'
	if Basis.count('CC-PVDZ') == 0:
		if not Basis:
			Basis = 'CC-PVDZ'
		else:
			Basis += ',CC-PVDZ(?)'
	InputButGeom = re.sub('BASIS\s*=\s*\{[^\}]*\}','BASIS',input_str2) #strip all basis set specifications out..
	result_dict = {}
	if Title: result_dict.update({'Title':Title})
	if Methods: result_dict.update({'Methods':Methods})
	if CalcType: result_dict.update({'CalcType':CalcType})
	if Basis: result_dict.update({'Basis':Basis})
	if InputButGeom: result_dict.update({'InputButGeom':InputButGeom})
	if not result_dict: return 0
	else: return result_dict
		#line_2_append = ''
		#newlines3 = []
		#inblock = 0
		#gain = 0
		####################################################################
		#for z in range(n2_length):
		#	line_pop = n2_length.pop()
		#	if gain == 0:
		#		gain = line_pop.count('{') - line_pop.count('}')
		#		if gain == 0:
		#			newlines3.append([line_pop])
		#		else:
		#			inblock = 1
		#			line_2_append += line_pop
		#	else:
		#		gain += (line_pop.count('{') - line_pop.count('}'))
		#		if gain == 0:
		#			line_2_append += line_pop
		#			newlines3.append([line_2_append])
		#			inblock = 0
		#		else:
		#			line_2_append += line_pop
		#			continue
		#####################################################################
		



class molpro(default.method):
	"""This is the molpro basic method, inheriting from C{method}.
	{default.method} has the line: from Parser import *
	"""
	def ParseInput(self,lines):
		"""Extrat echoed input from molpro output files, then draw data from."""
		try:
			input_sect = std.findsection(lines,'default\simplementation','default\simplementation','Variables\sinitialized',myfilter='capitalize',iflstrip=True)
		except:
			return None
		if not input_sect: return 0 #no match, return!
		input_dict = MolproInputParse(input_sect)
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
			raw_init_geom = std.findsection(linelist,'ATOMIC\s*COORDINATES','^\s*$','^\s*$',bra_skip=1)
		except:
			return None
		#print '[debug]raw_geom=:\n\t', raw_init_geom 
		if not raw_init_geom: return None
		# without joinlines, only match once. return value is a string. but skip the 1st match of '^\n$'

		# raw_init_geom is like: """
		#    1         13             0        0.000000    0.000000    0.000000\n
		#    2          8             0        0.000000    0.000000    1.842599\n
		#    3          8             0        1.675945    0.000000   -0.515496\n
		#    4          8             0       -1.127404   -1.085048   -0.773904\n
		#"""
		geom_jmol = rawstd2jmol(raw_init_geom, 'Initial Geometry')
		if not geom_jmol: return None
		else:
			if self.result.has_key('InitGeom'):
				self.result['InitGeom'] += (LINE_SEP + geom_jmol[0])
			else: self.result['InitGeom'] = geom_jmol[0]
			#Now, do formula:
			atom_list = []
			atom_list2 = (geom_jmol[0].split('|'))[3:] #['at1 x y z', 'at2 x y z', 'at3 x y z', '...']
			for num_empty in range(atom_list2.count('')):
				atom_list2.remove('')
			#print '[debug]atom_list2:\t',atom_list2
			for atom_a in atom_list2:
				try:
					atom = atom_a.strip().split()[0]
					#print '[debug]atom2:\t',atom
				except:
					pass
				else:
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
				ch_line = std.findline_2(linelist,'charge_line','PROGRAM\s*\*\s*RHF-SCF','(NUMBER\sOF\sELECTRONS:.*)') #that is 1st match only
			except:
				return None
			ch_list = []
			if ch_line:
				ch_line = ch_line
				#print '[debug]ch_line:\t',ch_line
				try:
					ch_list = (re.findall('NUMBER\s*OF\s*ELECTRONS:\s*([0-9]*)\+?\s*([0-9]*)\-?\s*(?:SPACE\s*SYMMETRY\s*=\s*)*([0-9]*)(?:\s*SPIN\s*SYMMETRY\s*=\s*)*([^\s]*)',ch_line['charge_line'].upper(),re.I))[0]
				except:
					return None
				if not ch_list:
					print 'Charge and Multiplicity are yet unkown..'
					self.result['Formula'] = formula
					return 0
				#print '[debug]ch_list:\t', ch_list
				ch_list = list(ch_list) # tuple --> list #use upper-case string,findall returns like [('12', '12', '4', 'Singlet')]
			#print '[debug] ch_list', ch_list
			for num_empty in range(ch_list.count('')):
				ch_list.remove('')
			ch_len = len(ch_list)
			total_charge, multi = '', '?' # default may be 1, but not for sure here, so leave a ? first
			if ch_len >= 2:
				ele_charge = int(ch_list[0]) + int(ch_list[1])
				if ch_len == 2:
					multi = 1
				if ch_len == 3:
					if ch_list[2].isdigit():
						multi = 1
					else:
						if ch_list[2] == 'SINGLET': multi = 1
						if ch_list[2] == 'TRIPLET': multi = 3
						if ch_list[2] == 'QUINTET': multi = 5
						if ch_list[2] == 'SEPTET': multi = 7
						if ch_list[2] == 'NONET': multi = 9
						if ch_list[2] == 'DOUBLET': multi = 2
						if ch_list[2] == 'QUARTET': multi = 4
						if ch_list[2] == 'SEXTET': multi = 6
						if ch_list[2] == 'OCTET': multi = 8
						if ch_list[2] == 'DECTET': multi = 10
				if ch_len == 4:
					if ch_list[3] == 'SINGLET': multi = 1
                                        if ch_list[3] == 'TRIPLET': multi = 3
                                        if ch_list[3] == 'QUINTET': multi = 5
                                        if ch_list[3] == 'SEPTET': multi = 7
                                        if ch_list[3] == 'NONET': multi = 9
                                        if ch_list[3] == 'DOUBLET': multi = 2
                                        if ch_list[3] == 'QUARTET': multi = 4
                                        if ch_list[3] == 'SEXTET': multi = 6
                                        if ch_list[3] == 'OCTET': multi = 8
                                        if ch_list[3] == 'DECTET': multi = 10
			else:
				print 'Problem with charge and multiplicity..', ch_list
				#ele_charge = 0 #for debug
				total_charge = '?'
				multi = '?'
			nuc_charge = geom_jmol[1]
			#print '[debug]charges: nuc, ele:\t', nuc_charge, ele_charge
			if total_charge != '?':
				total_charge = nuc_charge - ele_charge
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
		"""Take the final geometry from molpro output file."""
		try:
			raw_init_geom = std.findsection(linelist,'ATOMIC\s*COORDINATES','^\s*$','^\s*$',bra_skip=1,last_match=True)
		except:
			return None
		# without joinlines, only match once. return value is a string. but skip the 1st match of '^\n$', only pick the last match
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
		try:
			lines_str_dict = std.findline_s(linelist,'energy_str','^\s!(.*ENERGY\s*.*)',fullmatch=True)
		except:
			return None
		if not lines_str_dict: return 0
		lines_str = lines_str_dict['energy_str']
		if not lines_str: return 0
		energy_str_list = lines_str.split(RECORD_SEP)
		for x in range(energy_str_list.count('')):
			energy_str_list.remove('')
		if not energy_str_list: return 0
		#print '[debug]energy_str_list:\t',energy_str_list
		for energy_str in energy_str_list[:-1]:
			energy_str_new = string.join(energy_str.split(),' ') + RECORD_SEP
			if self.result.has_key('OtherInfo'):
				self.result['OtherInfo'] += energy_str_new
			else: self.result['OtherInfo'] = energy_str_new
		try:
			if len(energy_str_list[-1:][0].split('ENERGY',1)) == 2:
				#print "upper"
				trash, fin_energy = energy_str_list[-1:][0].split('ENERGY',1)
			elif len(energy_str_list[-1:][0].split('energy',1)) == 2:
				#print "lower"
				trash, fin_energy = energy_str_list[-1:][0].split('energy',1)
			else:
				trash, fin_energy = energy_str_list[-1:][0].split('Energy',1)
		except:
			print 'energy in molpro06.py, trash, fin_energy line error'
			print energy_str_list
			#print energy_str_list[-1:][0].split('ENERGY',1)
			#print energy_str_list[-1:][0].split('energy',1)
		#print '[debug]fin_energy:\t', fin_energy
		try:
			f_energy = fin_energy.strip()
		except:
			if self.result.has_key('OtherInfo'):
				self.result['OtherInfo'] += energy_str_list[-1:][0].strip()
			else: self.result['OtherInfo'] = energy_str_list[-1:][0].strip()
		else:
			self.result['Energy'] = f_energy
			fin_energy_str = string.join(energy_str_list[-1:][0].split())
			if self.result.has_key('OtherInfo'):
				self.result['OtherInfo'] += fin_energy_str
			else: self.result['OtherInfo'] = fin_energy_str
		return 1
					

	def status(self,linelist):
		if self.keys.has_key('JobStatus'):
			self.executeone(linelist,'JobStatus') ########### defined in parser loader by user
		else:
			try:
				status_dict = std.findkey_return_str(linelist,'return_value','Variable\smemory\sreleased','CalcDone','Unfinished',fullmatch=True) 
			except:
				return None
			if not status_dict or not status_dict.values()[0]:
				return 0
			else:
				self.result['JobStatus'] = status_dict['return_value']

	def parse(self,metafile):
		from copy import deepcopy				
		lines = deepcopy(metafile.lines)
		lines = self.parse_default(lines) #this is initialization
			
		self.init_geom(lines)
                try:
                        P_index = lines.index(PAIRSEPARATOR)
                except ValueError: pass
                else:
                        lines = lines[:P_index]
		self.final_geom(lines)
		self.energy(lines)
		self.executeall(lines)
		#print self.result
		return self.result
