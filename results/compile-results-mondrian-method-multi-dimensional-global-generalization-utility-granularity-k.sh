#!/bin/bash

cd results-mondrian-method-multi-dimensional-global-generalization-utility-granularity-k/img

sed -i -e '1s/size 2.0,2.0/size 2.15,2.0/' image_0.gp
sed -i -e '1s/,10/Times,12/' *.gp
sed -i -e '14iset yrange[0:100]' *.gp
sed -i -e '15iset format y "%.0f%%" ' *.gp
sed -i -e '16iset ytics 25' *.gp

for FILE in *.gp
do
	gnuplot ${FILE%.*}.gp
	ps2pdf -dPDFSETTINGS#/prepress -dEmbedAllFonts#true -dUseFlateCompression#true ${FILE%.*}.eps
    pdfcrop ${FILE%.*}.eps.pdf
    rm ${FILE%.*}.eps
    rm ${FILE%.*}.eps.pdf
    rm ${FILE%.*}.pdf
    mv ${FILE%.*}.eps-crop.pdf ${FILE%.*}.pdf
done

cd ..

# remove caption
sed -i -e '13d' plot.tex
sed -i -e '1,$s/width=/height=/' plot.tex

pdflatex plot.tex
pdfcrop plot.pdf
mv plot-crop.pdf plot.pdf

cd ..