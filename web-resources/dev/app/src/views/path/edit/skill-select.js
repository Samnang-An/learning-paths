import skillItem from './select-item.html';
import skillSelect from './skill-select.html';

const SkillItem = Marionette.View.extend({
    tagName: 'li',
    className: 'js-select-item',
    template: skillItem,
    events: {
        'click': 'onClick'
    },
    onClick() {
        this.model.trigger('skillSelected', {
            model: this.model.toJSON(),
            improving: this.options.improving
        });
    }
});

const SkillSelectCollection = Marionette.CollectionView.extend({
    childView: SkillItem,
    childViewOptions() {
        return {
            improving: this.options.improving
        }
    }
});

const SkillSelect = Marionette.View.extend({
    tagName: 'ul',
    className: 'dropdown-menu js-select-dropdown',
    template: skillSelect,
    regions: {
        list: {
            el: '.js-skills-list',
            replaceElement: true
        }
    },
    ui: {
        search: '.js-search input'
    },
    events: {
        'keyup @ui.search': 'onSearchChange'
    },
    onRender() {
        this.skills = new SkillSelectCollection({
            collection: this.collection,
            improving: this.options.improving
        });
        this.showChildView('list', this.skills);
    },
    onSearchChange() {
        const searchText = this.ui.search.val().toLowerCase();

        this.skills.setFilter((child, index, collection) => {
            return child.get('title').toLowerCase().includes(searchText);
        })
    }
});

export default SkillSelect;