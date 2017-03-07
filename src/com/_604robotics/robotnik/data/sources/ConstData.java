package com._604robotics.robotnik.data.sources;

import com._604robotics.robotnik.data.Data;

/**
 * Data with a constant value.
 */
public class ConstData implements Data {
    private final double value;

    /**
     * Creates constant data.
     * @param value Constant value to contain.
     */
    public ConstData (double value) {
        this.value = value;
    }
    
    @Override
    public double get() {
        return value;
    }
}
