/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.util.RelativePriority;
import org.snowjak.city.util.RelativelyPrioritized;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractCellRenderingHook
		implements CellRenderingHook, RelativelyPrioritized<AbstractCellRenderingHook, String> {
	
	private final String id;
	private final RelativePriority<String> relativePriority = new RelativePriority<>();
	
	public AbstractCellRenderingHook(String id) {
		
		this.id = id;
	}
	
	public String getId() {
		
		return id;
	}
	
	@Override
	public RelativePriority<String> getRelativePriority() {
		
		return relativePriority;
	}
	
	@Override
	public String getRelativePriorityKey() {
		
		return id;
	}
}
