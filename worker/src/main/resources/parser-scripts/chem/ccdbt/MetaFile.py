#!/usr/bin/env python
# ../Method/MetaFile
# to create a metafile from its fullpath for further data parsing purpose

import os
from os.path import basename, dirname, join
from os import listdir, environ, lstat
from BasicMethod.default import PAIRSEPARATOR, ENDOFLINES


try:
	HOSTNAME = environ['HOSTNAME']
except:
	HOSTNAME = 'Unkown'


class metafile:
        """Metafile object created from its full path for further data parsing purpose."""
        def __init__(self,path,st_m_date=-1,account='Unkown',user='Unkown',group='Default'):
                """Initialize a metafile object given its fullpath."""
                self.path = path
                self.st_m_date = st_m_date
                self.account = account
                self.user = user
                self.group = group
                self.dirname = dirname(path)
                self.basename = basename(path)
                path_r_split = self.basename.rsplit('.',1)
                try:    #try to get suffix
                        if path_r_split[1]:
                                pass
                except IndexError:
                        self.suffix = None
			self.preffix = self.basename
                else: 
			self.suffix = path_r_split[1]
			self.preffix = path_r_split[0]
                self.lines = []

        def readlines(self):
                try:
                        fd = open(self.path,'r')
                except:
                        print 'Cannot open %s..' % (self.path)
                        return 0
                else:
                        self.lines = fd.readlines()
                        fd.close()
                        return 1

        def parsertest_1(self, primary_parser_dict):
                """Primary_parser_list is to decide which parser to be used mainly from suffix. {parser_name:suffix}."""
                suffixes = primary_parser_dict
		suf_list_list = suffixes.values()
		suf_all = []
		for x in range(len(suf_list_list)):
			suf_all += suf_list_list[x]
                if self.suffix not in suf_all:
                        return []
                return [qualified_parser for qualified_parser in suffixes
                                        if self.suffix in suffixes[qualified_parser] or '*' in suffixes[qualified_parser] ] #return qualified parser_1's

        def parsertest_2(self, secondary_parser_dict, qualified_parser_list, case_sens=True, from_backwards=False):
                from Parser.std import findkey, findkeys
                """V_p_l is from parsertest_1."""
                filter_2 = dict([(p_name,secondary_parser_dict[p_name]) for p_name in qualified_parser_list
                                                                        if p_name in secondary_parser_dict])
                return [p_name for p_name in filter_2
                                if findkeys(self.lines, filter_2[p_name], case_sens, from_backwards)]
                                #filter_2[p_name] is a list of keys!
                # return [] when no such a match

        def parser_final(self, final_parser_dict, qualified_parser):
                try: parser = final_parser_dict[qualified_parser]
                except:
                        print 'Parser does not exist..'
                        return 0
                else:
			for pairname in parser.pair: #pairname in parser.pair is the suffix of a paired file.
				try:
					pair_path = join(self.dirname,(self.preffix + '.' + pairname))	
					fd_pair = open(pair_path, 'r')
					pair_lines = fd_pair.readlines()
					fd_pair.close()
				except:
					continue
				else:
					self.lines.append(PAIRSEPARATOR)
					self.lines.append((PAIRSEPARATOR+pairname))
					self.lines.extend(pair_lines)
			self.lines.append(ENDOFLINES)

                        self.record = parser.parse(self) # parse is the interface between metafile and parser object
                        #print self.record
			return 1

        def _parse_set(self,p_dict_1,p_dict_2,p_dict_3,p_dict_priority): # obselete p_dict_2_2
                """
                Entire process of parsing a metafile.
                p_dict_1 is primary_parser_dict, p_dict_2[_?] is secondary_parser_dict('s),
                p_dict_3 is final_parser_dict, {name1:{parser:,priority:,},name2...}
                """
                #print 'debug, parse begin'
                v_list = self.parsertest_1(p_dict_1) # qualified parser list
                #print 'debug qulified list', v_list
                if len(v_list) == 0:
                        return 0 #zero match
                try:
                        self.readlines() # contents parsing is required from test_2
                except:
                        print 'Error when loading lines from [ %s ]..' % (self.path)
                        return 0 # return 0 when fail

                q_list = self.parsertest_2(p_dict_2,v_list) #verified parser list
                if len(q_list) == 0:
                        return 0 #zero match
                #if len(q_list) > 1:
                #       if len(p_dict_2_2) == len(p_dict_2):
                #               q_list = self.parsertest_2(p_dict_2_2,q_list)
                #if len(q_list) == 0:
                #       return 0
                if len(q_list) >1: # chose one with the highest priority
                        highest_parser = max([(p_dict_priority[name], name) for name in q_list])[1]
                        q_list = [highest_parser]
                if len(q_list) == 1: #if the secondary parser could be trusted
                        return self.parser_final(p_dict_3,q_list[0])
        def parse(self, parserpyramid):
		try:
                	result = self._parse_set(parserpyramid.level_1,parserpyramid.level_2,parserpyramid.level_3,parserpyramid.priority_dict)
		except MemoryError:
			print 'debug,f5:\t',self.path
			return 0
		except:
			print '[debug_parsing error]',self.path
			raise #for debug
			return 0
                if not result:
                        return 0
                else:
			if self.st_m_date == -1: #then evoke os.lstat
				try:
					if self.path != None:
						pass
				except:
					print 'Error occurs when parsing last modified time..'
				else:
					st_mtime = os.lstat(self.path).st_mtime
					from time import gmtime
					time_tuple = gmtime(st_mtime)
					st_m_date = str(time_tuple[0]) + '-' + str(time_tuple[1]) + '-' + str(time_tuple[2])
					self.st_m_date = st_m_date

                        self.record['User'] = self.user
                        self.record['GroupName'] = self.group
                        self.record['Account'] = self.account
			self.record['FullPath'] = self.path
			try:
				file_root, relative_path = self.path.split(self.account,1)
			except:
				file_root = None
				relative_path = " [FullPath]" + self.path #tell some parser that it is a full path
                        self.record['FileRelativePath'] = relative_path[1:]
			self.record['FileRoot'] = file_root
			self.record['StorageHost'] = HOSTNAME
			self.record['LocalPath'] = (HOSTNAME + ':' + self.path)
			#print 'debug-localpath\t', self.record['LocalPath']
			self.record['RemotePath'] = (self.account + ':' + relative_path)
                        self.record['LastModTime'] = str(self.st_m_date)
			return 1
                #also add infomation from um and so on..


