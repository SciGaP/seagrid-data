#!/usr/bin/env python
# ../Method/Interpreter
# iterprete plain test into parser setting dict

# input is linelist, output is (parser_name, parser_setting_dict)

import re
#BRACKET = '\{[\'\"](?P<para>.*)[\'\"]\}'
#SQUARE = '\[(?P<name>.*?)\]\s*(?P<value>[^\{]*)\s*(\{[\'\"](?P<para>.*?)[\'\"]\})(?P=name)*.*'
# SQUARE not test yet

def interprete(linelist, parser_name):
	"""Interprete the setting dictionary for a parser from a list of string lines.
	The setting dictionary will be like the format:
	setting_dict________'basic'_________infoname__________'value'
			|				|_____'para'
			| 
			|___'keys'__________key_name__________'func'
							|_____'para'
	"""
	setting_dict = {'basic':{},'keys':{}}
	new_list = []
	length = len(linelist)
	for x in range(length): 
		temp_line = (linelist.pop().splitlines()[0]).split('#')[0] #remove documentations
		if not temp_line.isspace() and len(temp_line):
			new_list.append(temp_line)
	n_length = len(new_list)
	for y in range(n_length):
		cur_line = new_list.pop()
		cur_line_keys = cur_line.split()
		if cur_line_keys[0][0] == '[': #cur_line is about basic info
			info_name = cur_line_keys[0][1:-1]
			para_list = []
			info_value = ''
			if len(cur_line_keys) == 2: # only [info_name] value
				if not cur_line_keys[1].startswith('{'):
					info_value = cur_line_keys[1]
				else:
					for y in range(1,len(cur_line_keys)):
                                        	if cur_line_keys[y][0:2] == "{'":
                                                	para_list.append(cur_line_keys[y][2:-2])
                                        	if cur_line_keys[y][0:2] == '{\"':
                                                	temp_para = eval(cur_line_keys[y][2:-2])
                                                	para_list.append(temp_para)

			if len(cur_line_keys) > 2:
				if not cur_line_keys[1].startswith('{'):
                                        info_value = cur_line_keys[1]
					for y in range(2,len(cur_line_keys)):
						if cur_line_keys[y][0:2] == "{'":
							para_list.append(cur_line_keys[y][2:-2])
						if cur_line_keys[y][0:2] == '{\"':
							temp_para = eval(cur_line_keys[y][2:-2])
							para_list.append(temp_para)
				else:
					for y in range(1,len(cur_line_keys)):
                                                if cur_line_keys[y][0:2] == "{'":
                                                        para_list.append(cur_line_keys[y][2:-2])
                                                if cur_line_keys[y][0:2] == '{\"':
                                                        temp_para = eval(cur_line_keys[y][2:-2])
                                                        para_list.append(temp_para)

			if info_value.startswith('DICT:'):
				try:
					dict_final = {}
					dict_potential = info_value[6:0]
					for item in dict_potential.split('|'):
						dict_final.update([item.split(',',1)])
				except: pass
				else: info_value = dict_final #read a dictionary 
			temp_dict = {info_name:{'value':info_value,'para':para_list}} #empty value and para_list allowed!
			setting_dict['basic'].update(temp_dict)
		if cur_line_keys[0][0] == '<': #cur_line is about a function
			if len(cur_line_keys) == 1: #empty func_name not allowed!
				continue
			para_list = []
			if len(cur_line_keys) > 1:
				key_name = cur_line_keys[0][1:-1]
				func_name = cur_line_keys[1]
			if len(cur_line_keys) > 2:
				for y in range(2,len(cur_line_keys)):
					if cur_line_keys[y][0:2] == "{'":
                                                para_list.append(cur_line_keys[y][2:-2])
                                        if cur_line_keys[y][0:2] == '{\"':
                                                temp_para = eval(cur_line_keys[y][2:-2])
                                                para_list.append(temp_para)
			temp_dict = {key_name:{'func':func_name,'para':para_list}}
			setting_dict['keys'].update(temp_dict)
	#print '[Debug] PName, Dict:', parser_name, '----', setting_dict
	return [parser_name,setting_dict]
				

def interprete_p(path):
	"""Interprete the setting dictionary for a parser from a given full path."""
	from os.path import exists
	if not exists(path):
		return None
	else: 
		fd = open(path,'r')
		linelist = fd.readlines()
		return interprete(linelist,path)

def interprete_dir(dirpath):
	"""Interprete all the setting dictionary in a given diretory path."""
	from os import listdir
	from os.path import abspath, join
	try:
		dirlist = listdir(dirpath)
	except OSError:
		print 'Directory not exist..'
		return None
	else:
		#print 'debug,dir_list', dirlist
		setting_list = []  #[[name1,setting1],[name2,setting2],...]
		pa_list = [join(dirpath,parser) for parser in dirlist if parser.endswith('.pa')]
		#print 'debug,par_list', pa_list
		for pa in pa_list:
			#print 'debug, par', pa
			pa_setting = interprete_p(pa)
			#print 'debug, pa_setting', pa_setting
			if pa_setting:
				setting_list.append(pa_setting)
		if setting_list: return dict(setting_list) # return a dictionary value
		else: return None
		
