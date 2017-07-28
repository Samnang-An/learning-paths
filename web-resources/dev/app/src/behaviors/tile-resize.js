import { mixin } from 'core-decorators';

@mixin({
    defaults:{
        tile: 'tile',
        list: 'list',
        tiles: 'tiles',
        delay: 100
    }
})
export default class TilesResize extends Marionette.Behavior {
    initialize() {
        $(window).resize(_.debounce(() => {
            this.resizeTiles();
        }, this.options.delay));

        this.view.on('display:mode:changed', (mode) => {
            this.$el.removeClass(this.options.tiles + ' ' + this.options.list).addClass(mode);
            this.resizeTiles();
        });

        this.view.on('render', () => {
            this.view.collection.on('sync', () => {
                _.delay(() => {
                    this.resizeTiles();
                }, this.options.delay);
            })
        });
    }
    resizeTiles () {
        if(this.view.$el.hasClass(this.options.tiles)){
            this.makeEqualHeight(this.view.$('.' + this.options.tile));
        }else {
            this.unsetHeight(this.view.$('.' + this.options.tile))
        }
    }

    makeEqualHeight(tiles)  {
        this.unsetHeight(tiles);
        let heights =  _.map(tiles, (tile) => {
            return $(tile).height();
        });
        let max = Math.max.apply(Math, heights);

        tiles.height(max);
    }

    unsetHeight($elems) {
        $elems.height('auto');
    }
}
