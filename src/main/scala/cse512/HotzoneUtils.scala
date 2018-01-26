package cse512

object HotzoneUtils {

  def ST_Contains(queryRectangle: String, pointString: String ): Boolean = {
    val RectanlePoints = queryRectangle.split(",").map(_.toDouble)
    val point = pointString.split(",").map(_.toDouble)

    val min_x_coord = math.min(RectanlePoints(0),RectanlePoints(2))
    val min_y_coord = math.min(RectanlePoints(1),RectanlePoints(3))
    val max_x_coord = math.max(RectanlePoints(0),RectanlePoints(2))
    val max_y_coord = math.max(RectanlePoints(1),RectanlePoints(3))

    if( (point(0)>= min_x_coord && point(0)<= max_x_coord) && ( point(1)>= min_y_coord && point(1)<= max_y_coord)   )
    {
      true
    }

    else{
      false
    }

  }

  // YOU NEED TO CHANGE THIS PART

}
