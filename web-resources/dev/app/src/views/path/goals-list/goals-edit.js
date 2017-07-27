
import editTemplate from './goals-edit-item.html';
import mainTemplate from './goals-main.html';

import EmptyGoalsView from './empty-goals';

import Goals from 'collections/goals';
import DurationUtil from 'utils/duration-utils';

import ValamisUIControls from 'behaviors/valamis-ui-controls';
import App from 'application';
import * as enumerations from 'models/enumerations';
import Radio from 'backbone.radio';

const PathGoalItem = Marionette.View.extend({
    tagName: 'li',
    className() {
        return (!this.model.isGroup()) ? 'js-single-goal' : '';
    },
    template: editTemplate,
    templateContext() {
        let periodTypes = _.map(enumerations.validPeriodTypes, (value, key) => {
            return { value: key, label: App.language[value + 'PeriodLabel'] };
        });

        let childrenToComplete = '';
        if (this.model.isGroup()) {
            let count = this.model.get('count');
            childrenToComplete = (!isNaN(count)) ? count : 1;
        }

        return {
            isGroup: this.model.isGroup(),
            isEvent: this.model.isEvent(),
            isActivity: this.model.isActivity(),
            goalTypeLabel: App.language[this.model.get('goalType')+'GoalLabel'],
            periodTypes: periodTypes,
            cid: this.cid,
            toComplete: enumerations.childrenToComplete,
            childrenToComplete: childrenToComplete
        }
    },
    regions: {
        tree: {
            el: 'ul.js-group-children',
            replaceElement: true
        }
    },
    behaviors: [ValamisUIControls],
    ui: {
        handle: '> div .js-handle',
        deleteButton: '> div .js-delete',
        activitiesCount: '> div  .js-activities-count',
        title: '> div .js-group-title',
        mandatoryLayout: '> div .js-goal-mandatory',
        mandatory: '> div .js-goal-mandatory input',
        groupMandatory: '> div .js-group-mandatory',
        childrenToComplete: '> div .js-group-mandatory .js-children-to-complete',
        childrenToCompleteLabel: '> div .js-children-to-complete-label',
        setDeadline: '> div .js-set-deadline',
        deadline: '> div .js-deadline',
        deadlinePeriodValue: '> div .js-deadline-period-value',
        deadlinePeriodType: '> div .js-deadline-period-type',
    },
    events: {
        'click @ui.deleteButton': 'deleteGoal',
        'change @ui.activitiesCount': 'saveActivitiesCount',
        'change @ui.title': 'saveTitle',
        'change @ui.mandatory': 'saveMandatory',
        'click @ui.groupMandatory li': 'changeGroupMandatory',
        'change @ui.childrenToComplete': 'childrenToCompleteChanged',
        'click @ui.setDeadline': 'setDeadline',
        'change @ui.deadlinePeriodValue': 'updateDeadline',
        'change @ui.deadlinePeriodType': 'updateDeadline'
    },
    getChildren() {
        // get children using don elements because childView and models in collection
        // can be not up-to-date after sorting and grouping
        return this.$('> ul > li');
    },
    onRender() {
        this.on('valamis:controls:init', () => {
            if (this.model.isGroup()) {
                let childrenToComplete = enumerations.childrenToComplete;
                let value = (this.model.get('count') !== undefined)
                    ? childrenToComplete.any : childrenToComplete.custom;
                this.ui.groupMandatory.valamisDropDown('select', value);
                this.setChildrenCompleteText(this.model.get('count'));

                // prevent click to dropdown item event when click into input field
                this.ui.childrenToComplete.on('click', (event) => {
                    event.preventDefault();
                    event.stopPropagation();
                });
            }
        });

        this.$el.attr('id', this.model.get('id'));

        // catch the relocate sortable event
        this.ui.handle.on('update:goal:position', (e, goalObject, isFirstLevel) => {
            if (isFirstLevel) {
                this.ui.mandatoryLayout.removeClass('hidden');
            }
            if (this.model.get('id') == goalObject.id) {
                this.model.move({}, goalObject);
            }
        });

        this.ui.handle.on('update:children', () => {
            if (this.model.isGroup()) {
                this.setChildrenCompleteText(this.model.get('count'));
            }
        });

        if (this.model.get('timeLimit')) {
            let period = DurationUtil.getPeriod(this.model.get('timeLimit'));

            if (_.isEmpty(period)) {
                Radio.channel('notify').trigger('notify', 'warning', 'durationWarningLabel');
                console.log(App.language['durationWarningLabel'] + ' ' + this.model.get('timeLimit'));
            }
            else {
                this.ui.deadlinePeriodValue.val(period.value);
                this.ui.deadlinePeriodType.val(period.type);
            }

            this.toggleDeadlineFields(true);
        }

        let goals = this.model.get('goals');
        if (goals && goals.length) {
            let groupView = new PathGoalsGroup({
                collection: new Goals(goals)
            });

            this.showChildView('tree', groupView);
        }
    },
    deleteGoal() {
        this.model.destroy().then(
            () => {},
            () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
        );
    },
    saveActivitiesCount() {
        this.setFields({'count': parseInt(this.ui.activitiesCount.val()) || 1});
    },
    saveTitle() {
        this.setFields({'title': this.ui.title.val() || App.language['newGroupLabel']});
    },
    saveMandatory() {
        this.setFields({'optional': !this.ui.mandatory.prop('checked')});
    },
    setChildrenCompleteText(count, total) {
        let text = '';
        total = total || this.getChildren().length;

        if (count > total) {
            Radio.channel('notify').trigger('notify', 'warning',
                App.language['toBeCompletedWarningLabel'] + ' ' + this.model.get('title'));
        }

        if (count !== undefined) {
            text = App.language['anyLabel'] + ' ' + count + ' '
                + App.language['ofLabel'] + ' ' + total;
        }
        else {
            text = App.language['customLabel'];
        }
        this.ui.childrenToCompleteLabel.text(text);
        this.getChildren().find(this._uiBindings.mandatoryLayout).toggleClass('hidden', count !== undefined);
    },
    updateToCompleteValue(count) {
        let total = this.getChildren().length;

        if (count !== undefined) {
            this.setFields({'count': count});
        }
        else {
            this.unsetField('count');
        }
        this.setChildrenCompleteText(count, total);
    },
    childrenToCompleteChanged(e) {
        if (this.ui.groupMandatory.data('value') === enumerations.childrenToComplete.any) {
            this.updateToCompleteValue(parseInt($(e.target).val()) || 1)
        }
    },
    changeGroupMandatory(e) {
        let isCustom = $(e.target).attr('data-value') === enumerations.childrenToComplete.custom;
        let newValue = (!isCustom) ? parseInt(this.ui.childrenToComplete.val()) || 1 : undefined;
        this.updateToCompleteValue(newValue);
    },
    setDeadline() {
        this.toggleDeadlineFields(true);
        this.updateDeadline();
    },
    updateDeadline() {
        let value = parseInt(this.ui.deadlinePeriodValue.val());
        let timeLimit = '';
        if (!isNaN(value) && value > 0) {
            let type = this.ui.deadlinePeriodType.val();
            let obj = {};
            obj[type] = value;
            timeLimit = DurationUtil.getIsoDuration(obj);
        }
        else {
            this.ui.deadlinePeriodValue.val(1);
            this.toggleDeadlineFields(false);
        }

        if (timeLimit) {
            this.setFields({'timeLimit': timeLimit});
        }
        else {
            this.unsetField('timeLimit');
        }
    },
    toggleDeadlineFields(hasDeadline) {
        this.ui.setDeadline.toggleClass('hidden', hasDeadline);
        this.ui.deadline.toggleClass('hidden', !hasDeadline);
    },
    setFields(newFields) {
        this.model.set(newFields);
        this.saveGoal();
    },
    unsetField(field) {
        this.model.unset(field);
        this.saveGoal();
    },
    saveGoal() {
        this.model.save().then(
            () => {},
            () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
        );
    }
});

