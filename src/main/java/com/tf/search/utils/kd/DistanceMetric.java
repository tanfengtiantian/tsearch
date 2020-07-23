// Abstract distance metric class

package com.tf.search.utils.kd;

abstract class DistanceMetric {
    
    protected abstract double distance(double [] a, double [] b);
}
