import ValamisCollection from 'collections/valamis';
import PaginatedCollection from 'collections/paginated';
import appConfig from 'config';

const LevelsService = new Backbone.Service();

const Levels = ValamisCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = appConfig().endpoint.levels;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
}).extend(LevelsService);

export default Levels