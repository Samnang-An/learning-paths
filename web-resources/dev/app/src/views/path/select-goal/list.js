import template from './item.html';
import activityTemplate from './item-activity.html';
import lessonTemplate from './item-lesson.html';
import statementTemplate from './item-statement.html';
import StatementUtil from 'utils/statement-utils';

import ValamisUIControls from 'behaviors/valamis-ui-controls';
import * as enumerations from 'models/enumerations';

const ItemView  = Marionette.View.extend({
    tagName: 'tr',
    template: template,
    templateContext() {
        return { cid: this.cid }
    },
    ui: {
        checkbox: '.val-checkbox'
    },
    events: {
        'change @ui.checkbox': 'toggleModelSelected'
    },
    modelEvents: {
        'change:selected': 'toggleSelected'
    },
    toggleModelSelected(e) {
        // change event fires only in case of user activity
        // so it will not be triggered when select all members happens
        let isSelected = $(e.target).prop('checked');
        this.model.set('selected', isSelected);

        this.model.trigger('item:selected:changed', (isSelected) ? 1 : -1 );
    },
    toggleSelected: function() {
        this.ui.checkbox.prop('checked', this.model.get('selected'));
    }
});

const LessonItemView = ItemView.extend({
    template: lessonTemplate
});

const StetementItemView = ItemView.extend({
    template: statementTemplate,
    templateContext() {
        return {
            cid: this.cid,
            displayVerb: StatementUtil.getLangDictionaryTincanValue(this.model.get('verbName')),
            displayObj: StatementUtil.getLangDictionaryTincanValue(this.model.get('objName'))
        }
    }
});

const ActivityItemView  = ItemView.extend({
    template: activityTemplate,
    ui() {
        return _.extend({}, ItemView.prototype.ui, {
            count: '.js-count'
        });
    },
    events() {
        return _.extend({}, ItemView.prototype.events, {
            'keypress @ui.count': 'updateCount'
        });
    },
    behaviors: [ValamisUIControls],
    updateCount(e) {
        this.model.set('count', parseInt($(e.target).val()) || 1);
        this.model.set('selected', true);
    },
    toggleModelSelected(e) {
        ItemView.prototype.toggleModelSelected.apply(this, [e]);
        let isSelected = $(e.target).prop('checked');
        let newVal = (isSelected) ? 1 : '';
        this.ui.count.val(newVal);
        this.model.set('count', newVal);
    }

});

const ListView = Marionette.CollectionView.extend({
    tagName: 'table',
    className: 'val-table',
    childView() {
        let goalTypes = enumerations.goalTypes;
        switch(this.collection.goalType) {
            case goalTypes.activity:
                return ActivityItemView;
            case goalTypes.lesson:
                return LessonItemView;
            case goalTypes.statement:
                return StetementItemView;
            default:
                return ItemView;
        }
    }
});

export default ListView;
