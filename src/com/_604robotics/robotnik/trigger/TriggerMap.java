package com._604robotics.robotnik.trigger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com._604robotics.robotnik.exceptions.NonExistentTriggerError;
import com._604robotics.robotnik.logging.Logger;

/**
 * A map containing triggers.
 */
public class TriggerMap implements Iterable<Map.Entry<String, Trigger>> {
    private final Map<String, Trigger> triggerTable = new HashMap<String, Trigger>();
    
    /**
     * Adds a trigger.
     * @param name Name of the trigger.
     * @param trigger Trigger to add.
     */
    protected void add (String name, Trigger trigger) {
        this.triggerTable.put(name, trigger);
    }
    
    /**
     * Gets a trigger.
     * @param name Name of the trigger.
     * @return The retrieved trigger.
     * @throws NonExistentTriggerError
     */
    protected Trigger getTrigger (String name) {
        Trigger returnTrigger = this.triggerTable.get(name);
        if (returnTrigger == null) {
        	Logger.missing("Trigger", name);
        	throw new NonExistentTriggerError("Attempted to access nonexistent trigger" + name);
        }
        
        return returnTrigger;
    }

    /**
     * Gets the size of the trigger map.
     * @return The trigger map's size.
     */
    public int size() {
    	return this.triggerTable.size();
    }
    
    @Override
    public Iterator<Map.Entry<String, Trigger>> iterator () {
        return this.triggerTable.entrySet().iterator();
    }
}
