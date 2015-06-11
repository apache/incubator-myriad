package com.ebay.myriad.common;

import com.google.gson.Gson;

/**
 * 
 * Framework update from the scheduler to the executor
 *
 */
public class FrameworkUpdate {
    /**
     * Define what kind of this update is.
     */
    public static enum Type {Command};
    
    /**
     * Define what kind of operation or command this update is.
     */
    public static enum Operation {Shutdown};
    
    public FrameworkUpdate() {
    }
    
    public FrameworkUpdate(Type type, Operation operation) {
        setType(type);
        setOperation(operation);
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public void setOperation(Operation operation) {
        this.operation = operation;
    }
    
    public Operation getOperation() {
        return operation;
    }
    
    public String toString () {
        return new Gson().toJson(this);
    }
    
    private Type type;
    private Operation operation;
}
