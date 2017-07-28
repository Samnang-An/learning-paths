import { mixin } from 'core-decorators';
import ValamisModel from 'models/valamis';

const DisplayModes = {
    list: 'list',
    tiles: 'tiles'
};

@mixin({
    defaults: {
        displayMode: DisplayModes.tiles,
        page: 0
    }
})
export default class RootModel extends ValamisModel {
    isTiles() {
        return this.get('displayMode') == DisplayModes.tiles;
    }
    isList() {
        return this.get('displayMode') == DisplayModes.list;
    }
}