#!/usr/bin/python
from os import system, listdir
from os.path import isfile, join
from subprocess import call

INPUT_PATH = "svg-icons"
OUTPUT_PREFIX = "app/src/main/res/drawable-"
INKSCAPE_EX_PATH = "/Applications/Inkscape.app/Contents/Resources/bin/inkscape"
COMMAND_FORMAT = "%s -d %d -e `pwd`/%s%s/%s `pwd`/%s/%s"
RES_TYPES = [
        ('ldpi', 120), 
        ('mdpi', 160), 
        ('hdpi', 240), 
        ('xhdpi', 320), 
        ('xxhdpi', 480)
        ]

only_files = [ f for f in listdir(INPUT_PATH) if isfile(join(INPUT_PATH,f))]
svg_files = [f for f in only_files if f.endswith('.svg')]

for r in RES_TYPES:
    label, density = r
    system("mkdir -p %s%s" % (OUTPUT_PREFIX, label))


for f in svg_files:
    for r in RES_TYPES:
        label, density = r
        system(COMMAND_FORMAT % (INKSCAPE_EX_PATH, density, OUTPUT_PREFIX, label,
            f.replace('.svg', '.png'), INPUT_PATH, f))
