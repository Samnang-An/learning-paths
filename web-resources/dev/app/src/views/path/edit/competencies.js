import competency from './competency.html';

const CompetencyItem = Marionette.View.extend({
    tagName: 'li',
    template: competency,
    events: {
        'click .js-delete-competency': 'deleteCompetency'
    },
    deleteCompetency() {
        this.model.destroy();
    }
});

const Competencies = Marionette.CollectionView.extend({
    childView: CompetencyItem
});

export default Competencies;