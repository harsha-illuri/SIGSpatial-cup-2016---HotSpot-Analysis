package cse512

import java.util
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._


object HotcellAnalysis {
  Logger.getLogger("org.spark_project").setLevel(Level.WARN)
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)



  def runHotcellAnalysis(spark: SparkSession, pointPath: String): DataFrame = {
    // Load the original data from a data source
    var pickupInfo = spark.read.format("com.databricks.spark.csv").option("delimiter", ";").option("header", "false").load(pointPath);
    pickupInfo.createOrReplaceTempView("nyctaxitrips")
    //  pickupInfo.show()

    // Assign cell coordinates based on pickup points
    spark.udf.register("CalculateX", (pickupPoint: String) => ((
      HotcellUtils.CalculateCoordinate(pickupPoint, 0)
      )))
    spark.udf.register("CalculateY", (pickupPoint: String) => ((
      HotcellUtils.CalculateCoordinate(pickupPoint, 1)
      )))
    spark.udf.register("CalculateZ", (pickupTime: String) => ((
      HotcellUtils.CalculateCoordinate(pickupTime, 2)
      )))
    pickupInfo = spark.sql("select CalculateX(nyctaxitrips._c5),CalculateY(nyctaxitrips._c5), CalculateZ(nyctaxitrips._c1) from nyctaxitrips")
    var newCoordinateName = Seq("x", "y", "z")
    pickupInfo = pickupInfo.toDF(newCoordinateName: _*)
//    pickupInfo.show()


    // Define the min and max of x, y, z
    val minX = -74.50 / HotcellUtils.coordinateStep
    val maxX = -73.70 / HotcellUtils.coordinateStep
    val minY = 40.50 / HotcellUtils.coordinateStep
    val maxY = 40.90 / HotcellUtils.coordinateStep
    val minZ = 1
    val maxZ = 31
    val numCells = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)


    // changed code from here

    val x_range = maxX.toInt - minX.toInt + 1

    val y_range = maxY.toInt - minY.toInt + 1
    val z_range = maxZ.toInt - minZ.toInt + 1

    val count_df = pickupInfo.groupBy("x", "y", "z").count().persist()
    count_df.createOrReplaceTempView("tripscount")


    var xbar = pickupInfo.count() / numCells
    // calculate s
    var temp_top = spark.sql("select sum(count*count) from tripscount").first().get(0).toString.toDouble

    var sd = math.sqrt(temp_top / numCells - (xbar * xbar))
    //    println("---------------")
    //    println(sd)
    // main gscore value calculation
    spark.udf.register("checkneighbours", (x: Double, y: Double, z: Double, neigh_x: Double, neigh_y: Double, neigh_z: Double) => ((
      HotcellUtils.checkneighbours(x.toLong, y.toLong, z.toLong, neigh_x.toLong, neigh_y.toLong, neigh_z.toLong)
      )))

    spark.udf.register("getNeighbours", (x: Long, y: Long, z: Long) => ((
      HotcellUtils.getNeighbours(x.toLong, y.toLong, z.toLong)
      )))

    def getGscore(neighCountReduced: Double, neighCount: Double): Double = {
      var Numerator = neighCountReduced - xbar * neighCount
      var denom = sd * math.sqrt(((numCells * neighCount) - (neighCount * neighCount)) / (numCells - 1))
      var gScore = Numerator / denom
      if (gScore.toString.equals("NaN")) println(Numerator + " " + denom + " " + math.sqrt(((numCells * neighCount) - (neighCount * neighCount)) / (numCells - 1)))
      return gScore
    }

    spark.udf.register("getGscore", (neighCountReduced: Double, neighCount: Double) => ((
      getGscore(neighCountReduced, neighCount)
      )))

    var sumofNeighbours = spark.sql("select tc1.x, tc1.y, tc1.z, sum(tc2.count) as neighCountReduced, getNeighbours(tc1.x, tc1.y, tc1.z) as neighCount from tripscount tc1 CROSS JOIN tripscount tc2 where checkneighbours(tc1.x, tc1.y, tc1.z, tc2.x, tc2.y, tc2.z) group by tc1.x,tc1.y,tc1.z").persist()
//    sumofNeighbours.show()
//    sumofNeighbours.count()
    sumofNeighbours.createOrReplaceTempView("sumofNeighbours")

    var getFinalResult = spark.sql("select x,y,z,getGscore(neighCountReduced, neighCount) as zScore from sumofNeighbours order by zScore desc ")
    getFinalResult.createOrReplaceTempView("getFinalResult")
    var finalFinal = spark.sql("select x,y,z from getFinalResult")


    return finalFinal


  }

}

