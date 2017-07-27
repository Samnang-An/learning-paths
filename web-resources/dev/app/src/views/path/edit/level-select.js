import levelItem from './select-item.html';
import levelSelect from './level-select.html';

const LevelItem = Marionette.View.extend({
    tagName: 'li',
    template: levelItem,
    events: {
        'click': 'onClick'
    },
    onClick() {
        this.model.trigger('levelSelected', this.model.toJSON(), this.options);
    }
});

const LevelSelectCollection = Marionette.CollectionView.extend({
    childView: LevelItem,
    childViewOptions() {
        return {
            skillName: this.options.skillName,
            skillId: this.options.skillId,
            improving: this.options.improving
        }
    }
});

const LevelSelect = Marionette.View.extend({
    tagName: 'ul',
    className: 'dropdown-menu js-select-dropdown',
    template: levelSelect,
    regions: {
        list: {
            el: '.js-levels-list',
            replaceElement: true
        }
    },
    templateContext() {
        return {
            'skillName': this.options.skillName
        }
    },
    onRender() {
        let levels = new LevelSelectCollection({
            collection: this.collection,
            skillName: this.options.skillName,
            skillId: this.options.skillId,
            improving: this.options.improving
        });
        this.showChildView('list', levels);
    }
});

export default LevelSelect;