const PathGoalsGroup = Marionette.CollectionView.extend({
    tagName: 'ul',
    childView: PathGoalItem,
    emptyView: EmptyGoalsView,
    initSortable() {
        let that = this;
        this.startParent = undefined;

        this.$el.nestedSortable({
            forcePlaceholderSize: true,
            handle: '.js-handle',
            items: 'li:not(.dropdown-item)',
            toleranceElement: '> div.list-table-row',
            listType: 'ul',
            placeholder: 'placeholder',
            disableNestingClass: 'js-single-goal',
            start: (e, ui) => {
                that.startParent = ui.item.parents('li');
            },
            relocate: (e, ui) => {
                // change regexp to allow item.attr('id') be a number, not string like 'item_1'
                let rawArray = that.$el.nestedSortable('toArray', { expression: /(?=(.+))(.+)/ });
                let sortArray = _.filter(rawArray, (i) => { return !!i.id});
                let id = ui.item.attr('id');
                let parentId = _.find(sortArray, {id: id}).parent_id;
                let siblings = _.filter(sortArray, {parent_id: parentId});
                let index = siblings.map((i) => { return i.id }).indexOf(id);

                let endParent = ui.item.parents('li');
                let isFirstLevel = endParent.length === 0;

                // fire update to view that was moved
                ui.item.find('> div .js-handle').trigger('update:goal:position', [{
                    id: parseInt(id),
                    parentId: parseInt(parentId),
                    index: index
                }, isFirstLevel]);

                // update mandatory settings in old and new group
                if (that.startParent.length > 0) {
                    that.startParent.find('> div .js-handle').trigger('update:children');
                }
                this.startParent = undefined;

                if (endParent.length > 0) {
                    endParent.find('> div .js-handle').trigger('update:children');
                }
            }
        });
    }
});

const PathGoalsEdit = Marionette.View.extend({
    template: mainTemplate,
    className: 'val-list-table',
    regions: {
        goalsList: '.js-goals-list'
    },
    ui: {
        header: '.js-header'
    },
    onRender() {
        this.goals = new Goals();

        if (this.options.pathId) {
            this.goals.pathId = this.options.pathId;

            this.goals.fetch({
                getVersion: false,
                activities: App.activities.toJSON()
            }).then(() => { this.showGoals(); });
        }
        else {
            this.showGoals();
        }
    },
    showGoals(){
        this.toggleHeader();
        this.goals.on('destroy', () => {
            this.toggleHeader();
        });

        let goalsView = new PathGoalsGroup({
            collection: this.goals
        });
        this.showChildView('goalsList', goalsView);
        if (!!this.goals.length) {
            goalsView.initSortable();
        }
    },
    toggleHeader() {
        this.ui.header.toggleClass('hidden', !this.goals.length);
    },
    onDestroy() {
        // destroy all events for case when view is destroyed before request complete
        this.off();
    }
});

export default PathGoalsEdit;