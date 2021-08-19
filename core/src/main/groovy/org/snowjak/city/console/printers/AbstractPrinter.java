/**
 * 
 */
package org.snowjak.city.console.printers;

import java.util.List;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Allows a Console to intelligently print something. A "smart
 * Object.toString()".
 * 
 * @author snowjak88
 *
 */
public abstract class AbstractPrinter<T> {
	
	private final ConsoleDisplay display;
	private final Skin skin;
	private final LabelStyle labelStyle, whiteLabelStyle;
	
	public AbstractPrinter(ConsoleDisplay display, Skin skin) {
		
		this.display = display;
		this.skin = skin;
		
		if (skin.has("console", LabelStyle.class))
			labelStyle = skin.get("console", LabelStyle.class);
		else
			labelStyle = skin.get(LabelStyle.class);
		
		if (skin.has("console-white", LabelStyle.class))
			whiteLabelStyle = skin.get("console-white", LabelStyle.class);
		else
			whiteLabelStyle = skin.get(LabelStyle.class);
	}
	
	/**
	 * Returns {@code true} if this ConsolePrinter can handle the given object.
	 * 
	 * @param obj
	 * @return
	 */
	public abstract boolean canPrint(Object obj);
	
	/**
	 * Print the given object -- converting it into one or more Actors (each of
	 * which will occupy a separate entry on the console).
	 * 
	 * @param obj
	 */
	public abstract List<Actor> print(T obj);
	
	/**
	 * @return the owning {@link ConsoleDisplay} instance
	 */
	public ConsoleDisplay getDisplay() {
		
		return display;
	}
	
	public Skin getSkin() {
		
		return skin;
	}
	
	/**
	 * The active Skin's {@code "console"} Label-style, or {@code "default"} as a
	 * fallback.
	 * 
	 * @return
	 */
	protected LabelStyle getLabelStyle() {
		
		return labelStyle;
	}
	
	/**
	 * The active Skin's {@code "console-white"} Label-style (suitable for tinting
	 * via {@link Label#setColor(Color) Label.setColor()}, or {@code "default"} as a
	 * fallback.
	 * 
	 * @return
	 */
	protected LabelStyle getWhiteLabelStyle() {
		
		return whiteLabelStyle;
	}
	
	/**
	 * Get a basic {@link Label} containing the specified text, and outfitted with
	 * the correct style.
	 * 
	 * @param text
	 * @return
	 */
	protected Label getNewLabel(String text) {
		
		final Label newLabel = new Label(text, getLabelStyle());
		newLabel.setWrap(true);
		newLabel.setFontScale(getDisplay().getScale());
		
		return newLabel;
	}
	
	/**
	 * Get a basic {@link Label} containing the specified text, and outfitted with
	 * the correct style.
	 * 
	 * @param text
	 * @param color
	 * @return
	 */
	protected Label getNewLabel(String text, Color color) {
		
		final Label newLabel = new Label(text, getWhiteLabelStyle());
		newLabel.setWrap(true);
		newLabel.setFontScale(getDisplay().getScale());
		newLabel.setColor(color);
		
		return newLabel;
	}
}
