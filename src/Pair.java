/**
 * Author  : Marc Pellet et David Villa
 * Project : labo02 - Manager
 * File    : Pair.java
 * Date    : 2 déc. 2015
 */


public class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
    	super(); // euh, t'extend rien :O
    	this.first = first;
    	this.second = second;
    }



    public boolean equals(Object other) {
    	if (other instanceof Pair) {
    		Pair otherPair = (Pair) other;
    		return 
    		((  this.first == otherPair.first ||
    			( this.first != null && otherPair.first != null &&
    			  this.first.equals(otherPair.first))) &&
    		 (	this.second == otherPair.second ||
    			( this.second != null && otherPair.second != null &&
    			  this.second.equals(otherPair.second))) );
    	}

    	return false;
    }

    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }

    public A getFirst() {
    	return first;
    }

    public void setFirst(A first) {
    	this.first = first;
    }

    public B getSecond() {
    	return second;
    }

    public void setSecond(B second) {
    	this.second = second;
    }
}
