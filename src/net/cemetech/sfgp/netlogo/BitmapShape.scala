package net.cemetech.sfgp.netlogo

import org.nlogo.shape.VectorShape
import org.nlogo.shape.Element

import org.nlogo.shape.DrawableShape

import org.nlogo.api.{GraphicsInterface,Graphics2DWrapper}
import org.nlogo.shape.Rectangle

import java.awt.{Dimension,Point,Color,Graphics2D}
import java.awt.image.BufferedImage
import java.awt.image.AffineTransformOp
import java.awt.geom.AffineTransform

// This is a terrible thing to extend, but....meh.
// VectorShapes appear to be hardcoded into Turtles
class BitmapShape(var sheetName:String, var index:Any, var cKey:Color) extends VectorShape {
	var lastColor:Color = new java.awt.Color(org.nlogo.api.Color.getARGBByIndex(editableColorIndex))
	var bitmap = SpriteManager.getSprite(sheetName, index, lastColor, cKey)
	
	override def clone() = {
		val bmpS:BitmapShape = super.clone().asInstanceOf[BitmapShape]
		bmpS.bitmap = new BufferedImage(bitmap.getWidth(), bitmap.getHeight(), BufferedImage.TYPE_INT_ARGB)
		bitmap.copyData(bmpS.bitmap.getRaster)
		bmpS.lastColor = lastColor
		bmpS
	}
	
	def render(renderTarget:Graphics2D, turtleColor:Color,
                    x:Double, y:Double, size:Double, cellSize:Double, angle:Int,
                    offX:Double, offY:Double, flippedH:Boolean, flippedV:Boolean) = {
		
		//val scale = size*cellSize
		if (renderTarget != null) {
			val renderSource = if (lastColor == null || turtleColor.equals(lastColor)){
				bitmap
			} else {
				lastColor = turtleColor;
				bitmap = SpriteManager.getSprite(sheetName, index, turtleColor, cKey)
				bitmap
			}
			
			val at = new AffineTransform();
			// Java WTFy. These happen in reverse order.
			at.translate(cellSize * (x + offX), cellSize * (y + offY))
			at.scale(if(flippedH){-size}else{size},if(flippedV){-size}else{size})
			at.translate(-renderSource.getWidth / 2, -renderSource.getHeight / 2)
			
			renderTarget.drawImage(renderSource, at, null)
		}
	}
	
	override def toString() = {
		super.toString() + "BitmapSprite: " + sheetName + " " + index + " " + cKey.getRGB() + "\n"
	}
	
	override def addElement(line:String, translateClassicColorToNewColors:Boolean) = {
		if (line.startsWith("BitmapSprite:")) {
			Console.err.println("Error: Can't reconstitute a BitmapShape yet, sorry");
		} else{
			super.addElement(line, translateClassicColorToNewColors)
		}
	}
}