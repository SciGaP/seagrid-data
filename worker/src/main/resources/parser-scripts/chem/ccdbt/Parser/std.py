#!/bin/usr/env python

#.../Method/Parser/std.py
# a built-in, standard collection of parsing algorithms.
# USUALLY functions in this module take string list as their 1st parameter,
# take keyname in the dictionary as 2nd parameter, if the return value is a dictionary.
# For the database usage, the return value is recomanded to be a dictionary.
# Functions here are neither fastest nor coolest, you are welcome to create your own module in the same folder.
# To use these functions or your own ones, go to ../Method/MethodLibrary, to create a parser by write a '.pa' file.
# For further instruction, please see /Method/MethodLibrary/template.


import re
from string import join
from copy import deepcopy
from filter1 import * #i.e. g03tojmol_geom,..., they are imported to the namespace, so funtions can do more complicated jobs
from filter_user import * # like std2, but user could add functions to std_user.

RECORD_SEP = '; '
LINE_SEP = '\n'

###################
## Boolean Funcs ##
###################
def findkey(linelist,pattern,case_sens=False,from_backwards=False):
	"""Do a pattern match, success return 1, else return 0."""
	line_list = deepcopy(linelist)
	if not case_sens:
                case_flag = re.I         
	else: case_flag = 0
	if not from_backwards: # because pop() itself is reverse
		line_list.reverse()
	try:
		try: line = line_list.pop()
		except IndexError: return 0
		while line != None:
			match = re.search(pattern,line,case_flag)
			if match:
				return 1
			else:
				try: line = line_list.pop()
				except IndexError: line = None
		return 0
	except:
		print 'Unexpected error occurs during matching a key..'
		return 0

def findkeys(linelist,patternlist,case_sens=False,from_backwards=False):
	"""Verify all patterns in a list, if one of the match is False match, return 0."""
	for pattern in patternlist:
		if not findkey(linelist,pattern,case_sens,from_backwards):
			return 0
	return 1

def findkey_return_str(linelist,keyname,pattern,true_str,false_str,fullmatch=False,from_backwards=False,case_sens=False):
	"""Given keyname and supposed return value, do a pattern match inline, can also do a multiple times match."""
	line_list = deepcopy(linelist)
	result_dict = {}
	if not case_sens:
		case_flag = re.I
	else: case_flag = 0
	if not from_backwards:
		line_list.reverse()
	try:
		try: line = line_list.pop()
		except IndexError:
			if not result_dict:
				return {keyname:false_str}
			else:
				return result_dict
		else:
			while line != None:
				match = re.search(pattern,line,case_flag)
				if match:
					if not fullmatch: #return when 1st match
						return {keyname:true_str}
					else:
						if result_dict.has_key(keyname):
							result_dict[keyname] += (RECORD_SEP + true_str)
						else:
							result_dict[keyname] = true_str
				else:
					pass
				try: line = line_list.pop()
				except IndexError: line = None
			if not result_dict: #nothing to pop now.. 
                                return {keyname:false_str}
                        else:
                                return result_dict
	except:
		print 'Unexpected error occurs during matching a key..'
		return None
	


			
