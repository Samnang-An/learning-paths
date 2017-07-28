import PaginatedCollection from 'collections/paginated';
import LiferayUtil from 'utils/liferay-utils';
import appConfig from 'config';

const StatementsService = new Backbone.Service({
    sync: {
        'read': {
            'data': (collection, options) => {
                let sortAscDirection = options.filter.sort.indexOf('-') !== 0;
                return {
                    sortAscDirection: sortAscDirection,
                    sortBy: 'name',   // todo add sorting by date
                    filter: options.filter.filter,
                    page: options.page,
                    count: options.count,
                    courseId: LiferayUtil.courseId()
                }
            }
        }
    }
});

const Statements = PaginatedCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = appConfig().endpoint.statements;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
    getGoalData() {
        return _.map(this.filter({selected: true}), (item) => {
            return {
                goalType: 'statement',
                verbId: item.get('verb'),
                objectId: item.get('obj'),
                objectName: JSON.stringify(item.get('objName'))
            };
        });
    },
    parse(response) {
        this.options.total = response.total;
        this.options.page = response.page;

        return response.records;
    }
}).extend(StatementsService);

export default Statements