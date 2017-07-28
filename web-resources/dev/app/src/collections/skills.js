import ValamisCollection from 'collections/valamis';
import PaginatedCollection from 'collections/paginated';
import appConfig from 'config';

const SkillsService = new Backbone.Service();

const Skills = ValamisCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = appConfig().endpoint.skills;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
}).extend(SkillsService);

export default Skills