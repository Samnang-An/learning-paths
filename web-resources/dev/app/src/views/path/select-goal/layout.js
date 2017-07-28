import template from './layout.html';

import RootModel from 'models/root';
import * as Paging from 'paging/paging';
import * as enumerations from 'models/enumerations';
import Radio from 'backbone.radio';

import ToolbarLayout from './toolbar';
import ListView from './list';

import LiferayActivities from 'collections/liferay-activities';
import Courses from 'collections/courses';
import Assignments from 'collections/assignments';
import Lessons from 'collections/lessons';
import WebContents from 'collections/web-contents';
import TrainingEvents from 'collections/training-events';
import Statements from 'collections/statements';

const getGoalsCollection = function(selectedType) {
    let goalTypes = enumerations.goalTypes;
    let collection = undefined;

    switch(selectedType) {
        case goalTypes.activity:
            collection = new LiferayActivities();
            break;
        case goalTypes.course:
            collection = new Courses();
            break;
        case goalTypes.assignment:
            collection = new Assignments();
            break;
        case goalTypes.lesson:
            collection = new Lessons();
            break;
        case goalTypes.webContent:
            collection = new WebContents();
            break;
        case goalTypes.trainingEvent:
            collection = new TrainingEvents();
            break;
        case goalTypes.statement:
            collection = new Statements();
            break;
    }

    return collection;
};

const SelectGoalLayout = Marionette.View.extend({
    template: template,
    regions: {
        toolbar: '#viewToolbar',
        content: '#viewContent',
        more: '#viewPaginatorMore',
        showing: '#viewPaginatorShowing'
    },
    ui: {
        back: '.js-back',
        add: '.js-add-goals',
        loading: '.js-loading-container',
        infoLabel: '.js-info-label'
    },
    events: {
        'click @ui.back': 'goBack',
        'click @ui.add': 'addGoals'
    },
    childViewEvents: {
        'change:goal:type': 'onGoalTypeChange',
        'toggle:select:all': 'toggleSelectAll'
    },
    initialize() {
        this.goalType = this.options.goalType
    },
    onGoalTypeChange(goalType) {
        this.goalType = goalType;
        this.render();
    },
    onRender() {
        this.filter = new RootModel({
            goalType: this.goalType,
            isActivity: this.goalType === enumerations.goalTypes.activity,
            sort: 'title'
        });

        this.collection = getGoalsCollection(this.goalType);

        this.collection.goalType = this.goalType;
        this.paginatorModel = new Paging.PageModel({itemsOnPage: 10, isLazy: true});

        let toolbar = new ToolbarLayout({
            model: this.filter,
            paginatorModel: this.paginatorModel
        });
        this.showChildView('toolbar', toolbar);

        if (!this.filter.get('isActivity')) {

            this.filter.on('change', (model) => {
                toolbar.deselectAll();
                this.fetch();
            });

            this.paginatorModel.on('pageChanged', () => { this.fetchMore() });

            this.collection.on('sync', (collection) => {
                this.toggleLoading(false, !collection.hasItems());
                this.paginatorModel.set('totalElements', collection.options.total);
                this.paginatorModel.set('totalElements', collection.options.total);
            });
            this.collection.on('item:selected:changed', (difference) => {
                let isSmthSelected = this.collection.filter({'selected': true}).length > 0;
                toolbar.checkSelectedAmount(difference, isSmthSelected);
            }, this);

            let paginatorShowMoreView = new Paging.ValamisPaginatorShowMore({
                model: this.paginatorModel
            });
            this.showChildView('more', paginatorShowMoreView);

            let paginatorShowingView = new Paging.ValamisPaginatorShowing({
                model: this.paginatorModel
            });
            this.showChildView('showing', paginatorShowingView);
        }

        let listView = new ListView({ collection: this.collection });
        this.showChildView('content', listView);

        this.fetch();
    },
    fetch: function() {
        this.paginatorModel.set({ 'totalElements': 0 });
        this.collection.reset();
        let count = this.paginatorModel.get('currentPage') * this.paginatorModel.get('itemsOnPage');
        this.fetchMore(1, count);
    },
    fetchMore: function(page, count) {

        if (!this.filter.get('isActivity')) {
            this.toggleLoading(true);
        }

        this.collection.fetch({
            filter: this.filter.toJSON(),
            page: page || this.paginatorModel.get('currentPage'),
            count: count || this.paginatorModel.get('itemsOnPage'),
            add: true,
            remove: false,
            merge: false
        });
    },
    toggleSelectAll: function (selected) {
        this.collection.each((i) => { i.set('selected', selected) });
    },
    toggleLoading: function(loading, emptyList) {
        this.ui.loading.toggleClass('hidden', !loading);
        this.ui.infoLabel.toggleClass('hidden', loading || !emptyList)
    },
    addGoals() {
        let goalData = this.collection.getGoalData({selected: true});
        let defArray = [];
        _.each(goalData, (data) => {
            let def = $.Deferred();
            defArray.push(def);
            this.options.pathDraft.addGoals({}, {data: data}).then(
                () => {def.resolve();}
            );
        });
        $.when.apply(this, defArray).then(
            () => { this.goBack(); }
        );
    },
    goBack() {
        this.triggerMethod('select:goals:back');
    }
});

export default SelectGoalLayout;