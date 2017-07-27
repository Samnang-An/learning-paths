import PaginatedCollection from 'collections/paginated';
import LiferayUtil from 'utils/liferay-utils';
import appConfig from 'config';

const LessonsService = new Backbone.Service({
    sync: {
        'read': {
            'data': (collection, options) => {
                let sortAscDirection = options.filter.sort.indexOf('-') !== 0;
                return {
                    sortAscDirection: sortAscDirection,
                    filter: options.filter.filter,
                    page: options.page,
                    count: options.count,
                    action: 'ALL',
                    scope: 'instance',
                    courseId: LiferayUtil.courseId()
                }
            }
        }
    }
});

const Lessons = PaginatedCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = appConfig().endpoint.lessons;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
    getGoalData() {
        return _.map(this.filter({selected: true}), (item) => {
            return {
                goalType: 'lesson',
                lessonId: item.get('lesson').id
            };
        });
    },
    parse(response) {
        this.options.total = response.total;
        this.options.page = response.page;

        return response.records;
    }
}).extend(LessonsService);

export default Lessons