import json, sys, pybel
from cclib.parser import ccopen

#extracting fields from cclib
file_name = sys.argv[1]
output_file_name = sys.argv[2]
molecule_image_file = sys.argv[3]
myfile = ccopen(file_name)
data = myfile.parse()
data.application = myfile.logname

#extracting fields from open-babel
if data.application == 'Gaussian':
	gaussian_mol = pybel.readfile('g98',file_name).next()
	data.inchi = gaussian_mol.write('inchi').strip()
	data.inchikey = gaussian_mol.write('inchikey').strip()
	data.smiles = gaussian_mol.write('smiles').split('\t')[0]
	data.canosmiles = gaussian_mol.write('can').split('\t')[0]
	gaussian_mol.draw(show=False, filename=molecule_image_file)
elif data.application == 'GAMESS':
        gamess_mol = pybel.readfile('gamout',file_name).next()
        data.inchi = gamess_mol.write('inchi').strip()
        data.inchiKey = gamess_mol.write('inchikey').strip()
        data.smiles = gamess_mol.write('smiles').split('\t')[0]
        data.canoSmiles = gamess_mol.write('can').split('\t')[0]
        gamess_mol.draw(show=False, filename=molecule_image_file)
elif data.application == 'NWChem':
        nwchem_mol = pybel.readfile('nwo',file_name).next()
        data.inchi = nwchem_mol.write('inchi').strip()
        data.inchiKey = nwchem_mol.write('inchikey').strip()
        data.smiles = nwchem_mol.write('smiles').split('\t')[0]
        data.canoSmiles = nwchem_mol.write('can').split('\t')[0]
        nwchem_mol.draw(show=False, filename=molecule_image_file)
elif data.application == 'Molpro':
        molpro_mol = pybel.readfile('mpo',file_name).next()
        data.inchi = molpro_mol.write('inchi').strip()
        data.inchiKey = molpro_mol.write('inchikey').strip()
        data.smiles = molpro_mol.write('smiles').split('\t')[0]
        data.canoSmiles = molpro_mol.write('can').split('\t')[0]
        molpro_mol.draw(show=False, filename=molecule_image_file)

data.listify()
result = json.dumps(data.__dict__, separators=(',', ':'), sort_keys=True, indent=4)
file = open(output_file_name, 'w')
for row in result:
	file.write(row)
file.close()
