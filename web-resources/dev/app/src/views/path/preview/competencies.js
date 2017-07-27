import competency from './competency.html';

const CompetencyItem = Marionette.View.extend({
    tagName: 'li',
    template: competency
});

const Competencies = Marionette.CollectionView.extend({
    childView: CompetencyItem
});

export default Competencies;