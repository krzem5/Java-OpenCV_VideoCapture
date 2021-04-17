package com.krzem.opencv_videocapture;



import javax.swing.JComponent;
import java.awt.Graphics;



public class Canvas extends JComponent{
	Main parent;



	public Canvas(Main p){
		this.parent=p;
	}



	@Override
	public void paintComponent(Graphics g){
		this.parent.draw(g);
		super.paintComponent(g);
		g.dispose();
	}
}
