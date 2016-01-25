#!/usr/bin/env python

################################## README #######################################
# Read these first lines to start with writing your own parser!!!!!!!           #
# This is a template also a help document to create a user-defined parser.      #
#################################################################################
# Comments or helps will follow '#', which do not affect the parser at any time.#
# 'setting' is a D{dictionary};                                                 #
# -- {name},{level},etc. are K{keys} of the D{dictionary},                      # 
# -- values following '=' are V{values} of the D{dictionary}.                   #
# -- dict() is to constructed the D{dictionary} 'setting'.                      #
# DO separate lines by ','                                                      #
# strings should be quoted this way: 'string'                                   #
# string without quote marks is a variable                                      #
# list is quoted by [].                                                         #
# -- For example, b = ['str', 1, a]. In this case, b is a variable,             #
# -- whose value is a list which contains a string, an integer and a variable.  #
# -- elements in a list is separated by ','                                     #
# -- [] is a blank list.                                                        #
# {attrib} in comments referred to an attribute named 'attrib' of 'setting'.    #
#                                                                               #
# NEVER modify this template,                                                   #
# -- but copy it to another file named like 'prsrname.py', then do modification.#
#                                                                               #
#################################################################################
setting = dict(
		
	type = 'method'              # is 'method' by default. others are 'gaussian', 'molpro', etc, or you can define your own #####need more document here
	,
	name = __name__              # It is the name of the setting file, no need to modify it
	,
	parser_name = ''             # This is the parser name written in the database, if leave it blank, it will be consistant with the {name}
	,
	level = 1                    # Level accept a integer value from 0-3, it is 1 by default, lower number means bigger priority. 
	,
	outer_rule = '.log'          # It is a filename-level filter for output-data-file in this parser, means filename ends with {outer_rule}
	,
	pair_rule = []               # Similar like {outer_rule}, is a filter for related files of the output-data-file(the main file). if only want to analyse the main file, leave it blank.
	,
	parser_list = []             # THIS IS VERY IMPORTANT, choose which kinds of information will get parsed here.
	                             # -- these are available for parser_list: 
				     # -- 'version' : version of a calculation software package, e.g. 'Gaussion 03.02'.
				     # -- 'init_coor', 'init_coor_norm' : inital coordinamtes of a calculation, the latter one is normalized coordinates based on the former one.
				     # -- 'formula' : will only affect when have 'init_coor' and 'init_coor_norm'.
				     # -- 'title' : title of a calculation. usually specified by user who run a calculation.
				     # -- 'keyword' : keyword of a calculation. e.g. 'freq=noraman b3lyp/3-21G'.
				     # -- 'basicfunction' : how many basic functions have been used in the calculation.
				     # -- 'mem' : memory used by a calculation.
				     # -- 'ifnorm' : it is mandatory, you don't have to choose it here, but need to specify it later. see 'ifopted'
				     # -- 'ifopted' : if an optimization calculation finishes well, if not the parser will then skip this file, following parsings will not be performed.
				     # -- 'final_coor', 'final_coor_norm' : useful in an optimization calculation. will also return final_coor_type, e.g. 'opt' or 'ts', maybe...
				     # -- 'freq' : frequencies.
				     # -- 'zpe' : Zero point energy.
				     # -- 'energy' : will return energy and energytype. e.g. ('HF',236.1231)
				     # -- 'sym' : symmetry information.
				     # -- 'pg' : which point group the last geometry belongs to.
	,
	parser = dict(               # parser is a D{dictionary} inside 'setting', storing parameter for each parsers.
				     
		
		ifnorm = dict(func = 'std.findline', type = '', pattern_1 = '', pattern_o = '')		#'ifnorm' is a sub-dictionary of setting['parser']		     
			            # 'func' is the agorithm name. defined under the path '/RCcube/Method/Parser'. 'std' refers to 'std.py', 'findline' is the function inside 'std.py'		     
				    # 'type' is to specify different parsers, e.g. energy has types 'HF', 'b3lyp', 'CCSD(T)'.
				    # Other parameters following 'func' and 'type' will goes to the function referred by 'func', read documents of the 'func' to specify your parsers.
				    # Nth parameter here will be the Nth parameter of 'func',(i.e. the first 2 of 'func' is always fixed).
				    # All the parameters without a default value need be specified, others need to take values if necessarily.
				    # Basic agorithms are already defined in 'std.py'.
				    # You can also create your own under the same directory whose filename with a suffix '.py' .
				    # Before doing that, read the 'README' document in that directory.
		#...
		#also define parameters for other conponent parsers in setting['parser_list'].
				   
		)
	)