package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divrep.DivRep;

public abstract interface IView {	
	abstract public void render(PrintWriter out);
}