#######################
## Match Within Line ##
#######################
def findline(linelist,keyname,pattern_1,pattern_o,fullmatch=False,case_sens=False,line_diff=0,pattern_inline='',if_cross_line=False,from_backwards=False,last_match=False,myfilter=None): 
	"""
	'info' referred to the information that need be extracted by the function
	Usage:	1)find the wanted line by {pattern_1} + line_diff.
       		2)extract info by {pattern_o}.
		3)return a dictionary {keyname:matched pattern}
	line_diff=0 means wanted info is in the same line with pattern_1; 
	line_diff=-n means n lines above, vice versus.
	pattern_inline is the inline keyword to verify the line where the info is.
	after_pattern_inline: if wanted info is after {pattern_inline} or not.
	if_cross_line: whether search in multiple lines or not.
	return value is always a dictionary or None; (after ver 3.4)
	object_info is the place to store search result, it is how the inner talk with the outer(obslete);
	it is an attribute of object{file_pair} or {combo}(obslete).
	value assign is never accomplished by this function, but by outer roop object who evoke this function.
	'last_match': if True, only keep the last match.
	!!! pattern_1 and pattern_inline mustn't be cross-lines in this case, if any of them are, use f{findline_x}!!!
	"""
	#Case-sensitive?
	if not case_sens:
		case_flag = re.I
	else: case_flag = 0
	object_info = ''	
	#For both forwards and backwards, use different methods:
	if not from_backwards:
		line_list = deepcopy(linelist)
		length = len(line_list)
	
		object_line = ''
		#debug_info = ''
		for counter in xrange(length):
			#if object_info:#debug
			#	if object_info != debug_info:
			#		print 'stepN', '\t', object_info
			#		debug_info = object_info
			if re.search(pattern_1, line_list[counter], case_flag):
				if 0<= (counter + line_diff) <= length: #add line diff to aim the object line
					object_line = line_list[(counter + line_diff)]
					if if_cross_line and 1 < (counter + line_diff) < length:
						object_line = join(line_list[(counter + line_diff - 1)].splitlines()[0], object_line, line_list[(counter + line_diff + 1)].splitlines()[0])
					if not fullmatch:
						del line_list
						if len(object_line) == 0: return None
						try:
							if re.search(pattern_inline, object_line, case_flag):
								object_find = re.findall(pattern_o, object_line, case_flag)[0]
								if myfilter and myfilter in globals():
									try:
										object_find2 = eval(myfilter)(object_find)
									except: pass
									else: object_find = object_find2	
								object_info += object_find
								#take the first match, modify here to take others
								if last_match: #only keep the last match!!!
									object_info = re.findall(pattern_o, object_line, case_flag)[0]
								if len(object_info) == 0: raise KeyError
								else: return {keyname:object_info}
							else: raise ValueError
						except KeyError:
							print """Didn't match when overchecking with 2rd pattern(inline).."""
							return None
						except ValueError:
							print """Value not found, check if object match pattern is right.."""
							return None
						except: 
							print 'Other Error during parsing the file via {%s}' % (__name__)
							return None
					else:
						try:
                                                	if re.search(pattern_inline, object_line, case_flag):
								object_find = re.findall(pattern_o, object_line, case_flag)[0]
								if myfilter and myfilter in globals():
									try:
										object_find2 = eval(myfilter)(object_find)
									except:
										pass
										#raise #debug 
									else: 
										object_find = object_find2
										#print '[debug]findline','\t',object_find
                                                        	object_info += object_find
								object_info += RECORD_SEP
								if last_match: #only keep the last match!!!
									object_info = object_find 
							else: pass
						except: pass
				else:
					print 'Too large or too small index while adding line_diff'
					continue
		if len(object_info) == 0:
			return None	
		else: 
			#print {keyname:object_info} #debug
			return {keyname:object_info}
	# now the backwards case:	
	else:
		line_list = deepcopy(linelist)
		line_list.reverse() #reverse the list
		length = len(line_list)
		object_line = ''
		for counter in xrange(length):
			if re.search(pattern_1, line_list[counter], case_flag):
				if 0<= (counter - line_diff) <= length: #due to the reverse operation to the list, here is a '-'
					object_line = line_list[(counter - line_diff)]
					if if_cross_line and 1 < (counter - line_diff) < length:
						object_line = join(line_list[(counter - line_diff + 1)].splitlines()[0], object_line, 
								line_list[(counter - line_diff - 1)].splitlines()[0])
					if not fullmatch:
                                                del line_list
                                                if len(object_line) == 0: return None
                                                try:
                                                        if re.search(pattern_inline, object_line, case_flag):
                                                                object_info += re.findall(pattern_o, object_line, case_flag)[0]
                                                                #take the first match, modify here to take others
								if last_match: #only keep the last match!!!
									object_info = re.findall(pattern_o, object_line, case_flag)[0]
                                                                if len(object_info) == 0: raise KeyError
                                                                else: return {keyname:object_info}
                                                        else: raise ValueError
                                                except KeyError:
                                                        print """Didn't match when overchecking with 2rd pattern(inline).."""
							return None
                                                except ValueError:
                                                        print """Value not found, check if object match pattern is right.."""
							return None
                                                except: 
							print 'Other Error during parsing the file via {%s}' % (__name__)
							return None
                                        else:
                                                try:
                                                        if re.search(pattern_inline, object_line, case_flag):
                                 	                        object_info += re.findall(pattern_o, object_line, case_flag)[0]
								object_info += RECORD_SEP
								if last_match: #only keep the last match!!!
									object_info = re.findall(pattern_o, object_line, case_flag)[0]
                                                        else: pass
                                                except: pass
				else:
					print 'Too large or too small index while adding line_diff'
					continue
		if len(object_info) == 0: #that's keep object_info the initial value, but make sure the initial value of object_info is blank
			return None
		else: 
			#print {keyname:object_info} #debug
			return {keyname:object_info}
		

