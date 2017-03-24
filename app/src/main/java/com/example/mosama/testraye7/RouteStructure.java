package com.example.mosama.testraye7;

import java.util.HashMap;
import java.util.List;

/**
 * Created by MOsama on 3/24/2017.
 */

public class RouteStructure {
  public List<HashMap<String, String>> points;
    public String summary;
    public String distance;
    public  String duration;
    public boolean fastest;
    public boolean shortest;
    public boolean selected;
    public boolean isSelected()
    {
        return selected;
    }


}
