import PaginatedCollection from 'collections/paginated';
import ValamisModel from 'models/valamis';
import appConfig from 'config'

const MemberServices = new Backbone.Service({
    url(model) {
        return appConfig().endpoint.learningPaths + model.collection.pathId + '/members/'
            + model.collection.memberType
    }
});

const Member = ValamisModel.extend(MemberServices);

const MembersServices = new Backbone.Service({
    sync: {
        'read': {
            'path': (collection, options) => {
                return collection.pathId + '/members/' + collection.memberType;
            },
            'data': (collection, options) => {
                return  options.data
            },
            'method': 'get'
        }
    },
    targets: {
        'addMembers': {
            'path': (collection, options) => {
                return collection.pathId + '/members';
            },
            'data': (collection, options) => {
                return options.data
            },
            'method': 'post'
        }
    }
});

const Members = PaginatedCollection.extend({
    model: Member,
    initialize() {
        this.url = appConfig().endpoint.learningPaths;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    }
}).extend(MembersServices);

export default Members