def findline_s(linelist,keyname,pattern,fullmatch=False,case_sens=False,from_backwards=False,last_match=False,myfilter=None):
	"""Simpler way to find a line, only given a pattern. Then extract the matched pattern."""
	return findline(linelist,keyname,pattern,pattern,fullmatch,case_sens,0,'',False,from_backwards,last_match,myfilter)

def findline_2(linelist,keyname,pattern_loc,pattern,fullmatch=False,case_sens=False,from_backwards=False,last_match=False,myfilter=None):
	"""Simpler way to find a line using two patterns, then extract the matched pattern."""
	if not case_sens:
                case_flag = re.I
        else: case_flag = 0
	line_list = deepcopy(linelist)
	result_dict = {}
	if from_backwards:
		pass
	else:
		line_list.reverse()
	length = len(line_list)
	line_skip = 0
	for index in xrange(length):
		if line_skip:
			line_skip -= 1
			continue
		cur_line = line_list.pop()
		if not re.search(pattern_loc, cur_line, case_flag): # does not match pattern_loc
			continue
		else:
			while index < (length - 1):
				if not re.search(pattern, cur_line, case_flag):
					cur_line = line_list.pop()
					line_skip += 1
					continue
				else: # find pattern
					object_info = re.findall(pattern, cur_line, case_flag)
					if len(object_info) == 0:
						continue
					o_info = object_info[0]

					if myfilter and myfilter in globals():
						try:
							o_info_2 = eval(myfilter)(o_info)
						except: pass
						else:
							o_info = o_info_2
							
					if not fullmatch:
						return {keyname:o_info}
					else:
						if not last_match:
							keyvalue = o_info + RECORD_SEP
							if not result_dict.has_key(keyname):
								result_dict[keyname] = keyvalue
								break
							else:
								result_dict[keyname] += keyvalue
								break #goto next match
						else:#only last_match:
							keyvalue = o_info
							result_dict[keyname] = keyvalue
							break
	if result_dict.has_key(keyname): 
		return result_dict
	else: return None


