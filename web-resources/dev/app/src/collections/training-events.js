import PaginatedCollection from 'collections/paginated';
import appConfig from 'config';
import LiferayUtil from 'utils/liferay-utils';

const TrainingEventsService = new Backbone.Service({
    sync: {
        'read': {
            'data': (collection, options) => {
                let sortAscDirection = options.filter.sort.indexOf('-') !== 0;

                return {
                    sortAscDirection: sortAscDirection,
                    sortBy: 'name',
                    courseId: LiferayUtil.courseId(),
                    page: options.page,
                    count: options.count,
                    filter: options.filter.filter,
                }
            }
        }
    }
});

const TrainingEvents = PaginatedCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = appConfig().endpoint.trainingEvents;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
    getGoalData() {
        return _.map(this.filter({selected: true}), (item) => {
            return {
                goalType: 'trainingEvent',
                trainingEventId: item.get('id')
            };
        });
    },
    parse(response) {
        this.options.total = response.total;
        this.options.page = response.page;

        return response.records;
    }
}).extend(TrainingEventsService);

export default TrainingEvents