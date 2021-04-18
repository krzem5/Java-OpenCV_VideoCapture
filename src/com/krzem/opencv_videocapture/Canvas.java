package com.krzem.opencv_videocapture;



import javax.swing.JComponent;
import java.awt.Graphics;



public class Canvas extends JComponent{
	Main cls;



	public Canvas(Main p){
		this.cls=p;
	}



	@Override
	public void paintComponent(Graphics g){
		this.cls.draw(g);
		super.paintComponent(g);
		g.dispose();
	}
}
