package it.ltronchi.lpa

import scala.swing.Component
import java.awt.Graphics2D
import java.awt.Color
import scala.swing.event.MousePressed
import java.awt.Dimension
import scala.swing._
import javax.swing.border.LineBorder
import scala.swing.event.MouseClicked
import java.awt.Font
import scala.collection.Set
import scala.actors.Actor

object MyCell {
	var worldX = 0;
	var worldY = 0;
}


class MyCell(val x:Int, val y:Int) extends FlowPanel with Actor {
	
	
	//size = new Dimension(10,10)
	
	contents += new Label("")
	
	border = LineBorder.createGrayLineBorder
	
	var _living = false
	background = Color.WHITE
	
	
	var neighbors =  Set.empty[Array[Int]];
	for (i <- -1 to 1) {
		for (j <- -1 to 1) {
			if (!(i==0 && j==0))
				neighbors += Array((x + MyCell.worldX + i) % MyCell.worldX, (y + MyCell.worldY + j) % MyCell.worldY)
		}
	}
	
	listenTo(mouse.clicks) 
	reactions += {
		case e: MouseClicked =>{
			if (living) {
				living = false
				background = Color.WHITE
			}
			else {
				living = true 
				background = Color.BLACK
			}
			repaint()
		}
	}
	
	def living = _living
	
	def living_= (alive:Boolean) = _living = {
		if (alive == true) {
			background = Color.BLACK
		} else {
			background = Color.WHITE
		}	
		alive
	}
	
	def next() {
		var aliveNeighborsCounter = 0;
		neighbors foreach(item => if (Main.grid.contents(item(1)*MyCell.worldX + item(0)) match {
		case value:MyCell =>  {
			value ! "getLiving"
			react {
			  case response:Boolean => response
			  case s:Any => println(s);true
			}
		}
		})aliveNeighborsCounter += 1 )
		
		
		if (living && aliveNeighborsCounter < 2) living = false
		if (living && aliveNeighborsCounter > 3) living = false
		if (!living && aliveNeighborsCounter == 3) living = true 
	}
	
	
	var esecuting = true	
	def act() = {
		esecuting = false
//				println("started" + (y*MyCell.worldY + x))
		while(true) {
			
			receiveWithin(10) {
				case "p" => {if (esecuting) esecuting = false else esecuting = true}
				case "a" => esecuting = false
				case "getLiving" => sender ! living
				case _ =>
			}
			
			if (esecuting)
			try {
				next()
//				println(y*MyCell.worldY + x)
				Thread.sleep((Main.sleepTime*1000/Main.speedCoef).toLong)
			} catch {
				case ie: InterruptedException => 
			}
			
		}
	}
	
}