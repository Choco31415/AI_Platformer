package AI;

import java.util.ArrayList;

public class Neuron implements Cloneable {

	private double x;
	private int y;
	private Double activation;//How stimulated is this neuron?
	
	ArrayList<Gene> links = new ArrayList<Gene>();
	
	public Neuron(double x_, int y_) {
		x = x_;
		y = y_;
	}
	
	public Neuron(double x_, int y_, ArrayList<Gene> l) {
		x = x_;
		y = y_;
		links = l;
	}
	
	public void setActivation(Double num) {
		if (num == null) {
			activation = null;
		} else if (num > 1) {
			activation = 1.0;
		} else if (num < -1.0) {
			activation = -1.0;
		} else {
			activation = num;
		}
	}
	
	public void setx(double num) {
		x = num;
	}
	
	public void clearLink() {
		links = new ArrayList<Gene>();
	}
	
	public void addLink(Gene l) {
		links.add(l);
	}
	
	public void setLinks(ArrayList<Gene> l) {
		links = l;
	}
	
	public double getx() { return x; }
	public int gety() { return y; }
	public double[] getPos() { return new double[]{x, y}; }
	public Double getActivation() { return activation; }
	public ArrayList<Gene> getLinks() { return links; }
	
	public Neuron clone() {
		return new Neuron(x, y, links);
	}
}
