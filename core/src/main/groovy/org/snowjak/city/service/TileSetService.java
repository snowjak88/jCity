/**
 * 
 */
package org.snowjak.city.service;

import java.util.LinkedHashSet;
import java.util.Set;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.tiles.TileSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * Provides Tileset-related application-level services.
 * <p>
 * Handles:
 * <ul>
 * <li>Detecting all tile-sets available at boot-time</li>
 * <li>Loading each detected tile-set at boot-time</li>
 * <li>Merging all loaded tile-sets into a single TileSet instance</li>
 * <li>Making that single TileSet instance available</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
@Component
public class TileSetService {
	
	private static final Logger LOG = LoggerService.forClass(TileSetService.class);
	
	@Inject
	private AssetService assetService;
	
	private TileSet tileSet = null;
	
	private final Set<FileHandle> loadedScripts = new LinkedHashSet<>();
	
	public TileSet getTileSet() {
		
		if (tileSet == null)
			synchronized (this) {
				if (tileSet == null) {
					loadedScripts.forEach(f -> {
						assetService.finishLoading(f.path(), TileSet.class);
						mergeTileSet(assetService.get(f.path(), TileSet.class));
					});
				}
			}
		
		return tileSet;
	}
	
	public void mergeTileSet(TileSet toMerge) {
		
		final TileSet newTileSet;
		
		if (toMerge == null)
			return;
		
		try {
			if (this.tileSet == null)
				newTileSet = new TileSet(toMerge);
			else
				newTileSet = this.tileSet.merge(toMerge);
		} catch (IllegalArgumentException e) {
			return;
		}
		
		this.tileSet = newTileSet;
	}
	
	@Initiate(priority = AutumnActionPriority.LOW_PRIORITY)
	public void init() {
		
		LOG.info("Initializing ...");
		
		loadedScripts.addAll(scanDirectoryForScripts(Gdx.files.local(CityGame.EXTERNAL_ROOT_TILESETS)));
		loadedScripts.forEach(f -> assetService.load(f.path(), TileSet.class));
		
		LOG.info("Finished initializing.");
	}
	
	private Set<FileHandle> scanDirectoryForScripts(FileHandle directory) {
		
		final Set<FileHandle> results = new LinkedHashSet<>();
		
		if (directory == null || !directory.exists())
			return results;
		
		LOG.info("Scanning [{0}]", directory.path());
		
		for (FileHandle child : directory.list())
			if (child.isDirectory())
				results.addAll(scanDirectoryForScripts(child));
			else if (child.name().toLowerCase().endsWith(".groovy")) {
				LOG.info("Found script-file [{0}]", child.path());
				results.add(child);
			}
		
		return results;
	}
}