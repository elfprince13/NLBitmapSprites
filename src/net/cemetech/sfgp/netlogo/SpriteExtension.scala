package net.cemetech.sfgp.netlogo

import org.nlogo.api.DefaultClassManager

import org.nlogo.api.LogoListBuilder
import org.nlogo.api.PrimitiveManager
import org.nlogo.api.Syntax
import org.nlogo.api.Context
import org.nlogo.api.Turtle
import org.nlogo.api.World
import org.nlogo.api.DefaultReporter
import org.nlogo.api.DefaultCommand
import org.nlogo.api.Argument
import org.nlogo.api.ExtensionException
import org.nlogo.api.LogoException
import org.nlogo.nvm.ExtensionContext

import org.nlogo.nvm.FileManager
import org.nlogo.nvm.Workspace

import org.nlogo.shape.editor.ManagerDialog

import java.awt.{Point,Dimension,Color}
import java.awt.image.BufferedImage
import java.io.{PrintWriter,StringWriter}


class SpriteExtension extends DefaultClassManager {

	
	def load(primitiveManager:PrimitiveManager) = {
		primitiveManager.addPrimitive("new-bitmap-spritesheet", CreateSpriteSheet)
		primitiveManager.addPrimitive("get-indexed-sprite-with-chroma", GetSpriteCurrentColor)
		primitiveManager.addPrimitive("draw-sprite", DrawSprite)
	}
	
	
}

object CreateSpriteSheet extends DefaultCommand {
    override def getSyntax():Syntax = {
    	Syntax.commandSyntax(Array[Int](Syntax.StringType,Syntax.StringType,Syntax.ListType,Syntax.ListType, Syntax.ListType, Syntax.BooleanType));
    }

    override def getAgentClassString():String = { "OTPL" }

    def perform(args:Array[Argument], context:Context) = {
      try {
    	SpriteManager.setupSprites(
    			args(0).getString,
    			context.asInstanceOf[ExtensionContext].workspace.fileManager.attachPrefix(args(1).getString),
    			new Dimension(args(2).getList.get(0).asInstanceOf[Double].toInt,args(2).getList.get(1).asInstanceOf[Double].toInt),
    			new Point(args(3).getList.get(0).asInstanceOf[Double].toInt,args(3).getList.get(1).asInstanceOf[Double].toInt),
    			new Dimension(args(4).getList.get(0).asInstanceOf[Double].toInt,args(4).getList.get(1).asInstanceOf[Double].toInt),
    			args(5).getBoolean
    			)
      } catch {
        case e : Exception => val sw = new StringWriter
        		e.printStackTrace( new PrintWriter(sw) )
          throw new ExtensionException("Error in new-bitmap-spritesheet (in dir " + context.asInstanceOf[ExtensionContext].workspace.fileManager.getPrefix + " ): " + e.getMessage + "\n" + sw.toString())
      }
    }
}

object GetSpriteCurrentColor extends DefaultReporter {
    override def getSyntax():Syntax = {
    	Syntax.reporterSyntax(Array[Int](Syntax.StringType,Syntax.NumberType,Syntax.ListType),Syntax.WildcardType);
    }

    override def getAgentClassString():String = { "OTPL" }

    override def report(args:Array[Argument], context:Context):BitmapShape = {
      try {
    	val chromaList = args(2).getList
    	val chromaColor:Color = if(chromaList.size() == 3) {
    		new Color(org.nlogo.api.Color.getARGBIntByRGBAList(chromaList))
    	} else {
    		Console.out.println("Warning: bad color specification")
    		Color.BLACK
    	}
    	new BitmapShape(
    			args(0).getString,
    			args(1).getIntValue,
    			chromaColor
    	)
      } catch {
        case e : Exception => val sw = new StringWriter
        		e.printStackTrace( new PrintWriter(sw) )
          throw new ExtensionException("Error in get-indexed-sprite-with-chroma: " + e.getMessage + "\n" + sw.toString())
      }
    }
}

object DrawSprite extends DefaultCommand {
    override def getSyntax():Syntax = {
    	Syntax.commandSyntax(Array[Int](Syntax.WildcardType,Syntax.ListType,Syntax.BooleanType,Syntax.BooleanType));
    }

    override def getAgentClassString():String = { "T" }

    override def perform(args:Array[Argument], context:Context) = {
      try{
    	val turtle = context.getAgent.asInstanceOf[Turtle]
    	val sprite = args(0).get.asInstanceOf[BitmapShape]
    	val turtleColor = new Color(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(turtle.color.asInstanceOf[Double] % 140))
    	val world = context.asInstanceOf[ExtensionContext].workspace.world.asInstanceOf[World]
    	
    	val offset = args(1).getList
    	val offX = offset.get(0).asInstanceOf[Double]
    	val offY = offset.get(1).asInstanceOf[Double]
    	
    	val flippedH = args(2).getBoolean
    	val flippedV = args(3).getBoolean
    	
    	val rt = context.getDrawing
    	val rth = rt.getHeight
    	val rtw = rt.getWidth
    	
    	val turtleHeading = turtle.heading
    	sprite.render(rt.getGraphics.asInstanceOf[java.awt.Graphics2D],
    	    turtleColor,turtle.xcor - world.minPxcor,-turtle.ycor + world.maxPycor,turtle.size,
    	    world.patchSize,turtleHeading.toInt,offX,offY,flippedH,flippedV)
      } catch {
        case e : Exception => val sw = new StringWriter
        		e.printStackTrace( new PrintWriter(sw) )
          throw new ExtensionException("Error in get-indexed-sprite-with-chroma: " + e.getMessage + "\n" + sw.toString())
      }
    }
}