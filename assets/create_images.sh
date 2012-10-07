#!/bin/bash
# This file is part of jTimeSched.
# Copyright (C) 2010-2012 Dominik D. Geyer <dominik.geyer@gmail.com>
# See LICENSE.txt for details.

INFILE=jTimeSched.svg
SVG_SIZE=480
OUTFILE=jTimeSched.ico
ICO_SIZES="16 24 32 40 48 64 128 256"
OUTDIR=output

CONVERT=convert
IDENTIFY=identify

[[ ! -d $OUTDIR ]] && mkdir -p $OUTDIR

# create transparent PNG for each size
files=
for i in $ICO_SIZES; do
	out=$OUTDIR/jTimeSched_on_${i}px.png
	
	# density is 72 DPI * {desired size px} / {stored SVG size px}
	density=$(awk 'BEGIN{printf("%.12g", 72 * '${i}' / '${SVG_SIZE}')}')
	echo "$ix$i => density: $density"
	
	# convert SVG using into transparent PNG
	$CONVERT -background none -density $density $INFILE $out  #+antialias
	
	# duplicate PNG and make it darker ("inactive version")
	$CONVERT -modulate 80,30 $out $OUTDIR/jTimeSched_off_${i}px.png
	
	# add to ICO-filelist
	files="$files $out"
done

# merge into ICO
$CONVERT $files $OUTDIR/$OUTFILE  #-colors 256

# info
$IDENTIFY $OUTDIR/$OUTFILE

