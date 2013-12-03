package net.cemetech.sfgp.netlogo

import java.awt.image.{BufferedImage,RGBImageFilter,ImageProducer,FilteredImageSource}
import java.awt.{Dimension,Point,Color,Toolkit}
import javax.imageio.ImageIO
import java.io.File

object SpriteManager {
	var spriteSheets:Map[String,SpriteSheet] = Map[String,SpriteSheet]();
	
	def addSpriteSheet(name:String, sheet:SpriteSheet) = {
		spriteSheets = spriteSheets + (name -> sheet)
	}
	
	def makeSpriteSheet(path:String, spriteSize:Dimension, offset:Point = null, bounds:Dimension = null, columnMajor:Boolean = false):SpriteSheet = {
		val image = ImageIO.read(new File(path))
		val origin = if(offset == null){ new Point(0,0)} else {offset}
		val size = if(bounds == null){new Dimension(image.getWidth(), image.getHeight())} else {bounds}
		new SpriteSheet(image, path, origin, size, spriteSize, columnMajor)
	}
	
	def setupSprites(name:String, path:String, spriteSize:Dimension, offset:Point = null, bounds:Dimension = null, columnMajor:Boolean = false) = {
		addSpriteSheet(name, makeSpriteSheet(path, spriteSize, offset, bounds, columnMajor))
	}
	
	def getSprite(name:String, index:Any, tint:Color, chromaKey:Color):BufferedImage = {
		val sheet = spriteSheets(name)
		val idx:Point = index match {
			case p: Point => p
			case i: Int => if (sheet.columnMajor) {
				val div = (sheet.bounds.height / sheet.spriteBounds.height)
				new Point(i / div, i % div)
			} else {
				val div = (sheet.bounds.width / sheet.spriteBounds.width)
				new Point(i % div, i / div)
			}
		}
		val sprite = new BufferedImage(sheet.spriteBounds.width, sheet.spriteBounds.height, BufferedImage.TYPE_INT_ARGB)
		val gfx = sprite.createGraphics()
		gfx.drawImage(Toolkit.getDefaultToolkit().createImage(
				new FilteredImageSource(
						sheet.img.getSubimage(sheet.offset.x + idx.x * sheet.spriteBounds.width, sheet.offset.y + idx.y * sheet.spriteBounds.height, 
								sheet.spriteBounds.width, sheet.spriteBounds.height).getSource(),
						new ChromakeyFilter(tint, chromaKey))
				), 
				0, 0, null)
		gfx.dispose()
		sprite
	}
	
}

class SpriteSheet(val img:BufferedImage, val path:String, 
		val offset:Point, val bounds:Dimension, val spriteBounds:Dimension, val columnMajor:Boolean = false)
		
class ChromakeyFilter(tint:Color, chromaKey:Color) extends RGBImageFilter  {
	canFilterIndexColorModel = true
	def filterRGB(x:Int, y:Int, rgb:Int):Int = {
		val a = ((rgb & 0xff000000) >>> 24) //% 256
		val r = ((rgb & 0xff0000) >>> 16) //% 256
		val g = ((rgb & 0xff00) >>> 8) //% 256
		val b = (rgb & 0xff) //% 256
		
		val inColor:Color = new Color(rgb, true)
		val scale:Int = if(r == 0 && g == 0 && b == 0){1}else{List(r,g,b).filter(_ != 0).min}
		val testColor:Color = new Color(Math.min(255,chromaKey.getRed * scale), Math.min(255,chromaKey.getGreen * scale), Math.min(255,chromaKey.getBlue * scale))
		if(testColor.equals(inColor)) {
			val rtint = tint.getRed
			val gtint = tint.getGreen
			val btint = tint.getBlue
			(new Color(Math.min(255,tint.getRed * scale / 255), Math.min(255,tint.getGreen * scale / 255), Math.min(255,tint.getBlue * scale / 255), a)).getRGB()
		} else {
			rgb
		}
	}
}