package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.util.ListUnionIterator;
import com.mobeon.masp.execution_engine.util.Stack;

import java.util.ArrayList;

/**
 * @author Mikael Andersson
 */
public class HandlerTable {

    public Stack<Level> levels = new Stack<Level>();
    public Stack<Level> freeList = new Stack<Level>();

    private class Level {
        ArrayList<EventHandlerDeclaration> old = new ArrayList<EventHandlerDeclaration>();

        ArrayList<EventHandlerDeclaration> current = new ArrayList<EventHandlerDeclaration>();

        public Level() {
        }


        public Level instantiate(ArrayList<EventHandlerDeclaration> previousOld, ArrayList<EventHandlerDeclaration> previousCurrent) {
            //Create empty current
            this.current = new ArrayList<EventHandlerDeclaration>();

            //Clone previous current and use that as old
            this.old = new ArrayList<EventHandlerDeclaration>();
            this.old.addAll(previousCurrent);

            //Add all previous old to it's end (preserving ordering)
            this.old.addAll(previousOld);
            return this;
        }

        public void add(EventHandlerDeclaration ps) {
            current.add(ps);
        }

        public Level createNextLevel() {
            Level instance = freeList.pop();
            if(instance != null) {
                return instance.instantiate(old,current);
            }
            return  new Level().instantiate(old,current);
        }

        public void clear() {
            old.clear();
            current.clear();
        }
    }

    public HandlerTable() {
        levels.push(newLevel());
    }


    public void addHandler(Selector sel, EventHandler eh) {
        EventHandlerDeclaration ps = new EventHandlerDeclaration(sel, eh.getPredicate(), eh);
        levels.peek().add(ps);
    }

    public void addLevel() {
        levels.push(levels.peek().createNextLevel());
    }

    public void removeLevel() {
        Level old = levels.pop();
        if(old != null) {
            old.clear();
            freeList.push(old);
        }
    }

    private Level newLevel() {
        return new Level();
    }

    public ListUnionIterator<EventHandlerDeclaration> getIterator() {
        HandlerTable.Level current = levels.peek();
        return new ListUnionIterator<EventHandlerDeclaration>(current.current, current.old);
    }
}
