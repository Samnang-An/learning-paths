import PaginatedCollection from 'collections/paginated';
import LearningPath from 'models/learning-path';
import appConfig from 'config';

const LearningPaths = PaginatedCollection.extend({
    model: LearningPath,
    initialize(options) {
        this.url = appConfig().endpoint.users + 'current/learning-paths';
        PaginatedCollection.prototype.initialize.apply(this, arguments);
    }
});

export default LearningPaths
