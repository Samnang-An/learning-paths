import PaginatedCollection from 'collections/paginated';
import appConfig from 'config';

const CoursesService = new Backbone.Service({
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

const Courses = PaginatedCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = appConfig().endpoint.courses;
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    },
    getGoalData() {
        return _.map(this.filter({selected: true}), (item) => {
            return {
                goalType: 'course',
                courseId: item.get('id')
            };
        });
    }
}).extend(CoursesService);

export default Courses