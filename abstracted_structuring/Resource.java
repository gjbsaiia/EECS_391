/*
 *  Data type for resources
 *  Stores x,y coordinate along with whether or not the resource is actively being mined
 *  
 */

import java.lang.Math;

public class Resource implements Comparable<Resource>{
	public final int id;
	
	private final int x;
	private final int y;
	public double dist;
	
	private int amount;
	
	private int numWork;
	
	public Resource(int resourceID, int xDimen, int yDimen, int totalAmount){
		this.id = resourceID;
		this.x = xDimen;
		this.y = yDimen;
		this.dist = -1;
		this.amount = totalAmount;
	}
	
	public void incWork(){
		this.numWork ++;
	}
	
	public void decWork(){
		this.numWork --;
	}
	
	public boolean checkCap(){
		return (numWork < 3);
	}
	
	public int getX(){return this.x;}
	
	public int getY(){return this.y;}
	
	public void updateAmount(int update){
		this.amount = this.amount - update;
	}
	
	public int getAmount(){
		return this.amount;
	}
	
	public boolean isActive(){
		return (this.amount > 0);
	}
	
	public void setDist(int hX, int hY){
		this.dist = Math.sqrt(Math.pow((double)(this.x - hX),2.0) + Math.pow((double)(this.y - hY),2.0));
	}
	
	public boolean equals(Object o){
		if(!(o instanceof Resource)) return false;
		Resource toResource = (Resource) o;
		return (this.id == toResource.id);
	}
	
	public int compareTo(Resource o){
		if(this.dist > o.dist){
			return 1;
		}
		if(this.dist < o.dist){
			return -1;
		}
		return 0;
	}
}

