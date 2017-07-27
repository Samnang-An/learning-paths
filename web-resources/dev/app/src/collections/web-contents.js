import PaginatedCollection from 'collections/paginated';
import appConfig from 'config';

const WebContentsService = new Backbone.Service({
    sync: {
        'read': {
            'data': (collection, options) => {
                let take = options.count;
                let skip = (options.page - 1) * take;
                return {
                    take: take,
                    skip: skip,
                    title: options.filter.filter,
                    sort: options.filter.sort
                }
            }
        }
    }
});

const WebContents = PaginatedCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = appConfig().endpoint.webContents;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
    getGoalData() {
        return _.map(this.filter({selected: true}), (item) => {
            return {
                goalType: 'webContent',
                webContentId: item.get('id')
            };
        });
    }
}).extend(WebContentsService);

export default WebContents