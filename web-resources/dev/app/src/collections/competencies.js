import ValamisCollection from 'collections/valamis';
import PaginatedCollection from 'collections/paginated';
import appConfig from 'config';

const CompetenciesService = new Backbone.Service({
    sync: {
        'read': {
            'path': (collection, options) => {
                let path = collection.pathId;
                if (!!options.improving) {
                    path += '/improving-competences';
                }
                else {
                    path += '/recommended-competences';
                }

                return path;
            },
            'method': 'get'
        }
    },
    targets: {
        addCompetency: {
            'path': (collection, options) => {
                let path = collection.pathId;
                if (!!options && !!options.data && options.data.improving) {
                    path += '/improving-competences';
                }
                else {
                    path += '/recommended-competences';
                }

                return path;
            },
            'data': (collection, options) => {
                return options.data
            },
            'method': 'post'
        },
        deleteCompetency: {
            'path': (collection, options) => {
                let path = collection.pathId;
                if (!!options.improving) {
                    path += '/improving-competences/skills/' + options.skillId
                }
                else {
                    path += '/recommended-competences/skills/' + options.skillId;
                }

                return path;
            },
            'method': 'delete'
        }
    }
});

const Competencies = ValamisCollection.extend({
    model: Backbone.Model,
    initialize(models, options) {
        if (!!options && !!options.getForVersion) {
            this.url = appConfig().endpoint.versions;
        }
        else {
            this.url = appConfig().endpoint.learningPaths;
        }

        PaginatedCollection.prototype.initialize.apply(this, arguments);
    }
}).extend(CompetenciesService);

export default Competencies;