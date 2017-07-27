import ValamisModel from 'models/valamis';
import appConfig from 'config';

const LearningPathWithUserDataService = new Backbone.Service({
    url: function () {
        return appConfig().endpoint.users + 'current/learning-paths'
    }
});

const LearningPathWithUserData = ValamisModel.extend(LearningPathWithUserDataService);

export default LearningPathWithUserData
