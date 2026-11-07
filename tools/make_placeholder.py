"""Build a harmless placeholder jar that replaces an old Maré Elétrica jar the launcher still has enabled
(no code, no content: a 'lowcodefml' resource-only mod with a unique id, so it never clashes with the real mod)."""
import sys,zipfile,re
out,old=sys.argv[1],sys.argv[2]   # output path, old version text (e.g. 0.53.4)
mid='mare_antigo_'+re.sub(r'[^0-9a-z]','_',old.lower())
toml=f'''modLoader="lowcodefml"
loaderVersion="[1,)"
license="All rights reserved"
[[mods]]
modId="{mid}"
version="1"
displayName="Arquivo antigo da Maré Elétrica {old} (vazio — pode apagar)"
description="Este arquivo foi esvaziado para não duplicar o mod. Pode apagar ou desativar."
'''
with zipfile.ZipFile(out,'w') as z:
    z.writestr('META-INF/MANIFEST.MF','Manifest-Version: 1.0\n')
    z.writestr('META-INF/neoforge.mods.toml',toml)
    z.writestr('pack.mcmeta','{"pack":{"description":"vazio","pack_format":34}}')
print(out,mid)
