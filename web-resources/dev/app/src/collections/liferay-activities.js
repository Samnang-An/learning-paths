import ValamisCollection from 'collections/valamis';
import appConfig from 'config';

const LiferayActivities = ValamisCollection.extend({
    model: Backbone.Model,
    initialize(options) {
        this.url = `${appConfig().endpoint.liferayActivities}`;
        ValamisCollection.prototype.initialize.apply(this, arguments);
    },
    getGoalData() {
        return _.map(this.filter({selected: true}), (item) => {
            return {
                goalType: 'activity',
                activityName: item.get('activityName'),
                count: item.get('count') || 1
            };
        });
    }
});

export default LiferayActivities