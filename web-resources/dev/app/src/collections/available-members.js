import PaginatedCollection from 'collections/paginated';
import ValamisModel from 'models/valamis';
import appConfig from 'config'

const AvailableMembersServices = new Backbone.Service({
    sync: {
        'read': {
            'path': (collection, options) => {
                return collection.pathId + '/available-members/' + collection.memberType;
            },
            'data': (collection, options) => {
                return  options.data
            },
            'method': 'get'
        }
    }
});

const AvailableMembers = PaginatedCollection.extend({
    model: ValamisModel,
    initialize() {
        this.url = `${appConfig().endpoint.learningPaths}`;
        this.available = true;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
    getMembersSum() {
        let sum = 0;
        _.each(this.toJSON(), (i) => {
            let userCount = i.userCount;
            sum += (userCount != undefined) ? userCount : 1;
        });
        return sum;
    }
}).extend(AvailableMembersServices);

export default AvailableMembers
