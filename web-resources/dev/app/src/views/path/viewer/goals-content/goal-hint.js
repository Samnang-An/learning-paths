import goalTemplate from './goal-hint.html';

const Goal = Marionette.View.extend({
    template: goalTemplate,
    className: 'goal-hint',
    templateContext() {
        return {
            goalInfo: this.getOption('goalInfo')
        }
    }
});

export default Goal;