import java.lang.Math

id = 'terrain'
description = 'Handles fitting terrain-tiles to the map.'

//
// Get the configured tile-set name to use for the landscape,
// with a pre-programmed fallback
tilesetName = preferences.getString('tileset-name', 'default')

//
// Get that tileset from the tile-set service.
// Note that we need to mark that tile-set as explicitly depended-upon.
//
dependsOn tilesetName, TileSet

tileset = assets.getByID tilesetName, TileSet

//
// This module's systems need this Component-class.
//
class IsTerrainTile implements Component {
	
	List<Tile> tiles = []
}

//
// ComponentMappers make us faster at querying and retrieving Components from entities
terrainMapper = ComponentMapper.getFor(IsTerrainTile)

//
// This module declares its entity-processing systems in another file.
// That file is loaded and processed now.
//
include 'systems.groovy'

//
// Declare a cell-rendering hook into the map-rendering loop.
// This will be called every frame for every on-screen map-cell.
//
// Note that this hook as a name.
// Names must be unique for cell-rendering hooks. Hooks that are
// registered later will overwrite those registered earlier.
//
cellRenderHook 'terrainRender', { delta, cellX, cellY, support ->
	def entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(terrainMapper.has(entity))
			for(def tile in terrainMapper.get(entity).tiles)
				support.renderTile cellX, cellY, tile
}

//
// We define a "clouds" renderer in this file ...
//
include 'renderers/cloudRenderer.groovy'

//
// We define all our terain-tools in this file ...
//
include 'ui/tools.groovy'