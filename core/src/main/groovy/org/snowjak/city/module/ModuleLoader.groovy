/**
 * 
 */
package org.snowjak.city.module;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.snowjak.city.GameData;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.tiles.Tile;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.service.TileSetService;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * @author snowjak88
 *
 */
public class ModuleLoader extends AsynchronousAssetLoader<Module, AssetLoaderParameters<Module>> {
	
	private static final Logger LOG = LoggerService.forClass(ModuleLoader.class)
	
	private final TileSetService tilesetService
	private final CompilerConfiguration config
	private final Map<FileHandle, Module> moduleScripts = new HashMap<>()
	
	public ModuleLoader(FileHandleResolver resolver, TileSetService tilesetService) {
		
		super(resolver)
		
		final ImportCustomizer customizer = new ImportCustomizer()
		customizer.addStarImports "org.snowjak.city.ecs.components"
		customizer.addStarImports "com.badlogic.ashley.core"
		customizer.addImports(
				//
				// jCity types
				CityMap.class.getName(), GameData.class.getName(), Tile.class.getName(), TileSet.class.getName())
		
		config = new CompilerConfiguration()
		config.addCompilationCustomizers customizer
		config.scriptBaseClass = DelegatingScript.class.name
		
		this.tilesetService = tilesetService
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters parameter) {
		
		try {
			final GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), config)
			shell.config.scriptBaseClass = DelegatingScript.class.name
			final DelegatingScript moduleScript = (DelegatingScript) shell.parse(file.file())
			
			final Module module = new Module()
			module.tileSetService = tilesetService
			
			moduleScript.setDelegate(module)
			moduleScript.run()
			
			moduleScript.getBinding().getVariables().forEach({k, v -> module.getBinding().setVariable((String) k, v)})
			
			moduleScripts.put(file, module)
			
		} catch (IOException | CompilationFailedException e) {
			LOG.error e, "Cannot load module \"{0}\" [{1}]", file.nameWithoutExtension(), file.path()
		}
	}
	
	@Override
	public Module loadSync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters parameter) {
		
		if (!moduleScripts.containsKey(file) || moduleScripts.get(file) == null)
			return null
		
		moduleScripts.get(file)
	}
	
	
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters parameter) {
		
//		final Array<AssetDescriptor> dependencies = new Array<>()
//		
//		try {
//			final GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), config)
//			final DelegatingScript moduleScript = (DelegatingScript) shell.parse(file.file())
//			
//			final Module module = new Module()
//			
//			def foundTileSetFiles = tilesetService.scanDirectoryForScripts()
//			
//			def tilesetProxy = new groovy.util.Proxy()
//			tilesetProxy.wrap(tilesetService)
//			tilesetProxy.metaClass."get" = {name, wait=false ->
//				// Log the dependency
//				def fh = foundTileSetFiles.find { tilesetService.getNameFromFileHandle(it) == name }
//				if(fh != null) {
//					println "Intercepting a dependency: $name --> ${fh.path()}"
//					dependencies.add new AssetDescriptor(fh, TileSet)
//				}
//				// and return nothing
//				null
//			}
//			
//			module.tileSetService = tilesetProxy
//			
//			moduleScript.delegate = module
//			moduleScript.run()
//		} catch (IOException | CompilationFailedException e) {
//			LOG.error(e, "Cannot load module \"{0}\" [{1}]", file.nameWithoutExtension(), file.path())
//			return null
//		}
//		
//		dependencies
		null
	}
	
	public static class ModuleLoaderParameters extends AssetLoaderParameters<Module> {
	}
}