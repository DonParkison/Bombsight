package com.bombbomb.bombsight;

import com.esri.arcgisruntime.geometry.Point;

/**
 * Created by cos-mbp-don on 3/21/17.
 */

public interface BombsightLocListenerCallbacks {

    void onLocationChanged(Point locationPoint);
}