##############################
## Match A Section Of Lines ##
##############################

	
def findsection(linelist,pattern_loc,pattern_bra,pattern_cket,fullmatch=False,joinlines=False,case_sens=False,bra_include=False,cket_include=False,from_backwards=False,bra_skip=0,cket_skip=0,last_match=False,myfilter=None,iflstrip=False):
	"""
	Extract a section.
	'pattern_bra' & 'pattern_cket' is a pair of pattern_bracket for us to extract things in between.
	'pattern_loc' is the pattern in line before pattern_bra, or in the line after pattern_cket in the 'reverse match'.
	'joinlines': whether join matched lines.
	'case_sens': case-sensitive or not.
	'bra_include': whether return section or joined lines contain 'bra' pattern.
	'cket_include': ditto.
	'from_backwards': reverse match or not.
	'bra_skip': times to skip 'bra' pattern after 'patter_loc' has been matched.
	'cket_skip': ditto.
	"""
        if not case_sens:
                case_flag = re.I
        else: case_flag = 0

	object_info = ''
	backup_linelist = deepcopy(linelist)
	if from_backwards: # pop is from backwards
		pass
	else: linelist.reverse() # default is not from backwards

	new_list = []
	try: cur_line = linelist.pop()
	except IndexError:
		if joinlines:
			if from_backwards: new_list.reverse()
			linelist.extend(new_list)
		else:
			linelist.extend(backup_linelist)
		if not myfilter:
			return object_info
		else:
			try:
				ob_myfilter = eval(myfilter)(object_info)
			except:
				return object_info
			else:
				return ob_myfilter
	else:
		if iflstrip:#move leading whitespaces
			cur_line = cur_line.lstrip()
		while cur_line != None:
			if re.search(pattern_loc, cur_line, case_flag): # into section matching
				bra_skip_count = bra_skip
				cket_skip_count = cket_skip
				while True:  #while not find pattern_bra
					sect_start = re.search(pattern_bra,cur_line,case_flag)
					if not sect_start:
						new_list.append(cur_line)
						try: cur_line = linelist.pop()
						except IndexError:
							if joinlines:
								if from_backwards: new_list.reverse()
								linelist.extend(new_list)
							else:
								linelist.extend(backup_linelist)
							if not myfilter:
								return object_info
							else:
								try:
									ob_myfilter = eval(myfilter)(object_info)
								except:
									return object_info
								else:
					                                return ob_myfilter
						else:
							if iflstrip: #move leading whitespaces
								cur_line = cur_line.lstrip()

					else: #find 'bra'
						if bra_skip_count > 0: #if skip_count > 0, skip matched pattern this time
							bra_skip_count -= 1
							try: cur_line = linelist.pop() #pop a new line
                                                        except IndexError:
                                                                if joinlines:
                                                                        if from_backwards: new_list.reverse()
                                                                        linelist.extend(new_list)
                                                                else:
                                                                        linelist.extend(backup_linelist)
								if not myfilter:
                                                                	return object_info
								else:
									try:
                                                                        	ob_myfilter = eval(myfilter)(object_info)
                                                                	except:
                                                                        	return object_info
                                                                	else:
                                                                        	return ob_myfilter
							else:
								if iflstrip: #move leading whitespaces
									cur_line = cur_line.lstrip()

							continue #pop and continue

						if bra_include:
							if joinlines:
								object_info += cur_line.splitlines()[0] #remove '\n'
								if last_match: #only keep the last match
									object_info = cur_line.splitlines()[0] 
							else:
								object_info += cur_line #keep '\n'
								if last_match: #only keep the last match
									object_info = cur_line
						else: #bra not included
							if last_match:
								object_info = ''
						if joinlines:
							a_section = cur_line.splitlines()[0] #remove '\n'
						else: a_section = cur_line #keep '\n' #start of a section

						while True:  #while not find pattern_cket yet
							try: cur_line = linelist.pop() #pop a new line
							except IndexError:
								if joinlines:
                               	                                        if from_backwards: new_list.reverse()
                                       	                                linelist.extend(new_list)
                                       	                        else:
                                               	                        linelist.extend(backup_linelist)
								if not myfilter:
									return object_info

								else:
                                                                        try:
                                                                                ob_myfilter = eval(myfilter)(object_info)
                                                                        except:
                                                                                return object_info
                                                                        else:
                                                                                return ob_myfilter
							else:
								if iflstrip: #move leading whitespaces
									cur_line = cur_line.lstrip()



							sect_end = re.search(pattern_cket,cur_line,case_flag)
							if not sect_end:
								if joinlines:
									object_info += cur_line.splitlines()[0]
									a_section += cur_line.splitlines()[0]
								else:
									object_info += cur_line
									a_section += cur_line
							else: #find 'cket'
								if cket_skip_count: #if skip_count > 0, skip matched pattern this time
									cket_skip_count -= 1
									try: cur_line = linelist.pop() #pop a new line
		                                                        except IndexError:
                		                                                if joinlines:
                                		                                        if from_backwards: new_list.reverse()
                                                		                        linelist.extend(new_list)
                                                                		else:
                                                                        		linelist.extend(backup_linelist)
										if not myfilter:
                                                                			return object_info
										else:
        		                                                                try:
                        		                                                        ob_myfilter = eval(myfilter)(object_info)
	                                	                                        except:
                                                		                                return object_info
                                                                		        else:
                                                                                		return ob_myfilter
									else:
										if iflstrip: #move leading whitespaces
											cur_line = cur_line.lstrip()

									continue
								if cket_include:
									a_section += cur_line
                        	                                        new_list.append(a_section)
			
									object_info += cur_line
									if not fullmatch: #return after 1st match
										if joinlines:
											try:
												while True:
													new_list.append(linelist.pop())
											except: pass #pop all elements of linelist to new_list
											if from_backwards: new_list.reverse()
											linelist.extend(new_list)
											if not myfilter:
												return object_info
											else:
        	                                                                                try:
	                                                                                                ob_myfilter = eval(myfilter)(object_info)
													if not ob_myfilter:
														raise
                	                                                                        except:
                        	                                                                        return object_info
                                	                                                        else:
                                        	                                                        return ob_myfilter

										else:
											object_info += LINE_SEP #records separated by '\n\n', if not join lines
											try:
												while True:
													linelist.pop()
											except: pass
											linelist.extend(backup_linelist)
											if not myfilter:
												return object_info
											else:
												try:
                                	                                                                ob_myfilter = eval(myfilter)(object_info)             
	                   	                                                        	except:
                                                                                                        return object_info
                                                                                                else:
                                                                                                        return ob_myfilter

								else: #cket_not_include
									if joinlines: a_section += LINE_SEP #end a section
									new_list.append(a_section)
									new_list.append(cur_line)
									if not fullmatch: #return after 1st match
										if joinlines:	
											object_info += LINE_SEP
											try:
                                                                                                while True:
                                                                                                        new_list.append(linelist.pop())
                                                                                        except: pass #pop all elements of linelist to new_list
											if from_backwards: new_list.reverse()
                                                                                        linelist.extend(new_list)
                                                                                        if not myfilter:
												return object_info
											else:
                                                                                                try:
                                                                                                        ob_myfilter = eval(myfilter)(object_info)
													if not ob_myfilter:
														raise	
												except:
                                                                                                        return object_info
                                                                                                else:
                                                                                                        return ob_myfilter
	
										else:
											object_info += LINE_SEP  #records separated by '\n\n', if not join lines
											try:
                                                                                                while True:
                                                                                                        linelist.pop()
                                                                                        except: pass
                                                                                        linelist.extend(backup_linelist)
											if not myfilter:		
                                                                                        	return object_info
											else:
												try:	
													ob_myfilter = eval(myfilter)(object_info)
												except:
													return object_info
												else:
													return ob_myfilter
									else:
										object_info += LINE_SEP

								break

						try: cur_line = linelist.pop()
                                                except IndexError:
                   	                        	if joinlines:
                        	                        	if from_backwards: new_list.reverse()
                                	                	linelist.extend(new_list)
                                                	else:
                                        	        	linelist.extend(backup_linelist)
							if not myfilter:
                                                		return object_info
							else:
								try:
									ob_myfilter = eval(myfilter)(object_info)
								except:
									return object_info
								else:
									return ob_myfilter
						else:
							if iflstrip: #move leading whitespaces
								cur_line = cur_line.lstrip()

						break  #done a section extract or join
			else: #out of section
				new_list.append(cur_line) #append current line, then pop a newline
				try: cur_line = linelist.pop()
		                except IndexError: #most case, it is the end of the lines, that means out of range
		                        if joinlines:
		                                if from_backwards: new_list.reverse()
		                                linelist.extend(new_list)
		                        else:
		                                linelist.extend(backup_linelist)
					if not myfilter:
						return object_info
					else:
                                        	try:
							#print myfilter,eval(myfilter)
                                                	ob_myfilter = (eval(myfilter,globals()))(object_info)
							if not ob_myfilter:
								raise
                                                except:
                                                	return object_info
                                                else:
                                                	return ob_myfilter
				else:
					if iflstrip: #move leading whitespaces
						cur_line = cur_line.lstrip()
	
	


