set terminal pngcairo  transparent enhanced font "arial,10" fontscale 1.0 size 1024, 679
set output 'simple.1.png'
# put it to eps (vectors)
# change to black and white
set key inside left top vertical Right noreverse enhanced autotitles box linetype -1 linewidth 1.000

set pointintervalbox 1

set yrange [0:1.1]
set pointintervalbox 1

set title "CYCLON 1000 Nodes add 1 each cycle"

set style line 1 lc rgb '#DC143C' lt 1 lw 2 pt 3
set style line 2 lc rgb '#3CB371' lt 1 lw 2 pt 3
set style line 3 lc rgb '#4169E1' lt 1 lw 2 pt 3

plot [0:1000] "a.txt" using ($0):1 title "clustering" w l ls 1, \
    "b.txt" using ($0):1 title "reach quota" w l ls 2, \
    "c.txt" using ($0):1 title "node count" w l ls 3

