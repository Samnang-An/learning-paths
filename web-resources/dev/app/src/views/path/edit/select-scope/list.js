import template from './item.html';

const ItemView  = Marionette.View.extend({
    tagName: 'tr',
    template: template,
    events: {
        'click': 'modelSelected'
    },
    modelSelected: function() {
        this.model.trigger('scope:item:selected', this.model.toJSON());
    }
});

const ListView = Marionette.CollectionView.extend({
    tagName: 'table',
    className: 'val-table',
    childView: ItemView
});

export default ListView;