def findsection_dict(linelist,keyname,pattern_loc,pattern_bra,pattern_cket,fullmatch=False,joinlines=False,case_sens=False,bra_include=False,cket_include=False,from_backwards=False,bra_skip=0,cket_skip=0,last_match=False,myfilter=None,iflstrip=False): # 16 parameters;
	"""Similar as findsection, except that it return a dictionary instead of a string."""
	object_info = findsection(linelist,pattern_loc,pattern_bra,pattern_cket,fullmatch,joinlines,case_sens,bra_include,cket_include,from_backwards,bra_skip,cket_skip,last_match,myfilter,iflstrip)
	if object_info != None and len(object_info) > 0:
		return {keyname:object_info}
	else: return None

#####################################
## Multiple Patten Match In A Line ##
#####################################

def findInline(linelist,keyname_pattern_dict,pattern_1,fullmatch=False,case_sens=False,line_diff=0,pattern_inline='',if_cross_line=False,from_backwards=False):
        """
        'info' referred to the information that need be extracted by the function
        Usage:  1)find the wanted line by {pattern_1} + line_diff.
                2)extract info by {pattern_o} in {keyname_pattern_dict}.
                3)return a dictionary {keyname1:matched pattern1, keyname2:matched pattern2,...}
        line_diff=0 means wanted info is in the same line with pattern_1;
        line_diff=-n means n lines above, vice versus.
        pattern_inline is the inline keyword to verify the line where the info is.
        after_pattern_inline: if wanted info is after {pattern_inline} or not.
        if_cross_line: whether search in multiple lines or not.
        return value is always a dictionary or None; (after ver 3.4)
        object_info is the place to store search result, it is how the inner talk with the outer(obslete);
        it is an attribute of object{file_pair} or {combo}(obslete).
        value assign is never accomplished by this function, but by outer roop object who evoke this function.

        !!! pattern_1 and pattern_inline mustn't be cross-lines!!!
        """
        #Case-sensitive?
	result_dict = {}
	
        if not case_sens:
                case_flag = re.I
        else: case_flag = 0
        object_info = ''
        #For both forwards and backwards, use different methods:
        if not from_backwards:
                line_list = deepcopy(linelist)
		print len(linelist)
                length = len(line_list)

		print 'line_list',length
                object_line = ''
                for counter in xrange(length):
                        if re.search(pattern_1, line_list[counter], case_flag):
                                if 0<= (counter + line_diff) <= length: #add line diff to aim the object line
                                        object_line = line_list[(counter + line_diff)]
                                        if if_cross_line and 1 < (counter + line_diff) < length:
                                                object_line = join(line_list[(counter + line_diff - 1)].splitlines()[0], object_line, line_list[(counter + line_diff + 1)].splitlines()[0])
					
					for (keyname,pattern_o) in keyname_pattern_dict.items():
                                        	if not fullmatch:
                                                	if object_line == None or len(object_line) == 0: return None
                                                	try:
                                                        	if re.search(pattern_inline, object_line, case_flag):
									found_pattern_list = re.findall(pattern_o, object_line, case_flag)
									if len(found_pattern_list) == 0:
										raise ValueError
									else: found_pattern = found_pattern_list[0]
                                                                	#take the first match, modify here to take others
                                                                	if found_pattern == None or len(found_pattern) == 0: raise KeyError
                                                                	else: 
										result_dict.update({keyname:found_pattern})
                                                        	else: raise ValueError
                                                	except KeyError:
                                                        	print """Didn't match when overchecking with 2rd pattern(inline).."""
                                                        	continue
                                                	except ValueError:
                                                        	print """Value not found, check if object match pattern is right.."""
                                                        	continue
                                                	except:
                                                        	print 'Other Error during parsing the file via {%s}' % (__name__)
								#raise #debug
                                                        	continue
                                        	else:
                                                	try:
                                                        	if re.search(pattern_inline, object_line, case_flag):
                                                                	found_pattern = re.findall(pattern_o, object_line, case_flag)[0]
									if found_pattern == None or len(found_pattern) == 0:
										continue
									else: found_pattern += RECORD_SEP
									if result_dict.has_key(keyname):
										result_dict[keyname] += found_pattern
									else:
										result_dict[keyname] = found_pattern
                                                        	else: pass
                                                	except: pass
                                else:
                                        print 'Too large or too small index while adding line_diff'
                                        continue
                if len(result_dict) == 0:
                        return None
                else: return result_dict
        # now the backwards case:
        else:
                line_list = deepcopy(linelist)
                line_list.reverse() #reverse the list
                length = len(line_list)
                object_line = ''
                for counter in xrange(length):
                        if re.search(pattern_1, line_list[counter], case_flag):
                                if 0<= (counter - line_diff) <= length: #due to the reverse operation to the list, here is a '-'
                                        object_line = line_list[(counter - line_diff)]
                                        if if_cross_line and 1 < (counter - line_diff) < length:
                                                object_line = join(line_list[(counter - line_diff + 1)].splitlines()[0], object_line,
                                                                line_list[(counter - line_diff - 1)].splitlines()[0])
					for (keyname,pattern_o) in keyname_pattern_dict.items():
                                        	if not fullmatch:
                                                	if len(object_line) == 0: return None
                                                	try:
                                                        	if re.search(pattern_inline, object_line, case_flag):
									found_pattern_list == re.findall(pattern_o, object_line, case_flag)
									if len(found_pattern_list) == 0:
										raise ValueError
									else:
										found_pattern = found_pattern_list[0]
                                                                	#take the first match, modify here to take others
                                                                	if found_pattern == None or len(found_pattern) == 0: raise KeyError
                                                                	else: result_dict.update({keyname:found_pattern}) 
                                                        	else: raise ValueError
                                                	except KeyError:
                                                        	print """Didn't match when overchecking with 2rd pattern(inline).."""
                                                        	continue	
                                                	except ValueError:
                                                        	print """Value not found, check if object match pattern is right.."""
                                                        	continue
                                                	except:
                                                        	print 'Other Error during parsing the file via {%s}' % (__name__)
                                                        	continue
                                        	else:
                                                	try:
                                                        	if re.search(pattern_inline, object_line, case_flag):
									found_pattern = re.findall(pattern_o, object_line, case_flag)[0]
									if found_pattern == None or len(found_pattern) == 0:
										continue
									else: found_pattern += RECORD_SEP
                                                                	object_info += RECORD_SEP
									if result_dict.has_key(keyname):
										 result_dict[keyname] += found_pattern
                                                                        else:
                                                                                result_dict[keyname] = found_pattern
                                                        	else: pass
                                                	except: pass
                                else:
                                        print 'Too large or too small index while adding line_diff'
                                        continue
                if len(result_dict) == 0: #that's keep object_info the initial value, but make sure the initial value of object_info is blank
                        return None
                else: return result_dict 


