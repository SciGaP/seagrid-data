import json, sys, pybel
from cclib.parser import ccopen
from ccdbt.ParserLoader import ParserPyramid
from ccdbt.MetaFile import metafile
from collections import OrderedDict

file_name = sys.argv[1]
output_file_name = sys.argv[2]
#molecule_image_file = sys.argv[3]
result = OrderedDict()

#extracting fields from open-babel
mol = pybel.readfile('gamout',file_name).next()
result['InChI'] = mol.write('inchi').strip()
result['InChIKey'] = mol.write('inchikey').strip()
result['SMILES'] = mol.write('smiles').split('\t')[0]
result['CanonicalSMILES'] = mol.write('can').split('\t')[0]
result['PDB'] = mol.write('pdb').split('\t')[0]
result['SDF'] = mol.write('sdf').split('\t')[0]

#extracting fields from ccdbt
parser_pyramid = ParserPyramid()
if not parser_pyramid:
	print 'Fail to create a parser_pyramid'
	raise SystemExit
meta_f = metafile(file_name)
if meta_f.parse(parser_pyramid):
	if 'ParsedBy' in meta_f.record:
	    result['ParsedBy'] = meta_f.record['ParsedBy']
	if 'Formula' in meta_f.record:
	    result['Formula'] = meta_f.record['Formula']
	if 'Charge' in meta_f.record:
	    result['Charge'] = meta_f.record['Charge']
	if 'Multiplicity' in meta_f.record:
	    result['Multiplicity'] = meta_f.record['Multiplicity']
	if 'Keywords' in meta_f.record:
	    result['Keywords'] = meta_f.record['Keywords']
	if 'CalcType' in meta_f.record:
	    result['CalcType'] = meta_f.record['CalcType']
	if 'Methods' in meta_f.record:
	    result['Methods'] = meta_f.record['Methods']
	if 'Basis' in meta_f.record:
	    result['Basis'] = meta_f.record['Basis']
	if 'NumBasis' in meta_f.record:
	    result['NumBasis'] = meta_f.record['NumBasis']
	if 'NumFC' in meta_f.record:
	    result['NumFC'] = meta_f.record['NumFC']
	if 'NumVirt' in meta_f.record:
	    result['NumVirt'] = meta_f.record['NumVirt']
	if 'JobStatus' in meta_f.record:
	    result['JobStatus'] = meta_f.record['JobStatus']
	if 'FinTime' in meta_f.record:
	    result['FinTime'] = meta_f.record['FinTime']
	if 'InitGeom' in meta_f.record:
	    result['InitGeom'] = meta_f.record['InitGeom']
	if 'FinalGeom' in meta_f.record:
	    result['FinalGeom'] = meta_f.record['FinalGeom']
	if 'PG' in meta_f.record:
	    result['PG'] = meta_f.record['PG']
	if 'ElecSym' in meta_f.record:
	    result['ElecSym'] = meta_f.record['ElecSym']
	if 'NImag' in meta_f.record:
	    result['NImag'] = meta_f.record['NImag']
	if 'Energy' in meta_f.record:
	    result['Energy'] = meta_f.record['Energy']
	if 'EnergyKcal' in meta_f.record:
	    result['EnergyKcal'] = meta_f.record['EnergyKcal']
	if 'ZPE' in meta_f.record:
	    result['ZPE'] = meta_f.record['ZPE']
	if 'ZPEKcal' in meta_f.record:
	    result['ZPEKcal'] = meta_f.record['ZPEKcal']
	if 'HF' in meta_f.record:
	    result['HF'] = meta_f.record['HF']
	if 'HFKcal' in meta_f.record:
	    result['HFKcal'] = meta_f.record['HFKcal']
	if 'Thermal' in meta_f.record:
	    result['Thermal'] = meta_f.record['Thermal']
	if 'ThermalKcal' in meta_f.record:
	    result['ThermalKcal'] = meta_f.record['ThermalKcal']
	if 'Enthalpy' in meta_f.record:
	    result['Enthalpy'] = meta_f.record['Enthalpy']
	if 'EnthalpyKcal' in meta_f.record:
    	    result['EnthalpyKcal'] = meta_f.record['EnthalpyKcal']
	if 'Entropy' in meta_f.record:
	    result['Entropy'] = meta_f.record['Entropy']
	if 'EntropyKcal' in meta_f.record:
    	    result['EntropyKcal'] = meta_f.record['EntropyKcal']
	if 'Gibbs' in meta_f.record:
	    result['Gibbs'] = meta_f.record['Gibbs']
	if 'GibbsKcal' in meta_f.record:
    	    result['GibbsKcal'] = meta_f.record['GibbsKcal']
	if 'OrbSym' in meta_f.record:
	    result['OrbSym'] = meta_f.record['OrbSym']
	if 'Dipole' in meta_f.record:
            result['Dipole'] = meta_f.record['Dipole']
	if 'Freq' in meta_f.record:
            result['Freq'] = meta_f.record['Freq']
	if 'AtomWeigh' in meta_f.record:
            result['AtomWeigh'] = meta_f.record['AtomWeigh']
	if 'Conditions' in meta_f.record:
            result['Conditions'] = meta_f.record['Conditions']
	if 'ReacGeom' in meta_f.record:
            result['ReacGeom'] = meta_f.record['ReacGeom']
	if 'ProdGeom' in meta_f.record:
            result['ProdGeom'] = meta_f.record['ProdGeom']
	if 'MulCharge' in meta_f.record:
            result['MulCharge'] = meta_f.record['MulCharge']
	if 'NatCharge' in meta_f.record:
            result['NatCharge'] = meta_f.record['NatCharge']
	if 'S2' in meta_f.record:
            result['S2'] = meta_f.record['S2']
	if 'CodeVersion' in meta_f.record:
            result['CodeVersion'] = meta_f.record['CodeVersion']
	if 'CalcMachine' in meta_f.record:
            result['CalcMachine'] = meta_f.record['CalcMachine']
	if 'CalcBy' in meta_f.record:
            result['CalcBy'] = meta_f.record['CalcBy']
	if 'MemCost' in meta_f.record:
            result['MemCost'] = meta_f.record['MemCost']
	if 'TimeCost' in meta_f.record:
            result['TimeCost'] = meta_f.record['TimeCost']
	if 'CPUTime' in meta_f.record:
            result['CPUTime'] = meta_f.record['CPUTime']
	if 'Convergence' in meta_f.record:
            result['Convergenece'] = meta_f.record['Convergence']
	if 'FullPath' in meta_f.record:
            result['FullPath'] = meta_f.record['FullPath']
	if 'InputButGeom' in meta_f.record:
            result['InputButGeom'] = meta_f.record['InputButGeom']
	if 'Otherinfo' in meta_f.record:
            result['Otherinfo'] = meta_f.record['Otherinfo']
	if 'Comments' in meta_f.record:
            result['Comments'] = meta_f.record['Comments']

#extracting fields from cclib
myfile = ccopen(file_name)
data = myfile.parse()
if data:
    data.listify()
    if hasattr(data, 'natom'):
        result['NAtom'] = data.natom
    if hasattr(data, 'homos'):
        result['Homos'] = data.homos
    if hasattr(data, 'scfenergies'):
        result['ScfEnergies'] = data.scfenergies
    if hasattr(data, 'coreelectrons'):
       result['CoreElectrons'] = data.coreelectrons
    if hasattr(data, 'moenergies'):
        result['MoEnergies'] = data.moenergies
    if hasattr(data, 'atomcoords'):
        result['AtomCoords'] = data.atomcoords
    if hasattr(data, 'scftargets'):
        result['ScfTargets'] = data.scftargets
    if hasattr(data, 'nmo'):
        result['Nmo'] = data.nmo
    if hasattr(data, 'nbasis'):
        result['NBasis'] = data.nbasis
    if hasattr(data, 'atomnos'):
        result['AtomNos'] = data.atomnos

#Drawing the molecule
#mol.draw(show=False, filename=molecule_image_file)

result = json.dumps(result, separators=(',', ':'), sort_keys=False, indent=4)
file = open(output_file_name, 'w')
for row in result:
	file.write(row)
file.close()
