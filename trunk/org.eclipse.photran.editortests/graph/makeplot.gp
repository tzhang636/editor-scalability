# author: andrew kryczka

set terminal png font "Times-Roman,14"
set output "graph.png"
# set terminal postscript enhanced color font "Times-Roman,18"
# set output '| ps2pdf - ./responsiveness-graph.pdf'

set style line 81 lt rgb "#808080"  # grey
set grid back linestyle 81

set border 3 # Remove border on top and right.  
set xtics nomirror
set ytics nomirror

set xrange [0:90]
set yrange [0:5000]
set xtics 10
set ytics 500

set title "Photran Editor's Responsiveness to Keystrokes"
set xlabel "File size (thousands of lines)"
set ylabel "Typing and blocking time (ms)"

set style line 1 lt 1 lw 4 pt 1 linecolor rgb "#A00000" 
set style line 2 lt 1 lc rgb "#00A000" lw 4 pt 2

set key center left

plot "./results.dat" using 1:2 title 'Default Model Builder' w lp ls 1, \
	"./results.dat" using 1:3 title 'Concurrent Model Builder' w lp ls 2
