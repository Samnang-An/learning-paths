import template from './toolbar.html';

import App from 'application';
import appConfig from 'config';
import ValamisUIControls from 'behaviors/valamis-ui-controls';
import * as enumerations from 'models/enumerations';

const ToolbarLayout = Marionette.View.extend({
    template : template,
    templateContext() {
        let goalsToOmit = [enumerations.goalTypes.group];
        if (!appConfig().isAssignmentDeployed) {
            goalsToOmit.push(enumerations.goalTypes.assignment);
        }
        if (!appConfig().isTrainingEventDeployed) {
            goalsToOmit.push(enumerations.goalTypes.trainingEvent);
        }
        if (!appConfig().isValamisDeployed) {
            goalsToOmit.push(enumerations.goalTypes.statement);
            goalsToOmit.push(enumerations.goalTypes.lesson);
        }

        let goals = _.omit(enumerations.goalTypes, goalsToOmit);
        let goalTypesList = _.map(goals, (value, key) => {
            return { value: value, label: App.language[key + 'OptionLabel'] };
        });

        return {
            isActivity: this.model.get('isActivity'),
            goalTypesList: goalTypesList
        }
    },
    ui: {
        'search': '.js-search input',
        'sort': '.js-sort',
        'goalType': '.js-goal-type',
        'selectAll': '.js-select-all',
        'deselectAll': '.js-deselect-all',
        'selectedInfo': '.js-selected-info',
        'selectedAmount': '.js-selected-amount'
    },
    events: {
        'keyup @ui.search': 'changeSearchText',
        'click @ui.sort li': 'changeSort',
        'click @ui.goalType li': 'changeGoalType',
        'change @ui.selectAll': 'toggleSelectAll',
        'click @ui.deselectAll': 'deselectAll'
    },
    behaviors: [ValamisUIControls],
    onRender() {
        this.isSelectedAll = false;
        this.selectedItems = 0;

        this.on('valamis:controls:init', function() {
            this.ui.goalType.valamisDropDown('select', this.model.get('goalType'));
            this.ui.sort.valamisDropDown('select', this.model.get('sort'));
        }, this);
    },
    changeSearchText: function(e) {
        clearTimeout(this.inputTimeout);
        this.inputTimeout = setTimeout(() => {
            this.model.set('filter', $(e.target).val());
        }, 800);
    },
    changeSort: function(e){
        this.model.set('sort', $(e.target).attr('data-value'));
    },
    changeGoalType: function(e){
        let goalType = $(e.target).attr('data-value');
        this.triggerMethod('change:goal:type', goalType);
    },
    toggleSelectAll: function () {
        this.isSelectedAll = this.ui.selectAll.prop('checked');
        this.selectedItems = (this.isSelectedAll)
            ? this.options.paginatorModel.getShowingCount() : 0;

        this.updateSelectedText(this.isSelectedAll);
        this.triggerMethod('toggle:select:all', this.isSelectedAll);
    },
    deselectAll: function () {
        this.ui.selectAll.prop('checked', false);
        this.toggleSelectAll();
    },
    updateSelectedText: function (isSmthSelected) {
        this.ui.selectedInfo.toggleClass('hidden', !isSmthSelected);
        this.ui.selectedAmount.text(this.selectedItems);
    },
    checkSelectedAmount: function (difference, isSmthSelected) {
        this.selectedItems += difference;
        this.ui.selectAll.prop('checked',
            this.selectedItems == this.options.paginatorModel.getShowingCount());
        this.updateSelectedText(isSmthSelected);
    }
});

export default ToolbarLayout;
