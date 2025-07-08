./gradlew test --tests benchmarks.Benchmarks
awk 'BEGIN{FS=" *, *"; OFS=", "} NR==FNR{if($1==0) baseline[$2]=$3; next} FNR==1{print "Interval,Instructions executed,Normalized Overhead"; next} {print $1, $2, $3/baseline[$2]}' results-forward-execution.csv results-forward-execution.csv > normalized-results-forward-execution.csv

