import template from './layout.html';

import ToolbarLayout from './toolbar';
import PathsListView from './list';

import App from 'application';
import Radio from 'backbone.radio';
import ModalView from 'modals/modal-view';
import * as Paging from "paging/paging";

import LearningPaths from 'collections/learning-paths';
import LearningPath from 'models/learning-path';
import LearningPathWithUserData from 'models/learning-path-for-user';

import TabsLayoutForUser from 'views/path/tabs-layout-for-user';
import TabsLayoutForAdmin from 'views/path/tabs-layout-for-admin';

const RootLayout = Marionette.View.extend({
    template: template,
    regions: {
        toolbar: '#learningPathsToolbar',
        content: '#learningPathsContent',
        paginator: '#learningPathsPaginator',
        paginatorShowing: '#learningPathsPaginatorShowing',
        modals: {
            selector: '#learningPathsModal',
            regionClass: Marionette.Modals
        }
    },
    ui: {
        loading: '.js-loading',
        createdByMeTab: '.js-paths-groups .js-creator'
    },
    childViewEvents: {
        'open:modal': 'openModal'
    },
    onRender() {
        this.paths = new LearningPaths();
        this.paginatorModel = new Paging.PageModel();

        this.paths.on('sync', (collection) => {
            // 'sync' fires also when collection model sync
            if (collection instanceof Backbone.Collection) {
                this.paginatorModel.set('totalElements', collection.options.total);
            }
        });
        this.paths.on('reload:paths', () => {
            this.fetchPaths();
        });
        this.paginatorModel.on('change:currentPage', () => {
            this.fetchPaths(true);
        });

        this.fetchFilter();
        let toolbar = new ToolbarLayout({ model: this.filter });
        this.showChildView('toolbar', toolbar);

        this.paginatorView = new Paging.ValamisPaginator({
            model: this.paginatorModel,
            topEdgeParentView: this,
            topEdgeSelector: this.regions.paginatorShowing
        });

        this.paginatorShowingView = new Paging.ValamisPaginatorShowing({
            model: this.paginatorModel
        });
        this.showChildView('paginator', this.paginatorView);
        this.showChildView('paginatorShowing', this.paginatorShowingView);


        let pathsList = new PathsListView({
            collection: this.paths,
            displayMode: this.filter.get('displayMode')
        });
        this.showChildView('content', pathsList);
        this.filter.on('change', (model) => {
            model.save();
            if (model.changedAttributes().hasOwnProperty('displayMode')) {
                pathsList.toggleDisplayMode(model.get('displayMode'));
            }
            else {
                this.fetchPaths();
            }
        });

        this.fetchPaths();

        if (App.openLpId) {
            this.openPathByLink(App.openLpId);
        }
    },
    fetchFilter() {
        this.filter = new Backbone.SettingsHelper({url: window.location.href, portlet: 'learningPaths'});
        this.filter.fetch();

        this.filter.set({
            'page': 0,
            'joined': true,
            'displayMode': this.filter.has('displayMode') ? this.filter.get('displayMode') : 'tiles',
            'sort': this.filter.has('sort') ? this.filter.get('sort') : '-createdDate'
        });
    },
    fetchPaths(keepPage) {
        this.ui.loading.removeClass('hidden');
        if (!keepPage) {
            this.paginatorModel.set({'currentPage': 1, 'totalElements': 0});
        }

        let take = this.paginatorModel.get('itemsOnPage');
        let skip = (this.paginatorModel.get('currentPage') - 1) * take;

        let data = {
            title: this.filter.get('title'),
            courseId: this.filter.get('courseId'),
            sort: this.filter.get('sort'),
            skip: skip,
            take: take
        };

        if (this.filter.get('userId') !== undefined) {
            data.userId = this.filter.get('userId');
        }
        if (this.filter.get('joined') !== undefined) {
            data.joined = this.filter.get('joined');
            data.activated = true;
        }

        this.paths.reset();
        this.paths.doAsAdmin = this.filter.get('joined') === undefined;

        this.paths.fetch({
            data: data
        }).then(
            () => { this.ui.loading.addClass('hidden'); },
            () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
        );
    },
    openModal: function(model) {

        if (!model) {
            this.$(this._uiBindings.createdByMeTab).click();
        }

        let modalChannel = Radio.channel('modal');
        let that = this;

        let pathView;
        if (App.permissions.modify) {
            pathView = new TabsLayoutForAdmin({
                model: model,
                doAsAdmin: this.paths.doAsAdmin
            });
        } else {
            pathView = new TabsLayoutForUser({
                model: model
            });
        }

        let InfoModalView = new ModalView({
            contentView: pathView,
            isSimple: true,
            onDestroy() {
                if (App.openLpId) {
                    window.location.assign(window.location.pathname);
                }
                else{
                    pathView.destroy();
                    that.fetchPaths(true);
                }
            }
        });

        pathView.on('modal:close', () => {
            modalChannel.trigger('close', InfoModalView);
        });

        modalChannel.trigger('open', InfoModalView);
    },
    openPathByLink(id) {
        let pathWithUserData = new LearningPathWithUserData({id: id});
        pathWithUserData.fetch().then(
            () => {
                let path = new LearningPath(pathWithUserData.toJSON());
                this.openModal(path);
            }
        );
    }
});

export default RootLayout;