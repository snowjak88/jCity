/**
 * 
 */
package org.snowjak.city.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.files.FileHandle;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractScriptService<S, R> implements ScriptResourceService<S, R> {
	
	private static final Logger LOG = LoggerService.forClass(AbstractScriptService.class);
	
	private final Class<S> toLoadType;
	private final ResourceScriptConverter<S, R> scriptConverter;
	private final AssetService assetService;
	private final FileHandle baseDirectory;
	private final boolean includeSubdirectories;
	private final String extension;
	
	private final Map<String, FileHandle> scriptFiles = new HashMap<>();
	private final Map<String, R> resources = new HashMap<>();
	
	public AbstractScriptService(Class<S> toLoadType, ResourceScriptConverter<S, R> scriptConverter,
			AssetService assetService, FileHandle baseDirectory, boolean includeSubdirectories, String extension) {
		
		this.toLoadType = toLoadType;
		this.scriptConverter = scriptConverter;
		this.assetService = assetService;
		this.baseDirectory = baseDirectory;
		this.includeSubdirectories = includeSubdirectories;
		this.extension = extension;
	}

	@Override
	public Set<String> getLoadedNames() {
		
		return resources.keySet();
	}
	
	@Override
	public FileHandle getFile(String name) {
		
		return scriptFiles.get(name);
	}
	
	@Override
	public R get(String name) {
		
		return get(name, true);
	}
	
	/**
	 * Get the resource associated with the given name, or {@code null} if no such
	 * resource was loaded.
	 * 
	 * @param name
	 * @param waitToFinish
	 * @return
	 */
	public R get(String name, boolean waitToFinish) {
		
		//
		// If we're still waiting for this resource to load --
		// we'd better wait for this resource to finish loading.
		//
		if (waitToFinish && scriptFiles.containsKey(name) && !resources.containsKey(name)) {
			assetService.finishLoading(scriptFiles.get(name).path(), toLoadType);
			checkLoadedResources();
			return get(name, true);
		}
		
		return resources.get(name);
	}
	
	/**
	 * Make sure that you:
	 * <ol>
	 * <li>Tag this with {@link Initiate @Initiate}</li>
	 * <li>Call {@link #initInternal()}</li>
	 * </ol>
	 * This method-stub is purely to give your resource-services an entry-point for
	 * which you can customize the priority.
	 */
	public abstract void init();
	
	protected void initInternal() {
		
		LOG.info("Initializing ...");
		
		scanDirectoryForScripts().forEach(f -> {
			scriptFiles.put(getNameFromFileHandle(f), f);
			assetService.load(f.path(), toLoadType);
		});
		
		assetService.addOnLoadAction(this::checkLoadedResources);
		
		LOG.info("Finished initializing.");
	}
	
	@Override
	public Set<FileHandle> scanDirectoryForScripts() {
		
		return scanDirectoryForScripts(baseDirectory);
	}
	
	private Set<FileHandle> scanDirectoryForScripts(FileHandle directory) {
		
		final Set<FileHandle> results = new LinkedHashSet<>();
		
		if (directory == null || !directory.exists())
			return results;
		
		LOG.info("Scanning [{0}]", directory.path());
		
		for (FileHandle child : directory.list())
			if (child.isDirectory() && includeSubdirectories)
				results.addAll(scanDirectoryForScripts(child));
			else if (child.name().toLowerCase().endsWith(extension)) {
				LOG.info("Found script-file [{0}]", child.path());
				results.add(child);
			}
		
		return results;
	}
	
	/**
	 * Inspect the modules we tried to load. Update our internal maps as
	 * appropriate.
	 */
	protected void checkLoadedResources() {
		
		//
		// Review our script files.
		// Remove those that failed to load.
		//
		final Iterator<Entry<String, FileHandle>> scriptsIterator = scriptFiles.entrySet().iterator();
		while (scriptsIterator.hasNext()) {
			
			final Entry<String, FileHandle> entry = scriptsIterator.next();
			final String name = entry.getKey();
			final FileHandle file = entry.getValue();
			
			final S script = assetService.get(file.path(), toLoadType);
			
			//
			// If the resource failed to load -- get rid of its script-file reference.
			if (script == null) {
				LOG.info("Failed to load script \"{0}\" [{1}].", name, file.path());
				scriptsIterator.remove();
				continue;
			}
			
			try {
				final R resource = scriptConverter.convert(script);
				
				//
				// Otherwise -- looks like it worked.
				// Store the resource reference here.
				resources.put(name, resource);
				
			} catch (Throwable t) {
				LOG.error(t, "Failed to expand script [{0}] into resource \"{1}\"", file.path(), name);
				scriptsIterator.remove();
				continue;
			}
		}
	}
	
	@FunctionalInterface
	public interface ResourceScriptConverter<S, R> {
		
		public R convert(S script);
	}
}
