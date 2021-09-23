/**
 * 
 */
package org.snowjak.city.map.renderer;

import org.codehaus.groovy.util.HashCodeHelper
import org.snowjak.city.util.RelativePriority;
import org.snowjak.city.util.RelativelyPrioritized

/**
 * Describes a layer onto which a {@link Module} can
 * render {@link Tile}s. These layers are {@link RelativePriority relatively
 * prioritized} with each other.
 * 
 * @author snowjak88
 *
 */
public class MapLayer implements RelativelyPrioritized<MapLayer,String> {
	
	String id
	
	final RelativePriority<String> relativePriority = new RelativePriority()
	
	@Override
	public String getRelativePriorityKey() {
		
		id
	}
	
	@Override
	public int hashCode() {
		
		HashCodeHelper.updateHash(HashCodeHelper.initHash(), id)
	}
}