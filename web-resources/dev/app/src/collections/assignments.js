import PaginatedCollection from 'collections/paginated';
import LiferayUtil from 'utils/liferay-utils';
import appConfig from 'config';

const AssignmentsService = new Backbone.Service({
    sync: {
        'read': {
            'data': (collection, options) => {
                let sort = options.filter.sort;
                let sortBy = sort.substr(sort.indexOf('-') + 1);
                let sortAscDirection = sort.indexOf('-') !== 0;

                return {
                    page: options.page,
                    count: options.count,
                    filter: options.filter.filter,
                    sortBy: sortBy,
                    sortAscDirection: sortAscDirection,
                    action: 'ALL',
                    status: 'Published',
                    courseId: LiferayUtil.courseId()
                }
            }
        }
    }
});

const Assignments = PaginatedCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = appConfig().endpoint.assignments;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
    getGoalData() {
        return _.map(this.filter({selected: true}), (item) => {
            return {
                goalType: 'assignment',
                assignmentId: item.get('id')
            };
        });
    },
    parse(response) {
        this.options.total = response.total;
        this.options.page = response.page;

        return response.records;
    }
}).extend(AssignmentsService);

export default Assignments