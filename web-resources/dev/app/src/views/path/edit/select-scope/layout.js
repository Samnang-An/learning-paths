import template from './layout.html';

import RootModel from 'models/root';
import * as Paging from 'paging/paging';

import ToolbarLayout from './toolbar';
import ListView from './list';

import Courses from 'collections/courses';

const SelectScopeLayout = Marionette.View.extend({
    template: template,
    regions: {
        toolbar: '#viewToolbar',
        content: '#viewContent',
        more: '#viewPaginatorMore',
        showing: '#viewPaginatorShowing'
    },
    ui: {
        loading: '.js-loading-container',
        infoLabel: '.js-info-label'
    },
    onRender() {
        this.filter = new RootModel({
            sort: 'title'
        });

        let toolbar = new ToolbarLayout({ model: this.filter });
        this.showChildView('toolbar', toolbar);

        this.collection = new Courses();
        this.collection.on('scope:item:selected', (scopeInfo) => {
            this.setScope(scopeInfo);
        });

        this.collection.goalType = this.goalType;
        this.paginatorModel = new Paging.PageModel({itemsOnPage: 10, isLazy: true});

        this.filter.on('change', (model) => { this.fetch(); });

        this.paginatorModel.on('pageChanged', () => { this.fetchMore() });

        this.collection.on('sync', (collection) => {
            this.toggleLoading(false, !collection.hasItems());
            this.paginatorModel.set('totalElements', collection.options.total);
        });

        let paginatorShowMoreView = new Paging.ValamisPaginatorShowMore({
            model: this.paginatorModel
        });
        this.showChildView('more', paginatorShowMoreView);

        let paginatorShowingView = new Paging.ValamisPaginatorShowing({
            model: this.paginatorModel
        });
        this.showChildView('showing', paginatorShowingView);

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
        this.collection.fetch({
            filter: this.filter.toJSON(),
            page: page || this.paginatorModel.get('currentPage'),
            count: count || this.paginatorModel.get('itemsOnPage'),
            add: true,
            remove: false,
            merge: false
        });
    },
    toggleLoading: function(loading, emptyList) {
        this.ui.loading.toggleClass('hidden', !loading);
        this.ui.infoLabel.toggleClass('hidden', loading || !emptyList)
    },
    setScope(scopeInfo) {
        this.triggerMethod('scope:selected', scopeInfo);
    }
});

export default SelectScopeLayout;