# SIGSpatial-cup-2016 HotSpot-Analysis

## Requirement

This is code for final project of distributed database systems, you can find the requirements of the project here https://github.com/jiayuasu/CSE512-Project-Hotspot-Analysis-Template. This problem statement is a modified problem statement of ACM SIGSpatial cup in 2016.


The original problem definition page is here: [http://sigspatial2016.sigspatial.org/giscup2016/problem](http://sigspatial2016.sigspatial.org/giscup2016/problem) 

our findings and results are here : SIGSpatial-cup-2016---HotSpot-Analysis/ProjectReport_BigDataAndSpark.pdf

### submit code to Spark
1. Go to project root folder
2. Run ```sbt clean assembly```. You may need to install sbt in order to run this command.
3. Find the packaged jar in "./target/scala-2.11/CSE512-Project-Hotspot-Analysis-Template-assembly-0.1.0.jar"
4. Submit the jar to Spark using Spark command "./bin/spark-submit". A pseudo code example: ```./bin/spark-submit ~/GitHub/CSE512-Project-Hotspot-Analysis-Template/target/scala-2.11/CSE512-Project-Hotspot-Analysis-Template-assembly-0.1.0.jar test/output hotzoneanalysis src/resources/point-hotzone.csv src/resources/zone-hotzone.csv hotcellanalysis src/resources/yellow_tripdata_2009-01_point.csv```
